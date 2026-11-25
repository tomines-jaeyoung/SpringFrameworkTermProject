package kr.ac.kopo.kim.onairmarket.service;

import kr.ac.kopo.kim.onairmarket.domain.AdminAudit;
import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.domain.Product;
import kr.ac.kopo.kim.onairmarket.domain.TimedAuction;
import kr.ac.kopo.kim.onairmarket.domain.TimedBid;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 기능: 현황 요약, 회원 정지·관리자 지정, 경매 숨김·복구, 즉시 구매 상품 삭제, 관리 이력.
 * 모든 변경은 admin_audit 에 기록한다. 화면 값은 믿지 않고 여기서 다시 검사한다.
 */
@Service
@RequiredArgsConstructor
public class AdminService {
    private static final int PAGE_SIZE = 15;

    private final MemberRepository memberRepository;
    private final TimedAuctionRepository auctionRepository;
    private final TimedBidRepository bidRepository;
    private final ProductRepository productRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AdminAuditRepository auditRepository;

    // ------------------------------------------------------------------ 조회

    /** 대시보드 숫자 (테이블별 행 수) */
    @Transactional(readOnly = true)
    public Map<String, Long> stats() {
        long now = System.currentTimeMillis();
        Map<String, Long> s = new LinkedHashMap<>();
        s.put("members", memberRepository.count());
        s.put("suspended", memberRepository.countBySuspendedTrue());
        s.put("auctions", auctionRepository.count());
        s.put("openAuctions", auctionRepository.countByArchivedFalseAndStartsAtLessThanEqualAndEndsAtGreaterThan(now, now));
        s.put("archived", auctionRepository.countByArchivedTrue());
        s.put("bids", bidRepository.count());
        s.put("bookmarks", bookmarkRepository.count());
        s.put("selling", productRepository.countByStatus(Product.SELLING));
        s.put("sold", productRepository.countByStatus(Product.SOLD));
        s.put("audits", auditRepository.count());
        return s;
    }

    @Transactional(readOnly = true)
    public List<AdminAudit> recentAudits() {
        return auditRepository.findTop8ByOrderByCreatedDesc();
    }

    @Transactional(readOnly = true)
    public Page<AdminAudit> audits(int page) {
        return auditRepository.findAllByOrderByCreatedDesc(PageRequest.of(Math.max(page, 0), 20));
    }

    @Transactional(readOnly = true)
    public Page<Member> members(String q, int page) {
        return memberRepository.adminSearch(blankToNull(q), PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional(readOnly = true)
    public Page<TimedAuction> auctions(String q, int page) {
        return auctionRepository.adminSearch(blankToNull(q), PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "created")));
    }

    @Transactional(readOnly = true)
    public TimedAuction auction(String id) {
        return auctionRepository.findById(id).orElseThrow(() -> new ServiceException("경매를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<TimedBid> bids(String auctionId) {
        return bidRepository.findTop30ByAuctionIdOrderByCreatedDescRevisionDesc(auctionId);
    }

    @Transactional(readOnly = true)
    public Page<Product> products(String q, int page) {
        return productRepository.adminSearch(blankToNull(q), PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "created")));
    }

    // ------------------------------------------------------------------ 회원

    /** 이용 정지 ↔ 해제. 관리자 계정과 본인은 정지할 수 없다. */
    @Transactional
    public String toggleSuspend(Long memberId, Member admin) {
        Member m = memberRepository.findById(memberId).orElseThrow(() -> new ServiceException("회원을 찾을 수 없습니다."));
        if (m.getId().equals(admin.getId())) throw new ServiceException("본인 계정은 정지할 수 없습니다.");
        if (!m.isSuspended() && m.isAdmin()) throw new ServiceException("관리자 계정은 정지할 수 없습니다. 먼저 관리자 권한을 해제해 주세요.");
        m.setSuspended(!m.isSuspended());
        String action = m.isSuspended() ? "member.suspend" : "member.restore";
        log(admin, action, String.valueOf(m.getId()), m.getUsername() + " (" + m.getName() + ")");
        return m.isSuspended() ? m.getUsername() + " 회원을 이용 정지했습니다. 이제 로그인할 수 없습니다."
                : m.getUsername() + " 회원의 이용 정지를 해제했습니다.";
    }

    /** 관리자 권한 지정 ↔ 해제. 본인 권한은 바꿀 수 없다(관리자가 0명이 되는 것을 막음). */
    @Transactional
    public String toggleAdmin(Long memberId, Member admin) {
        Member m = memberRepository.findById(memberId).orElseThrow(() -> new ServiceException("회원을 찾을 수 없습니다."));
        if (m.getId().equals(admin.getId())) throw new ServiceException("본인의 관리자 권한은 바꿀 수 없습니다.");
        if (m.isSuspended()) throw new ServiceException("정지된 회원은 관리자로 지정할 수 없습니다.");
        m.setRole(m.isAdmin() ? "USER" : "ADMIN");
        log(admin, m.isAdmin() ? "member.grantAdmin" : "member.revokeAdmin", String.valueOf(m.getId()), m.getUsername() + " (" + m.getName() + ")");
        return m.isAdmin() ? m.getUsername() + " 회원을 관리자로 지정했습니다." : m.getUsername() + " 회원의 관리자 권한을 해제했습니다.";
    }

    // ------------------------------------------------------------------ 경매 · 상품

    /** 경매 숨김(archived) ↔ 복구. 숨긴 경매는 목록·상세·입찰에서 빠지고 데이터는 남는다. */
    @Transactional
    public String toggleArchive(String auctionId, Member admin) {
        TimedAuction a = auction(auctionId);
        a.setArchived(!a.isArchived());
        a.setUpdated(System.currentTimeMillis());
        log(admin, a.isArchived() ? "auction.archive" : "auction.restore", a.getId(), a.getTitle() + " · 판매자 " + a.getSeller());
        return a.isArchived() ? "'" + a.getTitle() + "' 경매를 숨겼습니다. 목록과 상세에서 보이지 않고 입찰도 막힙니다."
                : "'" + a.getTitle() + "' 경매를 다시 공개했습니다.";
    }

    /** 판매 중인 즉시 구매 상품 삭제. 판매 완료된 상품은 거래 기록이라 지우지 않는다. */
    @Transactional
    public String deleteProduct(String productId, Member admin) {
        Product p = productRepository.findById(productId).orElseThrow(() -> new ServiceException("상품을 찾을 수 없습니다."));
        if (p.isSold()) throw new ServiceException("판매 완료된 상품은 거래 기록이라 삭제할 수 없습니다.");
        String title = p.getTitle(), seller = p.getSeller();
        if (productRepository.deleteIfSelling(productId) != 1) {
            throw new ServiceException("그 사이 판매가 완료되어 삭제하지 않았습니다.");
        }
        log(admin, "product.delete", productId, title + " · 판매자 " + seller);
        return "'" + title + "' 상품을 삭제했습니다.";
    }

    private void log(Member admin, String action, String target, String detail) {
        auditRepository.save(new AdminAudit(admin, action, target, detail));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}

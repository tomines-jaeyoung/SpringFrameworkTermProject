package kr.ac.kopo.kim.onairmarket.service;

import kr.ac.kopo.kim.onairmarket.domain.*;
import kr.ac.kopo.kim.onairmarket.dto.AuctionForm;
import kr.ac.kopo.kim.onairmarket.exception.BidConflictException;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.repository.BookmarkRepository;
import kr.ac.kopo.kim.onairmarket.repository.TimedAuctionRepository;
import kr.ac.kopo.kim.onairmarket.repository.TimedBidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/** 사진·동영상 기간 경매: 등록, 목록, 상세, 입찰, 관심 저장 */
@Service
@RequiredArgsConstructor
public class AuctionService {
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final long HOUR = 3_600_000L;
    private static final long DAY = 86_400_000L;

    private final TimedAuctionRepository auctionRepository;
    private final TimedBidRepository bidRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MediaService mediaService;

    // ------------------------------------------------------------------ 등록

    /**
     * 판매 등록. 입력값 검사 → 사진(필수)·영상(선택) 저장 → 경매 저장.
     * 파일 저장 후 경매 저장이 실패하면 저장한 파일을 정리한다.
     */
    public TimedAuction create(AuctionForm form, MultipartFile image, MultipartFile video, Member owner) {
        long now = System.currentTimeMillis();
        Category category = Category.find(form.getCategoryId())
                .orElseThrow(() -> new ServiceException("카테고리를 선택해 주세요."));
        long[] period = period(form, now);
        if (image == null || image.isEmpty()) throw new ServiceException("대표 사진을 올려 주세요.");

        AuctionMedia img = null, vid = null;
        try {
            img = mediaService.store(image, "image", owner);
            if (video != null && !video.isEmpty()) vid = mediaService.store(video, "video", owner);

            TimedAuction a = new TimedAuction();
            a.setId(UUID.randomUUID().toString());
            a.setOwner(owner);
            a.setSeller(owner.getName());
            a.setTitle(form.getTitle().trim());
            a.setDescription(form.getDescription().trim());
            a.setCategoryId(category.id());
            a.setCategory(category.label());
            a.setImage("/api/media/" + img.getId());
            a.setVideo(vid == null ? "" : "/api/media/" + vid.getId());
            a.setStartPrice(form.getStartPrice());
            a.setPrice(form.getStartPrice());
            a.setStartsAt(period[0]);
            a.setEndsAt(period[1]);
            a.setCreated(now);
            a.setUpdated(now);
            return auctionRepository.save(a);
        } catch (RuntimeException e) {
            mediaService.discard(img);
            mediaService.discard(vid);
            throw e;
        }
    }

    /** 시작·종료 시각 검사 (기존 온에어마켓 validateTimedCreate 와 같은 범위) */
    static long[] period(AuctionForm f, long now) {
        long start = now;
        if ("scheduled".equals(f.getStartMode())) {
            if (f.getStartsAt() == null) throw new ServiceException("시작 시각을 입력해 주세요.");
            long submitted = f.getStartsAt().atZone(KST).toInstant().toEpochMilli();
            if (submitted < now - 60_000 || submitted > now + 365 * DAY) {
                throw new ServiceException("시작 시각은 현재부터 1년 이내로 설정해 주세요.");
            }
            start = Math.max(now, submitted);
        }
        long end;
        switch (f.getDuration() == null ? "" : f.getDuration()) {
            case "7" -> end = start + 7 * DAY;
            case "30" -> end = start + 30 * DAY;
            case "90" -> end = start + 90 * DAY;
            case "custom" -> {
                if (f.getEndsAt() == null) throw new ServiceException("종료 시각을 입력해 주세요.");
                end = f.getEndsAt().atZone(KST).toInstant().toEpochMilli();
            }
            default -> throw new ServiceException("경매 기간을 선택해 주세요.");
        }
        long duration = end - start;
        if (duration < HOUR || duration > 365 * DAY) {
            throw new ServiceException("경매 기간은 1시간 이상, 365일 이내로 설정해 주세요.");
        }
        return new long[]{start, end};
    }

    // ------------------------------------------------------------------ 조회

    @Transactional(readOnly = true)
    public Page<TimedAuction> list(String categoryId, String keyword, String sort, boolean openOnly, int page) {
        Sort order = switch (sort == null ? "" : sort) {
            case "ending" -> Sort.by("endsAt").ascending();
            case "price" -> Sort.by("price").descending();
            case "bids" -> Sort.by("bidCount").descending();
            default -> Sort.by("created").descending().and(Sort.by("id").descending());
        };
        String cat = (categoryId == null || categoryId.isBlank()) ? null : categoryId;
        String q = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return auctionRepository.search(cat, q, openOnly, System.currentTimeMillis(), PageRequest.of(Math.max(page, 0), 12, order));
    }

    @Transactional(readOnly = true)
    public TimedAuction get(String id) {
        TimedAuction a = auctionRepository.findById(id).orElseThrow(() -> new ServiceException("경매를 찾을 수 없습니다."));
        if (a.isArchived()) throw new ServiceException("보관 처리된 경매입니다.");
        a.getOwner().getId();   // 지연 로딩 초기화 (owner 비교용)
        return a;
    }

    @Transactional(readOnly = true)
    public List<TimedBid> history(String auctionId) {
        return bidRepository.findTop30ByAuctionIdOrderByCreatedDescRevisionDesc(auctionId);
    }

    @Transactional(readOnly = true)
    public List<TimedAuction> mySales(Member m) {
        return auctionRepository.findByOwnerIdAndArchivedFalseOrderByCreatedDesc(m.getId());
    }

    @Transactional(readOnly = true)
    public List<TimedBid> myBids(Member m) {
        return bidRepository.findMyBids(m.getId());
    }

    // ------------------------------------------------------------------ 입찰

    /**
     * 입찰: 금액은 반드시 현재가 + 1,000원, 화면이 본 revision 과 서버 revision 이 같아야 한다.
     * 조건부 UPDATE 로 한 건만 성공시키고, 실패하면 충돌로 처리해 최신 상태를 다시 보게 한다.
     */
    @Transactional
    public void bid(String auctionId, long amount, long revision, Member bidder) {
        long now = System.currentTimeMillis();
        TimedAuction a = auctionRepository.findById(auctionId).orElseThrow(() -> new ServiceException("경매를 찾을 수 없습니다."));
        if (a.isArchived()) throw new ServiceException("보관 처리된 경매입니다.");
        if (a.getOwner().getId().equals(bidder.getId())) throw new ServiceException("본인 경매에는 입찰할 수 없습니다.");
        if (now < a.getStartsAt()) throw new ServiceException("아직 시작 전인 경매입니다.");
        if (now >= a.getEndsAt()) throw new ServiceException("이미 종료된 경매입니다.");
        if (amount != a.getPrice() + 1000 || revision != a.getRevision()) throw new BidConflictException();

        int changed = auctionRepository.applyBid(auctionId, bidder.getId(), revision, a.getPrice(), amount, bidder.getName(), now);
        if (changed != 1) throw new BidConflictException();

        TimedBid b = new TimedBid();
        b.setId(UUID.randomUUID().toString());
        b.setAuction(auctionRepository.getReferenceById(auctionId));
        b.setUser(bidder);
        b.setName(bidder.getName());
        b.setAmount(amount);
        b.setCreated(now);
        b.setRevision(revision + 1);
        try {
            bidRepository.saveAndFlush(b);
        } catch (DataIntegrityViolationException e) {   // UNIQUE (auction, revision)
            throw new BidConflictException();
        }
    }

    // ------------------------------------------------------------------ 관심 저장

    @Transactional(readOnly = true)
    public boolean isBookmarked(Member m, String auctionId) {
        return m != null && bookmarkRepository.existsById(new BookmarkId(m.getId(), auctionId));
    }

    /** 저장되어 있으면 해제, 없으면 저장. 반환값: 저장 후 상태 */
    @Transactional
    public boolean toggleBookmark(Member m, String auctionId) {
        BookmarkId key = new BookmarkId(m.getId(), auctionId);
        if (bookmarkRepository.existsById(key)) {
            bookmarkRepository.deleteById(key);
            return false;
        }
        TimedAuction a = get(auctionId);
        bookmarkRepository.save(new Bookmark(m, a));
        return true;
    }

    @Transactional(readOnly = true)
    public List<Bookmark> myBookmarks(Member m) {
        return bookmarkRepository.findMine(m.getId());
    }
}

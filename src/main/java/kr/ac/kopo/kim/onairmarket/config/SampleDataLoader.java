package kr.ac.kopo.kim.onairmarket.config;

import kr.ac.kopo.kim.onairmarket.domain.Category;
import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.domain.Product;
import kr.ac.kopo.kim.onairmarket.domain.TimedAuction;
import kr.ac.kopo.kim.onairmarket.domain.TimedBid;
import kr.ac.kopo.kim.onairmarket.repository.MemberRepository;
import kr.ac.kopo.kim.onairmarket.repository.ProductRepository;
import kr.ac.kopo.kim.onairmarket.repository.TimedAuctionRepository;
import kr.ac.kopo.kim.onairmarket.repository.TimedBidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 처음 실행할 때(회원이 한 명도 없을 때)만 체험용 회원 2명과 샘플 경매·즉시 구매 상품을 넣는다.
 * 사진·영상은 기존 온에어마켓 public/media 에서 복사한 파일(/sample/...)을 사용한다.
 * 체험 계정 아이디·비밀번호는 README.md 에 적어 두었다.
 */
@Component
@RequiredArgsConstructor
public class SampleDataLoader implements ApplicationRunner {
    private static final long HOUR = 3_600_000L, DAY = 86_400_000L;

    private final MemberRepository memberRepository;
    private final TimedAuctionRepository auctionRepository;
    private final TimedBidRepository bidRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (memberRepository.count() > 0) {
            // 이미 회원이 있는 DB(이전 버전으로 실행한 DB)에는 즉시 구매 샘플만 없으면 넣는다
            if (productRepository.count() == 0) {
                memberRepository.findByUsername("onair_seller").ifPresent(seller ->
                        seedProducts(seller, memberRepository.findByUsername("onair_buyer").orElse(null), System.currentTimeMillis()));
            }
            return;
        }
        Member seller = memberRepository.save(new Member("onair_seller", passwordEncoder.encode("onair1234"), "온에어셀렉트"));
        Member buyer = memberRepository.save(new Member("onair_buyer", passwordEncoder.encode("onair1234"), "수집가민지"));
        long now = System.currentTimeMillis();

        TimedAuction a1 = add(seller, "코랄 러닝 스니커즈 270mm", "sneakers", "hd-product-sneakers",
                "/sample/showcase-fashion.mp4", 59_000, now - 2 * DAY, now + 5 * DAY,
                "두 번 착용한 러닝 스니커즈입니다. 밑창 마모가 거의 없고 박스와 여분 끈을 함께 보내 드립니다. 영상에서 착화감과 측면 상태를 확인해 주세요.");
        add(seller, "휴대용 레트로 게임기 풀박스", "games", "hd-product-console", "", 120_000, now - DAY, now + 2 * HOUR,
                "전원·버튼·화면 모두 정상 작동합니다. 충전 케이블과 보관 파우치를 함께 드립니다. 마감이 가까운 경매입니다.");
        add(seller, "빈티지 워크 재킷 L", "men", "hd-product-jacket", "", 45_000, now - 3 * DAY, now + 7 * DAY,
                "90년대 워크 재킷입니다. 안감 상태가 좋고 단추 교체 이력이 있습니다. 실측은 상세 사진을 참고해 주세요.");
        add(seller, "스포츠 다이버 워치", "watches", "hd-product-sportwatch", "", 180_000, now - 5 * HOUR, now + 30 * DAY,
                "정상 작동하는 다이버 워치입니다. 생활 스크래치가 조금 있고 정품 보증서는 없습니다.");
        add(seller, "필름 카메라 + 35mm 렌즈", "electronics", "hd-product-film", "", 150_000, now + DAY, now + 8 * DAY,
                "셔터·노출계 정상입니다. 내일부터 시작하는 예약 경매입니다.");
        add(seller, "핸드메이드 테디베어", "toys", "hd-product-bear", "", 25_000, now - 10 * DAY, now - HOUR,
                "직접 만든 테디베어입니다. 이미 종료된 경매 예시입니다.");
        add(seller, "트레이딩 카드 미개봉 박스", "tcg", "hd-product-cardbox", "", 90_000, now - DAY, now + 3 * DAY,
                "미개봉 박스입니다. 비닐 포장 상태를 영상과 사진으로 확인해 주세요.");
        add(seller, "포켓몬 카드 슬리브 세트", "pokemon", "hd-pokemon-card-back", "", 12_000, now - DAY, now + 14 * DAY,
                "카드 보호용 슬리브와 보관 박스 세트입니다.");
        // 상품 소개 영상이 있는 경매 (홈 '영상으로 만나는 경매')
        add(seller, "빈티지 데님 셀렉트 3종", "vintage", "/media/hd-live-3-640.webp", "/media/showcase-boutique.mp4",
                35_000, now - 6 * HOUR, now + 4 * DAY,
                "90년대 데님 재킷과 셔츠 3종 묶음입니다. 영상에서 원단 질감과 워싱을 확인해 주세요.");
        add(seller, "스트릿 패션 셀렉트 후드", "women", "/media/hd-live-7-640.webp", "/media/showcase-fashion.mp4",
                28_000, now - DAY, now + 6 * DAY,
                "오버핏 후드와 볼캡 세트입니다. 착용 영상으로 핏을 확인할 수 있어요.");
        // 시작 예정 경매 (홈 '곧 시작하는 경매')
        add(seller, "LP 레코드 컬렉션 10장", "music", "/media/hd-live-8-640.webp", "", 70_000, now + 2 * DAY, now + 9 * DAY,
                "재즈·팝 LP 10장 묶음입니다. 모레 시작하는 예약 경매입니다.");
        add(seller, "아트토이 베어 피규어 400%", "toys", "/media/hd-live-9-640.webp", "", 95_000, now + 3 * DAY, now + 10 * DAY,
                "박스 포함 아트토이 피규어입니다. 사흘 뒤 시작하는 예약 경매입니다.");

        // 입찰 기록 예시 (수집가민지가 첫 번째 경매에 두 번 입찰)
        for (int i = 0; i < 2; i++) {
            a1.setPrice(a1.getPrice() + 1000);
            a1.setRevision(a1.getRevision() + 1);
            a1.setBidCount(a1.getBidCount() + 1);
            a1.setLeader(buyer.getName());
            TimedBid b = new TimedBid();
            b.setId(UUID.randomUUID().toString());
            b.setAuction(a1);
            b.setUser(buyer);
            b.setName(buyer.getName());
            b.setAmount(a1.getPrice());
            b.setCreated(now - (2 - i) * HOUR);
            b.setRevision(a1.getRevision());
            bidRepository.save(b);
        }
        seedProducts(seller, buyer, now);
    }

    /** 즉시 구매 샘플 (중고 거래처럼 정해진 가격으로 바로 사는 예시 상품) */
    private void seedProducts(Member seller, Member buyer, long now) {
        product(seller, "나이키 에어 조던 1 시카고 (2015, 중고)", "sneakers", "hd-product-sneakers", 1_590_000, now - 3 * HOUR,
                "2015년 발매 시카고 컬러입니다. 착용감 있으나 갑피 상태가 좋습니다. 박스 포함.");
        product(seller, "닌텐도 게임보이 컬러 (클리어 퍼플)", "games", "hd-product-console", 89_000, now - 5 * HOUR,
                "화면·버튼 정상 작동합니다. 배터리 커버 포함, 생활 기스 조금 있습니다.");
        product(seller, "포켓몬 카드 VMAX 클라이맥스 박스 (미개봉)", "pokemon", "hd-product-cardbox", 159_000, now - DAY,
                "미개봉 박스입니다. 비닐 포장 상태 양호합니다.");
        product(seller, "코닥 포트라 800 필름 (미개봉)", "electronics", "hd-product-film", 65_000, now - 2 * DAY,
                "유통기한 내 미개봉 필름 3롤 묶음입니다.");
        Product sold = product(seller, "스포츠 다이버 워치 스트랩 세트", "watches", "hd-product-sportwatch", 32_000, now - 4 * DAY,
                "러버 스트랩 2종 세트입니다. 판매 완료 예시 상품입니다.");
        if (buyer != null) {
            sold.setStatus(Product.SOLD);
            sold.setBuyer(buyer);
            sold.setBuyerName(buyer.getName());
            sold.setSoldAt(now - 2 * DAY);
            productRepository.save(sold);
        }
    }

    private Product product(Member owner, String title, String categoryId, String image, long price, long created, String description) {
        Product p = new Product();
        p.setId(UUID.randomUUID().toString());
        p.setOwner(owner);
        p.setSeller(owner.getName());
        p.setTitle(title);
        p.setDescription(description);
        p.setCategoryId(categoryId);
        p.setCategory(Category.find(categoryId).orElseThrow().label());
        p.setImage("/sample/" + image + "-640.webp");
        p.setPrice(price);
        p.setCreated(created);
        p.setUpdated(created);
        return productRepository.save(p);
    }

    private TimedAuction add(Member owner, String title, String categoryId, String image, String video,
                             long startPrice, long startsAt, long endsAt, String description) {
        TimedAuction a = new TimedAuction();
        a.setId(UUID.randomUUID().toString());
        a.setOwner(owner);
        a.setSeller(owner.getName());
        a.setTitle(title);
        a.setDescription(description);
        a.setCategoryId(categoryId);
        a.setCategory(Category.find(categoryId).orElseThrow().label());
        a.setImage(image.startsWith("/") ? image : "/sample/" + image + "-640.webp");
        a.setVideo(video);
        a.setStartPrice(startPrice);
        a.setPrice(startPrice);
        a.setStartsAt(startsAt);
        a.setEndsAt(endsAt);
        a.setCreated(Math.min(startsAt, System.currentTimeMillis()));
        a.setUpdated(a.getCreated());
        return auctionRepository.save(a);
    }
}

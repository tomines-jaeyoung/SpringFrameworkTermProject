package kr.ac.kopo.kim.onairmarket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사진·동영상 기간 경매 (timed_auctions).
 * 가격은 원화 정수, 시각은 밀리초 타임스탬프로 저장한다.
 * 설계 보고서처럼 SQL 외래 키 제약은 만들지 않고(논리 참조), 소유자 검사는 서비스에서 한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "timed_auctions", indexes = {
        @Index(name = "timed_auction_feed", columnList = "archived, created"),
        @Index(name = "timed_auction_owner", columnList = "owner_id, archived, ends_at")})
public class TimedAuction {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member owner;

    @Column(nullable = false, length = 20)
    private String seller;            // 등록 당시 판매자 표시 이름

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false, length = 40)
    private String categoryId;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 500)
    private String image;             // /api/media/{파일ID} 또는 /sample/...

    @Column(nullable = false, length = 500)
    private String video = "";        // 선택 항목

    @Column(nullable = false)
    private Long startPrice;

    @Column(nullable = false)
    private Long price;               // 현재가

    @Column(nullable = false)
    private Long startsAt;

    @Column(nullable = false)
    private Long endsAt;

    @Column(nullable = false)
    private Long revision = 0L;       // 갱신 번호 (입찰마다 +1)

    @Column(nullable = false)
    private Integer bidCount = 0;

    @Column(length = 20)
    private String leader;            // 최고 입찰자 표시 이름 (선택)

    @Column(nullable = false)
    private Long created;

    @Column(nullable = false)
    private Long updated;

    @Column(nullable = false)
    private boolean archived = false;

    public String status(long now) {
        if (archived) return "보관됨";
        if (now < startsAt) return "시작 전";
        if (now >= endsAt) return "종료";
        return "진행 중";
    }

    public boolean isOpen(long now) {
        return !archived && startsAt <= now && now < endsAt;
    }

    /** 다음 입찰 금액 = 현재가 + 1,000원 */
    public long nextBid() {
        return price + 1000;
    }
}

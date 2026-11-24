package kr.ac.kopo.kim.onairmarket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 관심 저장 (bookmarks): 회원 ↔ 경매 N:M. 복합 기본 키로 같은 경매의 중복 저장을 막는다. */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "bookmarks")
public class Bookmark {
    @EmbeddedId
    private BookmarkId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member user;

    @MapsId("auctionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TimedAuction auction;

    @Column(nullable = false)
    private Long created;

    public Bookmark(Member user, TimedAuction auction) {
        this.id = new BookmarkId(user.getId(), auction.getId());
        this.user = user;
        this.auction = auction;
        this.created = System.currentTimeMillis();
    }
}

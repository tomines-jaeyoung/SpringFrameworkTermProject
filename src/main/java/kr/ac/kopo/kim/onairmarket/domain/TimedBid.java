package kr.ac.kopo.kim.onairmarket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 기간 경매 입찰 기록 (timed_bids). UNIQUE (auction, revision)로 같은 갱신 번호의 중복 기록을 막는다. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "timed_bids",
        uniqueConstraints = @UniqueConstraint(name = "uk_timed_bids_auction_revision", columnNames = {"auction_id", "revision"}),
        indexes = @Index(name = "timed_bid_history", columnList = "auction_id, created"))
public class TimedBid {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TimedAuction auction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member user;

    @Column(nullable = false, length = 20)
    private String name;              // 입찰 당시 표시 이름

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long created;

    @Column(nullable = false)
    private Long revision;
}

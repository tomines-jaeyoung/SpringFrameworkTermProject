package kr.ac.kopo.kim.onairmarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** bookmarks 의 복합 기본 키 (user, auction) */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class BookmarkId implements Serializable {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "auction_id", length = 36)
    private String auctionId;
}

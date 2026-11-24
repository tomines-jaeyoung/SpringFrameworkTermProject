package kr.ac.kopo.kim.onairmarket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 업로드 파일 메타데이터 (auction_media).
 * 파일 본문은 업로드 폴더의 objectKey 위치에 저장하고 /api/media/{id} 로 읽는다.
 * status 는 pending → ready 로 바뀌며, 저장에 실패하면 파일과 행을 함께 정리한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "auction_media")
public class AuctionMedia {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member owner;

    @Column(name = "object_key", nullable = false, length = 200)
    private String objectKey;

    @Column(nullable = false, length = 10)
    private String kind;              // image | video

    @Column(nullable = false, length = 40)
    private String mime;

    @Column(nullable = false)
    private Long bytes;

    @Column(nullable = false, length = 10)
    private String status;            // pending | ready

    @Column(nullable = false)
    private Long created;
}

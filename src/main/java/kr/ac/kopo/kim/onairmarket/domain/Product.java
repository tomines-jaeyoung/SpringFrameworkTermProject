package kr.ac.kopo.kim.onairmarket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 즉시 구매 상품 (products): 중고 거래처럼 정해진 가격으로 바로 사는 실물 상품.
 * '구매하기'를 누른 첫 회원에게 판매 완료(sold)로 바뀐다. 실제 결제는 없다.
 * 기간 경매와 같이 SQL 외래 키 제약은 만들지 않는다(논리 참조).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products", indexes = {
        @Index(name = "product_feed", columnList = "status, created"),
        @Index(name = "product_owner", columnList = "owner_id, created"),
        @Index(name = "product_buyer", columnList = "buyer_id, sold_at")})
public class Product {
    public static final String SELLING = "selling";
    public static final String SOLD = "sold";

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

    @Column(nullable = false)
    private Long price;               // 판매 가격 (원)

    @Column(nullable = false, length = 10)
    private String status = SELLING;  // selling: 판매 중, sold: 판매 완료

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member buyer;             // 판매 완료 시 구매자

    @Column(length = 20)
    private String buyerName;

    private Long soldAt;

    @Column(nullable = false)
    private Long created;

    @Column(nullable = false)
    private Long updated;

    public boolean isSold() {
        return SOLD.equals(status);
    }

    public String statusLabel() {
        return isSold() ? "판매 완료" : "판매 중";
    }
}

package kr.ac.kopo.kim.onairmarket.repository;

import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    /** 즉시 구매 목록: 카테고리(선택)·검색어(선택), 판매 중만 보기(선택) */
    @Query("""
            select p from Product p
            where (:categoryId is null or p.categoryId = :categoryId)
              and (:keyword is null or lower(p.title) like lower(concat('%', :keyword, '%')))
              and (:sellingOnly = false or p.status = 'selling')
            """)
    Page<Product> search(@Param("categoryId") String categoryId,
                         @Param("keyword") String keyword,
                         @Param("sellingOnly") boolean sellingOnly,
                         Pageable pageable);

    /** 홈 '즉시 구매': 판매 중인 최근 상품 4개 */
    List<Product> findTop4ByStatusOrderByCreatedDesc(String status);

    List<Product> findByOwnerIdOrderByCreatedDesc(Long ownerId);

    /** 관리자: 상품명·판매자 검색 */
    @Query("""
            select p from Product p
            where :keyword is null or lower(p.title) like lower(concat('%', :keyword, '%'))
               or lower(p.seller) like lower(concat('%', :keyword, '%'))
            """)
    Page<Product> adminSearch(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(String status);

    /** 관리자 삭제: 판매 중인 상품만 지운다 (판매 완료된 거래 기록은 남긴다). 동시에 구매되면 0 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Product p where p.id = :id and p.status = 'selling'")
    int deleteIfSelling(@Param("id") String id);

    List<Product> findByBuyerIdOrderBySoldAtDesc(Long buyerId);

    /**
     * 조건부 갱신 구매. 아직 판매 중이고 본인 상품이 아닐 때만 1행이 바뀐다.
     * 두 사람이 동시에 눌러도 먼저 반영된 한 명만 구매에 성공한다(0이면 이미 판매됨).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Product p
               set p.status = 'sold', p.buyer = :buyer, p.buyerName = :buyerName,
                   p.soldAt = :now, p.updated = :now
             where p.id = :id and p.status = 'selling' and p.owner <> :buyer
            """)
    int markSold(@Param("id") String id, @Param("buyer") Member buyer,
                 @Param("buyerName") String buyerName, @Param("now") long now);
}

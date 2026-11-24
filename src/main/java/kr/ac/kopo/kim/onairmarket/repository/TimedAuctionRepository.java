package kr.ac.kopo.kim.onairmarket.repository;

import kr.ac.kopo.kim.onairmarket.domain.TimedAuction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimedAuctionRepository extends JpaRepository<TimedAuction, String> {

    /** 목록 탐색: 카테고리(선택)·검색어(선택), 보관되지 않은 경매만 */
    @Query("""
            select a from TimedAuction a
            where a.archived = false
              and (:categoryId is null or a.categoryId = :categoryId)
              and (:keyword is null or lower(a.title) like lower(concat('%', :keyword, '%')))
              and (:openOnly = false or (a.startsAt <= :now and a.endsAt > :now))
            """)
    Page<TimedAuction> search(@Param("categoryId") String categoryId,
                              @Param("keyword") String keyword,
                              @Param("openOnly") boolean openOnly,
                              @Param("now") long now,
                              Pageable pageable);

    List<TimedAuction> findByOwnerIdAndArchivedFalseOrderByCreatedDesc(Long ownerId);

    /** 홈 '곧 낙찰됩니다': 진행 중인 경매를 마감이 가까운 순으로 4개 */
    List<TimedAuction> findTop4ByArchivedFalseAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(long startedBy, long endsAfter);

    /** 홈 '사진으로 보고, 경매에 참여하세요': 최근 등록 경매 8개 */
    List<TimedAuction> findTop8ByArchivedFalseOrderByCreatedDesc();

    /**
     * 조건부 갱신 입찰. 서버가 확인한 revision·현재가가 그대로이고, 본인 경매가 아니며,
     * 입찰 기간 안일 때만 1행이 바뀐다. 0이면 다른 입찰이 먼저 반영된 충돌이다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update TimedAuction a
               set a.price = :amount, a.leader = :leader, a.revision = a.revision + 1,
                   a.bidCount = a.bidCount + 1, a.updated = :now
             where a.id = :id and a.owner.id <> :bidderId and a.revision = :revision
               and a.price = :expectedPrice and a.startsAt <= :now and a.endsAt > :now and a.archived = false
            """)
    int applyBid(@Param("id") String id, @Param("bidderId") Long bidderId, @Param("revision") long revision,
                 @Param("expectedPrice") long expectedPrice, @Param("amount") long amount,
                 @Param("leader") String leader, @Param("now") long now);
}

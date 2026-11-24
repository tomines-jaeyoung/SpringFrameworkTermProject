package kr.ac.kopo.kim.onairmarket.repository;

import kr.ac.kopo.kim.onairmarket.domain.TimedBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimedBidRepository extends JpaRepository<TimedBid, String> {
    /** 상세 화면 입찰 기록 (최근 30건) */
    List<TimedBid> findTop30ByAuctionIdOrderByCreatedDescRevisionDesc(String auctionId);

    /** 내가 입찰한 경매 기록 */
    @Query("select b from TimedBid b join fetch b.auction where b.user.id = :userId order by b.created desc")
    List<TimedBid> findMyBids(@Param("userId") Long userId);
}

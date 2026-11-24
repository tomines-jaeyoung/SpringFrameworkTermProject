package kr.ac.kopo.kim.onairmarket.repository;

import kr.ac.kopo.kim.onairmarket.domain.AuctionMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuctionMediaRepository extends JpaRepository<AuctionMedia, String> {
    Optional<AuctionMedia> findByIdAndStatus(String id, String status);

    long countByOwnerId(Long ownerId);

    @Query("select coalesce(sum(m.bytes), 0) from AuctionMedia m where m.owner.id = :ownerId")
    long sumBytesByOwner(@Param("ownerId") Long ownerId);
}

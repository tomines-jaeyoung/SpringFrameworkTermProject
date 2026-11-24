package kr.ac.kopo.kim.onairmarket.repository;

import kr.ac.kopo.kim.onairmarket.domain.Bookmark;
import kr.ac.kopo.kim.onairmarket.domain.BookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {
    @Query("select b from Bookmark b join fetch b.auction where b.user.id = :userId order by b.created desc")
    List<Bookmark> findMine(@Param("userId") Long userId);

    long countByUserId(Long userId);
}

package kr.ac.kopo.kim.onairmarket.repository;

import kr.ac.kopo.kim.onairmarket.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    /** 관리자: 아이디·이름 검색 (검색어 없으면 전체) */
    @Query("""
            select m from Member m
            where :keyword is null or lower(m.username) like lower(concat('%', :keyword, '%'))
               or lower(m.name) like lower(concat('%', :keyword, '%'))
            """)
    Page<Member> adminSearch(@Param("keyword") String keyword, Pageable pageable);

    long countBySuspendedTrue();
}

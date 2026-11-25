package kr.ac.kopo.kim.onairmarket.repository;

import kr.ac.kopo.kim.onairmarket.domain.AdminAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminAuditRepository extends JpaRepository<AdminAudit, String> {
    List<AdminAudit> findTop8ByOrderByCreatedDesc();

    Page<AdminAudit> findAllByOrderByCreatedDesc(Pageable pageable);
}

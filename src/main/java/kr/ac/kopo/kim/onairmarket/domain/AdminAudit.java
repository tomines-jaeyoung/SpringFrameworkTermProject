package kr.ac.kopo.kim.onairmarket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * 관리 이력 (admin_audit). 원 설계의 admin_audit 와 같은 구성:
 * 누가(actor) 어떤 작업(action)을 어떤 대상(target)에 했는지와 상세 내용(detail), 시각(created)을 남긴다.
 * target 은 회원·경매·상품 ID 를 담는 문자열이라 여러 종류의 대상을 한 테이블에 기록한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "admin_audit", indexes = @Index(name = "admin_audit_recent", columnList = "created"))
public class AdminAudit {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member actor;

    @Column(nullable = false, length = 20)
    private String actorName;         // 작업 당시 관리자 아이디

    @Column(nullable = false, length = 40)
    private String action;            // 예: member.suspend, auction.archive, product.delete

    @Column(nullable = false, length = 60)
    private String target;            // 대상 ID (회원 번호·경매 ID·상품 ID)

    @Column(nullable = false, length = 300)
    private String detail;            // 화면에 보여 줄 설명

    @Column(nullable = false)
    private Long created;

    public AdminAudit(Member actor, String action, String target, String detail) {
        this.id = UUID.randomUUID().toString();
        this.actor = actor;
        this.actorName = actor.getUsername();
        this.action = action;
        this.target = target;
        this.detail = detail.length() > 300 ? detail.substring(0, 300) : detail;
        this.created = System.currentTimeMillis();
    }

    public String actionLabel() {
        return switch (action) {
            case "member.suspend" -> "회원 정지";
            case "member.restore" -> "정지 해제";
            case "member.grantAdmin" -> "관리자 지정";
            case "member.revokeAdmin" -> "관리자 해제";
            case "auction.archive" -> "경매 숨김";
            case "auction.restore" -> "경매 복구";
            case "product.delete" -> "상품 삭제";
            default -> action;
        };
    }
}

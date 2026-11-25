package kr.ac.kopo.kim.onairmarket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 (members).
 * 설계 보고서의 "외부 인증 사용자(userId)"를 과제 조건(회원가입·로그인 DB 연동)에 맞춰 로컬 테이블로 구현했다.
 * 다른 테이블의 owner / user 가 이 회원을 가리킨다.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "members", uniqueConstraints = @UniqueConstraint(name = "uk_members_username", columnNames = "username"))
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String username;          // 로그인 아이디

    @Column(nullable = false, length = 100)
    private String password;          // BCrypt 암호문 (평문 저장 안 함)

    @Column(nullable = false, length = 20)
    private String name;              // 화면 표시 이름 (판매자·입찰자명)

    @Column(nullable = false, length = 20)
    private String role = "USER";     // USER 또는 ADMIN

    @Column(nullable = false)
    private boolean suspended = false; // 관리자가 이용 정지한 회원은 로그인할 수 없다

    @Column(nullable = false)
    private Long created;             // 밀리초 타임스탬프

    public Member(String username, String encodedPassword, String name) {
        this.username = username;
        this.password = encodedPassword;
        this.name = name;
        this.created = System.currentTimeMillis();
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}

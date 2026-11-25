package kr.ac.kopo.kim.onairmarket.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;

/**
 * 이미 로그인해 있는 동안 상태가 바뀐 회원 처리.
 * - 관리자가 이용 정지하면: 다음 요청에서 바로 로그아웃시키고 로그인 화면으로 보낸다.
 * - 관리자 권한이 해제되면: /admin 화면을 403으로 막는다 (세션에 남은 예전 권한을 믿지 않음).
 */
@Component
@RequiredArgsConstructor
public class MemberStateInterceptor implements HandlerInterceptor {
    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Principal principal = request.getUserPrincipal();
        if (principal == null) return true;
        Member m = memberRepository.findByUsername(principal.getName()).orElse(null);
        if (m == null || m.isSuspended()) {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?suspended");
            return false;
        }
        if (request.getRequestURI().startsWith(request.getContextPath() + "/admin") && !m.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}

package kr.ac.kopo.kim.onairmarket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 화면 요청마다 회원 상태(정지·관리자 권한)를 DB 기준으로 다시 확인 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final MemberStateInterceptor memberStateInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(memberStateInterceptor)
                .excludePathPatterns("/css/**", "/js/**", "/media/**", "/sample/**", "/api/media/**", "/favicon.*", "/error", "/login", "/logout");
    }
}

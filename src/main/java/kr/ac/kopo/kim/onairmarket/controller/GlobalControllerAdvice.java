package kr.ac.kopo.kim.onairmarket.controller;

import kr.ac.kopo.kim.onairmarket.domain.Category;
import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/** 모든 화면 공통: 로그인한 회원(me), 카테고리 목록, 업로드 용량 초과 처리 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final MemberRepository memberRepository;

    @ModelAttribute("me")
    public Member me(Principal principal) {
        return principal == null ? null : memberRepository.findByUsername(principal.getName()).orElse(null);
    }

    @ModelAttribute("categories")
    public List<Category> categories() {
        return Category.ALL;
    }

    /** 요청 전체가 60MB(파일 하나 50MB)를 넘은 경우 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooLarge(RedirectAttributes ra) {
        ra.addFlashAttribute("error", "파일이 너무 큽니다. 영상은 50MB, 사진은 8MB까지 올릴 수 있습니다.");
        return "redirect:/sell/timed";
    }
}

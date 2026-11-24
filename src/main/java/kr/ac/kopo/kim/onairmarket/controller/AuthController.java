package kr.ac.kopo.kim.onairmarket.controller;

import jakarta.validation.Valid;
import kr.ac.kopo.kim.onairmarket.dto.SignupForm;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 로그인 화면과 회원가입 (로그인 처리 자체는 Spring Security 가 담당) */
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm(@ModelAttribute("form") SignupForm form) {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("form") SignupForm form, BindingResult binding, RedirectAttributes ra) {
        if (binding.hasErrors()) return "signup";
        try {
            memberService.signup(form);
        } catch (ServiceException e) {
            binding.reject("signup", e.getMessage());
            return "signup";
        }
        ra.addFlashAttribute("notice", "회원가입이 완료되었습니다. 로그인해 주세요.");
        ra.addFlashAttribute("joinedUsername", form.getUsername());
        return "redirect:/login";
    }
}

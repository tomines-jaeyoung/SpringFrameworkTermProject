package kr.ac.kopo.kim.onairmarket.controller;

import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.service.AdminService;
import kr.ac.kopo.kim.onairmarket.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.function.Supplier;

/**
 * 관리자 화면 (/admin/**). SecurityConfig 에서 ROLE_ADMIN 만 들어올 수 있게 막는다.
 * 대시보드 · 회원 · 경매 · 즉시 구매 상품 · 관리 이력
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final MemberService memberService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.stats());
        model.addAttribute("audits", adminService.recentAudits());
        model.addAttribute("tab", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/members")
    public String members(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", adminService.members(q, page));
        model.addAttribute("q", q);
        model.addAttribute("tab", "members");
        return "admin/members";
    }

    @GetMapping("/auctions")
    public String auctions(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", adminService.auctions(q, page));
        model.addAttribute("q", q);
        model.addAttribute("tab", "auctions");
        return "admin/auctions";
    }

    @GetMapping("/auctions/{id}")
    public String auction(@PathVariable String id, Model model, HttpServletResponse response) {
        try {
            model.addAttribute("a", adminService.auction(id));
        } catch (ServiceException e) {
            model.addAttribute("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "not-found";
        }
        model.addAttribute("bids", adminService.bids(id));
        model.addAttribute("tab", "auctions");
        return "admin/auction";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", adminService.products(q, page));
        model.addAttribute("q", q);
        model.addAttribute("tab", "products");
        return "admin/products";
    }

    @GetMapping("/audit")
    public String audit(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", adminService.audits(page));
        model.addAttribute("tab", "audit");
        return "admin/audit";
    }

    // ------------------------------------------------------------------ 변경 (POST + CSRF)

    @PostMapping("/members/{id}/suspend")
    public String suspend(@PathVariable Long id, @RequestParam(required = false) String back, Principal principal, RedirectAttributes ra) {
        Member admin = memberService.get(principal.getName());
        return run(() -> adminService.toggleSuspend(id, admin), back, "/admin/members", ra);
    }

    @PostMapping("/members/{id}/role")
    public String role(@PathVariable Long id, @RequestParam(required = false) String back, Principal principal, RedirectAttributes ra) {
        Member admin = memberService.get(principal.getName());
        return run(() -> adminService.toggleAdmin(id, admin), back, "/admin/members", ra);
    }

    @PostMapping("/auctions/{id}/archive")
    public String archive(@PathVariable String id, @RequestParam(required = false) String back, Principal principal, RedirectAttributes ra) {
        Member admin = memberService.get(principal.getName());
        return run(() -> adminService.toggleArchive(id, admin), back, "/admin/auctions", ra);
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable String id, @RequestParam(required = false) String back, Principal principal, RedirectAttributes ra) {
        Member admin = memberService.get(principal.getName());
        return run(() -> adminService.deleteProduct(id, admin), back, "/admin/products", ra);
    }

    private String run(Supplier<String> action, String back, String fallback, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("notice", action.get());
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        // 돌아갈 주소는 관리자 화면 안쪽만 허용 (외부 주소로 보내지 않음)
        boolean safe = back != null && back.startsWith("/admin") && !back.contains("//") && !back.contains("\\");
        return "redirect:" + (safe ? back : fallback);
    }
}

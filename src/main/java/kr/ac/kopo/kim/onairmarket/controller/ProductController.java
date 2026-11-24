package kr.ac.kopo.kim.onairmarket.controller;

import jakarta.servlet.http.HttpServletResponse;
import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.domain.Product;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.service.MemberService;
import kr.ac.kopo.kim.onairmarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** 즉시 구매 상품: 상세 · 구매 */
@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final MemberService memberService;

    @GetMapping("/products/{id}")
    public String detail(@PathVariable String id, Model model, HttpServletResponse response) {
        Product p;
        try {
            p = productService.get(id);
        } catch (ServiceException e) {
            model.addAttribute("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "not-found";
        }
        Member me = (Member) model.getAttribute("me");
        model.addAttribute("p", p);
        model.addAttribute("mine", me != null && p.getOwner().getId().equals(me.getId()));
        return "product-detail";
    }

    @PostMapping("/products/{id}/buy")
    public String buy(@PathVariable String id, Principal principal, RedirectAttributes ra) {
        Member me = memberService.get(principal.getName());
        try {
            productService.buy(id, me);
            ra.addFlashAttribute("notice", "구매했습니다. 판매자와 거래를 진행해 주세요. (실제 결제는 없는 체험 서비스입니다)");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + id;
    }
}

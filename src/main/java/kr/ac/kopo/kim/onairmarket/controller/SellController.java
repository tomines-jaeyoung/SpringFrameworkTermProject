package kr.ac.kopo.kim.onairmarket.controller;

import jakarta.validation.Valid;
import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.domain.TimedAuction;
import kr.ac.kopo.kim.onairmarket.domain.Product;
import kr.ac.kopo.kim.onairmarket.dto.AuctionForm;
import kr.ac.kopo.kim.onairmarket.dto.ProductForm;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.service.AuctionService;
import kr.ac.kopo.kim.onairmarket.service.MemberService;
import kr.ac.kopo.kim.onairmarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** 판매하기: 판매 방식 선택 → 사진·동영상 기간 경매 등록 또는 즉시 판매 상품 등록 */
@Controller
@RequiredArgsConstructor
public class SellController {
    private final AuctionService auctionService;
    private final ProductService productService;
    private final MemberService memberService;

    @GetMapping("/sell")
    public String choose() {
        return "sell-choice";
    }

    @GetMapping("/sell/timed")
    public String form(@ModelAttribute("form") AuctionForm form) {
        return "sell-timed";
    }

    @PostMapping("/sell/timed")
    public String create(@Valid @ModelAttribute("form") AuctionForm form, BindingResult binding,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         @RequestParam(value = "video", required = false) MultipartFile video,
                         Principal principal, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) return "sell-timed";
        Member me = memberService.get(principal.getName());
        try {
            TimedAuction a = auctionService.create(form, image, video, me);
            ra.addFlashAttribute("notice", "경매를 등록했습니다.");
            return "redirect:/auctions/" + a.getId();
        } catch (ServiceException e) {
            model.addAttribute("error", e.getMessage() + " 파일은 다시 선택해 주세요.");
            return "sell-timed";
        }
    }

    @GetMapping("/sell/product")
    public String productForm(@ModelAttribute("form") ProductForm form) {
        return "sell-product";
    }

    @PostMapping("/sell/product")
    public String createProduct(@Valid @ModelAttribute("form") ProductForm form, BindingResult binding,
                                @RequestParam(value = "image", required = false) MultipartFile image,
                                Principal principal, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) return "sell-product";
        Member me = memberService.get(principal.getName());
        try {
            Product p = productService.create(form, image, me);
            ra.addFlashAttribute("notice", "상품을 등록했습니다.");
            return "redirect:/products/" + p.getId();
        } catch (ServiceException e) {
            model.addAttribute("error", e.getMessage() + " 사진은 다시 선택해 주세요.");
            return "sell-product";
        }
    }
}

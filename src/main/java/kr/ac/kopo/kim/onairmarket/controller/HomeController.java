package kr.ac.kopo.kim.onairmarket.controller;

import kr.ac.kopo.kim.onairmarket.domain.Category;
import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.domain.TimedAuction;
import kr.ac.kopo.kim.onairmarket.service.AuctionService;
import kr.ac.kopo.kim.onairmarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 첫 화면(소개) · 경매 목록(/home): 카테고리·검색·정렬·진행 중 필터 */
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final AuctionService auctionService;
    private final ProductService productService;

    /** 원래 온에어 마켓의 첫 화면(소개 페이지) */
    @GetMapping("/")
    public String landing() {
        return "landing";
    }

    /**
     * 원래 온에어 마켓의 홈(/home).
     * 검색어·카테고리·정렬 등 조건이 없으면 섹션 화면(곧 낙찰·오늘의 발견·사진 경매 등),
     * 조건이 있거나 view=all 이면 경매 목록(둘러보기), view=products 이면 즉시 구매 목록을 보여 준다.
     */
    @GetMapping("/home")
    public String home(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "latest") String sort,
                       @RequestParam(defaultValue = "false") boolean open,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String view,
                       @RequestParam(defaultValue = "false") boolean video,
                       Model model) {
        category = (category == null || category.isBlank()) ? null : category;
        q = (q == null || q.isBlank()) ? null : q.trim();
        boolean products = "products".equals(view);
        boolean results = !products && (category != null || q != null || "all".equals(view) || open || video
                || page > 0 || !"latest".equals(sort));

        model.addAttribute("category", category);
        model.addAttribute("categoryLabel", Category.find(category).map(Category::label).orElse(null));
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("open", open);
        model.addAttribute("video", video);
        model.addAttribute("bookmarkCount", auctionService.bookmarkCount((Member) model.getAttribute("me")));
        if (products) {                                   // 즉시 구매 목록
            model.addAttribute("page", productService.list(category, q, open, page));
            model.addAttribute("mode", "products");
        } else if (results) {                              // 경매 둘러보기
            Page<TimedAuction> result = auctionService.list(category, q, sort, open, video, page);
            model.addAttribute("page", result);
            model.addAttribute("mode", "results");
        } else {                                           // 홈 섹션
            model.addAttribute("videoAuctions", auctionService.withVideo());
            model.addAttribute("endingSoon", auctionService.endingSoon());
            model.addAttribute("latest", auctionService.latest());
            model.addAttribute("products", productService.latestSelling());
            model.addAttribute("upcoming", auctionService.upcoming());
            model.addAttribute("mode", "home");
        }
        return "home";
    }
}

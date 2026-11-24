package kr.ac.kopo.kim.onairmarket.controller;

import kr.ac.kopo.kim.onairmarket.domain.Category;
import kr.ac.kopo.kim.onairmarket.domain.TimedAuction;
import kr.ac.kopo.kim.onairmarket.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 메인 · 목록: 카테고리·검색·정렬·진행 중 필터 */
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final AuctionService auctionService;

    @GetMapping("/")
    public String home(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "latest") String sort,
                       @RequestParam(defaultValue = "false") boolean open,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<TimedAuction> result = auctionService.list(category, q, sort, open, page);
        model.addAttribute("page", result);
        model.addAttribute("category", category);
        model.addAttribute("categoryLabel", Category.find(category).map(Category::label).orElse(null));
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("open", open);
        return "home";
    }
}

package kr.ac.kopo.kim.onairmarket.controller;

import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.service.AuctionService;
import kr.ac.kopo.kim.onairmarket.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/** 내 정보: 내 판매 목록 · 내 입찰 기록 · 관심 경매 */
@Controller
@RequiredArgsConstructor
public class MyPageController {
    private final AuctionService auctionService;
    private final MemberService memberService;

    @GetMapping("/my")
    public String my(Principal principal, Model model) {
        Member me = memberService.get(principal.getName());
        model.addAttribute("sales", auctionService.mySales(me));
        model.addAttribute("myBids", auctionService.myBids(me));
        model.addAttribute("bookmarks", auctionService.myBookmarks(me));
        return "my";
    }
}

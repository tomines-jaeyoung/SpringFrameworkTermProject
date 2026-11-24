package kr.ac.kopo.kim.onairmarket.service;

import kr.ac.kopo.kim.onairmarket.dto.AuctionForm;

/** 테스트에서 패키지 내부 메서드를 호출하기 위한 통로 */
public final class AuctionServiceTestAccess {
    public static long[] period(AuctionForm form, long now) {
        return AuctionService.period(form, now);
    }
}

package kr.ac.kopo.kim.onairmarket.exception;

/** 다른 입찰이 먼저 반영되어 revision·현재가가 바뀐 경우 (갱신 충돌) */
public class BidConflictException extends ServiceException {
    public BidConflictException() {
        super("다른 입찰이 먼저 반영되었습니다. 최신 가격을 확인한 뒤 다시 입찰해 주세요.");
    }
}

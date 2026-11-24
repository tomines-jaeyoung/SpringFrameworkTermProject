package kr.ac.kopo.kim.onairmarket.exception;

/** 화면에 그대로 보여 줄 수 있는 업무 오류 (입력 오류, 권한, 기간 등) */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}

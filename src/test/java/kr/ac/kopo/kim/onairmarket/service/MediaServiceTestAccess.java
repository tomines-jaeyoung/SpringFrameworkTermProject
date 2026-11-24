package kr.ac.kopo.kim.onairmarket.service;

/** 테스트에서 패키지 내부 메서드를 호출하기 위한 통로 */
public final class MediaServiceTestAccess {
    public static String detect(byte[] head) {
        return MediaService.detectMime(head);
    }
}

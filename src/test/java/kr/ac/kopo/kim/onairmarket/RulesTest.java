package kr.ac.kopo.kim.onairmarket;

import kr.ac.kopo.kim.onairmarket.dto.AuctionForm;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.service.AuctionServiceTestAccess;
import kr.ac.kopo.kim.onairmarket.service.MediaServiceTestAccess;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** DB 없이 돌아가는 규칙 테스트: 파일 형식 판정, 경매 기간 검사 */
class RulesTest {

    @Test
    void detectsFileTypeByMagicBytes() {
        assertEquals("image/jpeg", MediaServiceTestAccess.detect(new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertEquals("video/mp4", MediaServiceTestAccess.detect(new byte[]{0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2'}));
        assertEquals("image/webp", MediaServiceTestAccess.detect("RIFF0000WEBP".getBytes()));
        assertThrows(ServiceException.class, () -> MediaServiceTestAccess.detect("hello world!".getBytes()));
    }

    @Test
    void periodMustBeBetweenOneHourAnd365Days() {
        long now = System.currentTimeMillis();
        AuctionForm f = new AuctionForm();
        f.setStartMode("now");
        f.setDuration("7");
        long[] p = AuctionServiceTestAccess.period(f, now);
        assertEquals(7L * 86_400_000L, p[1] - p[0]);

        f.setDuration("custom");
        f.setEndsAt(LocalDateTime.now().plusMinutes(30));
        assertThrows(ServiceException.class, () -> AuctionServiceTestAccess.period(f, now));

        f.setEndsAt(LocalDateTime.now().plusDays(400));
        assertThrows(ServiceException.class, () -> AuctionServiceTestAccess.period(f, now));
    }
}

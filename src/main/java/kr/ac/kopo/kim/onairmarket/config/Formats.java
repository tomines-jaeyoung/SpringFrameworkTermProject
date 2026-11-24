package kr.ac.kopo.kim.onairmarket.config;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** 화면 표시용 포맷 (템플릿에서 ${@fmt.won(...)} 형태로 사용). 저장은 밀리초, 표시는 한국 시간. */
@Component("fmt")
public class Formats {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public String won(Long amount) {
        return amount == null ? "-" : NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원";
    }

    public String dateTime(Long millis) {
        return millis == null ? "-" : DT.format(Instant.ofEpochMilli(millis).atZone(KST));
    }

    public long now() {
        return System.currentTimeMillis();
    }

    /** 남은 시간: 1일 이상이면 "n일 n시간", 아니면 HH:MM:SS (분·초는 59를 넘지 않음) */
    public String remaining(Long endsAt) {
        long left = Math.max(0, endsAt - System.currentTimeMillis()) / 1000;
        if (left == 0) return "종료";
        long d = left / 86400, h = (left % 86400) / 3600, m = (left % 3600) / 60, s = left % 60;
        if (d > 0) return d + "일 " + h + "시간";
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}

package kr.ac.kopo.kim.onairmarket.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** 사진·동영상 기간 경매 등록 폼 (파일은 컨트롤러에서 MultipartFile 로 따로 받는다) */
@Data
public class AuctionForm {
    @NotBlank(message = "상품명을 입력해 주세요.")
    @Size(min = 3, max = 100, message = "상품명은 3~100자로 입력해 주세요.")
    private String title;

    @NotBlank(message = "상품 설명을 입력해 주세요.")
    @Size(min = 10, max = 3000, message = "상품 설명은 10~3,000자로 입력해 주세요.")
    private String description;

    @NotBlank(message = "카테고리를 선택해 주세요.")
    private String categoryId;

    @NotNull(message = "시작가를 입력해 주세요.")
    @Min(value = 100, message = "시작가는 100원 이상이어야 합니다.")
    @Max(value = 100_000_000, message = "시작가는 1억 원 이하로 입력해 주세요.")
    private Long startPrice;

    /** now: 등록 즉시 시작, scheduled: 시작 시각 예약 */
    private String startMode = "now";

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startsAt;

    /** 7, 30, 90 (일) 또는 custom */
    private String duration = "7";

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endsAt;

    @AssertTrue(message = "직접 보유한 실물 상품만 등록할 수 있습니다.")
    private boolean physicalItem;
}

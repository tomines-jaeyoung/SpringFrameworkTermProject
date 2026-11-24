package kr.ac.kopo.kim.onairmarket.controller;

import kr.ac.kopo.kim.onairmarket.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * /api/media/{id}: 업로드한 사진·영상 읽기.
 * Resource 로 응답하면 Spring MVC 가 브라우저의 Range(구간) 요청을 처리해 영상의 필요한 부분만 보낸다.
 */
@RestController
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @GetMapping("/api/media/{id}")
    public ResponseEntity<Resource> media(@PathVariable String id) {
        if (!id.matches("^[a-f0-9-]{36}$")) return ResponseEntity.notFound().build();
        return mediaService.load(id)
                .map(f -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(f.mime()))
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                        .header("X-Content-Type-Options", "nosniff")
                        .body(f.resource()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

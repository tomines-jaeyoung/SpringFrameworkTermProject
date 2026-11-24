package kr.ac.kopo.kim.onairmarket.service;

import kr.ac.kopo.kim.onairmarket.domain.AuctionMedia;
import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.repository.AuctionMediaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

/**
 * 사진·동영상 업로드. 기존 온에어마켓(auction-media.ts)과 같은 규칙:
 * 영상 50MB(MP4·WebM), 사진 8MB(JPG·PNG·WebP), 파일 앞부분 바이트로 실제 형식 확인,
 * 계정당 100개·1GB 제한, pending → ready, 실패하면 파일과 DB 행을 정리한다.
 */
@Service
public class MediaService {
    public static final long VIDEO_MAX_BYTES = 50L * 1024 * 1024;
    public static final long IMAGE_MAX_BYTES = 8L * 1024 * 1024;
    private static final long OWNER_MAX_FILES = 100;
    private static final long OWNER_MAX_BYTES = 1024L * 1024 * 1024;

    private final AuctionMediaRepository mediaRepository;
    private final Path root;

    public MediaService(AuctionMediaRepository mediaRepository, @Value("${onair.upload-dir}") String uploadDir) throws IOException {
        this.mediaRepository = mediaRepository;
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root.resolve("auction-media"));
    }

    /** 파일 1개 저장. 실패하면 남은 파일·행을 지우고 예외를 던진다. */
    @Transactional
    public AuctionMedia store(MultipartFile file, String kind, Member owner) {
        if (file == null || file.isEmpty()) throw new ServiceException("업로드할 파일을 선택해 주세요.");
        long size = file.getSize();
        long max = kind.equals("video") ? VIDEO_MAX_BYTES : IMAGE_MAX_BYTES;
        if (size < 12) throw new ServiceException("파일 크기를 확인해 주세요.");
        if (size > max) throw new ServiceException(kind.equals("video") ? "영상은 50MB까지 올릴 수 있습니다." : "사진은 8MB까지 올릴 수 있습니다.");
        if (mediaRepository.countByOwnerId(owner.getId()) >= OWNER_MAX_FILES
                || mediaRepository.sumBytesByOwner(owner.getId()) + size > OWNER_MAX_BYTES) {
            throw new ServiceException("계정당 파일 100개, 총 1GB까지 올릴 수 있습니다.");
        }
        String mime;
        try (InputStream in = file.getInputStream()) {
            mime = detectMime(in.readNBytes(12));
        } catch (IOException e) {
            throw new ServiceException("파일을 읽지 못했습니다.");
        }
        if (!mime.startsWith(kind + "/")) {
            throw new ServiceException(kind.equals("video") ? "영상 칸에는 MP4·WebM 영상을 올려 주세요." : "사진 칸에는 JPG·PNG·WebP 사진을 올려 주세요.");
        }

        AuctionMedia media = new AuctionMedia();
        media.setId(UUID.randomUUID().toString());
        media.setOwner(owner);
        media.setObjectKey("auction-media/" + media.getId());
        media.setKind(kind);
        media.setMime(mime);
        media.setBytes(size);
        media.setStatus("pending");
        media.setCreated(System.currentTimeMillis());
        // ID를 직접 지정한 엔티티는 save 가 merge 로 동작하므로, 반환된(영속 상태) 객체를 계속 사용한다
        media = mediaRepository.saveAndFlush(media);

        Path target = root.resolve(media.getObjectKey());
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            if (Files.size(target) != size) throw new IOException("size mismatch");
        } catch (IOException e) {
            deleteQuietly(target);
            mediaRepository.delete(media);
            throw new ServiceException("파일을 저장하지 못했습니다. 다시 시도해 주세요.");
        }
        media.setStatus("ready");
        return mediaRepository.save(media);
    }

    /** 경매 등록이 실패했을 때 이미 저장한 파일과 행을 정리 */
    @Transactional
    public void discard(AuctionMedia media) {
        if (media == null) return;
        deleteQuietly(root.resolve(media.getObjectKey()));
        mediaRepository.findById(media.getId()).ifPresent(mediaRepository::delete);
    }

    @Transactional(readOnly = true)
    public Optional<MediaFile> load(String id) {
        return mediaRepository.findByIdAndStatus(id, "ready").map(m -> {
            Resource resource = new FileSystemResource(root.resolve(m.getObjectKey()));
            return new MediaFile(resource, m.getMime());
        }).filter(f -> f.resource().exists());
    }

    public record MediaFile(Resource resource, String mime) {
    }

    /** 확장자가 아니라 파일 앞부분(매직 바이트)으로 형식 판정 */
    static String detectMime(byte[] b) {
        if (b.length >= 12 && new String(b, 4, 4, StandardCharsets.US_ASCII).equals("ftyp")) return "video/mp4";
        if (b.length >= 4 && (b[0] & 0xff) == 0x1a && (b[1] & 0xff) == 0x45 && (b[2] & 0xff) == 0xdf && (b[3] & 0xff) == 0xa3) return "video/webm";
        if (b.length >= 3 && (b[0] & 0xff) == 0xff && (b[1] & 0xff) == 0xd8 && (b[2] & 0xff) == 0xff) return "image/jpeg";
        if (b.length >= 8 && (b[0] & 0xff) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71) return "image/png";
        if (b.length >= 12 && new String(b, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
                && new String(b, 8, 4, StandardCharsets.US_ASCII).equals("WEBP")) return "image/webp";
        throw new ServiceException("MP4·WebM 영상 또는 JPG·PNG·WebP 사진을 올려 주세요.");
    }

    private static void deleteQuietly(Path p) {
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
        }
    }
}

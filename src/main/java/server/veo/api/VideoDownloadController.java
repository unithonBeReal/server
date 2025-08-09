package server.veo.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.veo.service.VideoStorageService;

import java.nio.file.Path;

/**
 * 생성된 비디오 파일 다운로드 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@Slf4j
public class VideoDownloadController {

    private final VideoStorageService videoStorageService;

    /**
     * 비디오 파일 다운로드
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> downloadVideo(@PathVariable String fileName) {
        try {
            log.info("📥 비디오 다운로드 요청: {}", fileName);

            // 파일 존재 여부 확인
            if (!videoStorageService.videoFileExists(fileName)) {
                log.warn("❌ 비디오 파일을 찾을 수 없음: {}", fileName);
                return ResponseEntity.notFound().build();
            }

            // 파일 리소스 생성
            Path filePath = videoStorageService.getVideoFilePath(fileName);
            Resource resource = new FileSystemResource(filePath);

            // Content-Type 설정
            String contentType = determineContentType(fileName);
            
            log.info("✅ 비디오 파일 다운로드 시작: {} (타입: {})", fileName, contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ 비디오 다운로드 실패: {}, 오류: {}", fileName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 파일 확장자에 따른 Content-Type 결정
     */
    private String determineContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        
        if (lowerFileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerFileName.endsWith(".webm")) {
            return "video/webm";
        } else if (lowerFileName.endsWith(".avi")) {
            return "video/avi";
        } else if (lowerFileName.endsWith(".mov")) {
            return "video/quicktime";
        } else {
            return "video/mp4"; // 기본값
        }
    }
} 
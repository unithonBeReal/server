package server.veo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 비디오 파일 저장 및 URL 생성 서비스
 * videoBytes를 로컬 파일로 저장하고 접근 가능한 URL 반환
 */
@Service
@Slf4j
public class VideoStorageService {

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String UPLOAD_DIR = "uploads/videos";
    private static final String URL_PREFIX = "http://localhost";

    /**
     * videoBytes를 파일로 저장하고 다운로드 URL 반환
     */
    public String saveVideoAndGetUrl(byte[] videoBytes, String mimeType) {
        try {
            // 1. 업로드 디렉토리 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("📁 비디오 업로드 디렉토리 생성: {}", uploadPath.toAbsolutePath());
            }

            // 2. 고유한 파일명 생성
            String fileExtension = getFileExtension(mimeType);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("veo_video_%s_%s%s", 
                    timestamp, UUID.randomUUID().toString().substring(0, 8), fileExtension);

            // 3. 파일 저장
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, videoBytes);
            
            log.info("💾 비디오 파일 저장 완료: {} (크기: {} bytes)", 
                    filePath.toAbsolutePath(), videoBytes.length);

            // 4. 다운로드 URL 생성
            String downloadUrl = String.format("%s:%s/api/v1/videos/%s", 
                    URL_PREFIX, serverPort, fileName);
            
            log.info("🔗 비디오 다운로드 URL 생성: {}", downloadUrl);
            return downloadUrl;

        } catch (IOException e) {
            log.error("❌ 비디오 파일 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("비디오 파일 저장 실패", e);
        }
    }

    /**
     * MIME 타입에서 파일 확장자 추출
     */
    private String getFileExtension(String mimeType) {
        if (mimeType == null) {
            return ".mp4"; // 기본값
        }
        
        switch (mimeType.toLowerCase()) {
            case "video/mp4":
                return ".mp4";
            case "video/webm":
                return ".webm";
            case "video/avi":
                return ".avi";
            case "video/mov":
            case "video/quicktime":
                return ".mov";
            default:
                return ".mp4"; // 기본값
        }
    }

    /**
     * 파일명으로 저장된 비디오 파일의 절대 경로 반환
     */
    public Path getVideoFilePath(String fileName) {
        return Paths.get(UPLOAD_DIR).resolve(fileName);
    }

    /**
     * 저장된 비디오 파일 존재 여부 확인
     */
    public boolean videoFileExists(String fileName) {
        return Files.exists(getVideoFilePath(fileName));
    }
} 
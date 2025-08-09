package server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

/**
 * Veo 비디오 생성을 위한 Google Gen AI 설정 클래스
 * 
 * ✅ Vertex AI 서비스 계정 인증을 지원
 * ✅ 적절한 OAuth2 scope 설정
 * ✅ 상세한 디버깅 및 오류 처리
 */
@Configuration
public class VeoConfig {

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${google.cloud.location}")
    private String location;

    @Value("${google.genai.api-key:}")
    private String apiKey;

    @Value("${google.cloud.storage.bucket:veo-images-bucket}")
    private String gcsBucketName;

    // ✅ Vertex AI/Gen AI에 필요한 OAuth2 scopes
    private static final List<String> REQUIRED_SCOPES = Arrays.asList(
        "https://www.googleapis.com/auth/cloud-platform",
        "https://www.googleapis.com/auth/generative-language"
    );

    @Bean
    public Client genAiClient() {
        try {
            // ✅ 서비스 계정 인증 설정 (개선된 방법)
            GoogleCredentials credentials = createGoogleCredentials();
            
            if (credentials != null) {
                // ✅ Credentials 유효성 검증 및 refresh
                validateAndRefreshCredentials(credentials);
                
                System.out.println("🔑 Vertex AI 서비스 계정 인증 설정 완료");
                System.out.println("📋 Project ID: " + projectId);
                System.out.println("📍 Location: " + location);
                
                return Client.builder()
                        .project(projectId)
                        .location(location)
                        .vertexAI(true)
                        .credentials(credentials)
                        .build();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Vertex AI 인증 실패: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("🔍 원인: " + e.getCause().getMessage());
            }
            e.printStackTrace();
        }
        
        // ✅ Fallback: API 키 방식 (제한된 기능)
        if (apiKey != null && !apiKey.isEmpty()) {
            System.out.println("⚠️ API 키 방식으로 fallback (이미지 기능 제한됨)");
            return Client.builder()
                    .apiKey(apiKey)
                    .build();
        }
        
        throw new IllegalStateException(
            "Google Gen AI 클라이언트 생성 실패: " +
            "서비스 계정 키 파일 또는 GOOGLE_API_KEY가 필요합니다."
        );
    }
    
    /**
     * ✅ Google Cloud Storage 클라이언트 Bean 생성 (GCS 이미지 업로드용)
     */
    @Bean
    public Storage gcsStorage() {
        try {
            GoogleCredentials credentials = createGoogleCredentials();
            
            if (credentials != null) {
                validateAndRefreshCredentials(credentials);
                
                System.out.println("🗄️ Google Cloud Storage 클라이언트 설정 완료");
                System.out.println("📋 Project ID: " + projectId);
                System.out.println("🪣 Storage Bucket: " + gcsBucketName);
                
                return StorageOptions.newBuilder()
                        .setProjectId(projectId)
                        .setCredentials(credentials)
                        .build()
                        .getService();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Google Cloud Storage 초기화 실패: " + e.getMessage());
        }
        
        System.err.println("❌ Google Cloud Storage 설정 실패 - null 반환");
        return null;
    }
    
    /**
     * Google Credentials 객체 생성
     * OAuth2 scope 설정 포함
     */
    private GoogleCredentials createGoogleCredentials() throws Exception {
        GoogleCredentials credentials = null;
        
        // ✅ 방법 1: resources 폴더에서 파일 읽기 (CI/CD + 로컬)
        try {
            ClassPathResource resource = new ClassPathResource("service-account-key.json");
            if (resource.exists()) {
                System.out.println("📁 resources에서 서비스 계정 키 파일 읽기");
                
                try (InputStream inputStream = resource.getInputStream()) {
                    credentials = GoogleCredentials.fromStream(inputStream);
                    System.out.println("✅ 서비스 계정 키로부터 credentials 생성 완료");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ resources에서 서비스 계정 키 파일을 읽을 수 없습니다: " + e.getMessage());
        }
        
        // ✅ 방법 2: 기존 환경변수 GOOGLE_APPLICATION_CREDENTIALS 파일 읽기
        if (credentials == null) {
            String existingCredentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (existingCredentialsPath != null) {
                File credentialsFile = new File(existingCredentialsPath);
                if (credentialsFile.exists()) {
                    System.out.println("🔗 기존 GOOGLE_APPLICATION_CREDENTIALS 파일 사용: " + existingCredentialsPath);
                    
                    try (FileInputStream fileInputStream = new FileInputStream(credentialsFile)) {
                        credentials = GoogleCredentials.fromStream(fileInputStream);
                        System.out.println("✅ 환경변수 경로의 서비스 계정 키로부터 credentials 생성 완료");
                    }
                } else {
                    System.err.println("❌ GOOGLE_APPLICATION_CREDENTIALS 파일이 존재하지 않습니다: " + existingCredentialsPath);
                }
            }
        }
        
        // ✅ 방법 3: 기본 credentials (ADC - Application Default Credentials)
        if (credentials == null) {
            try {
                credentials = GoogleCredentials.getApplicationDefault();
                System.out.println("🔄 Application Default Credentials 사용");
            } catch (Exception e) {
                System.err.println("⚠️ Application Default Credentials 사용 불가: " + e.getMessage());
            }
        }
        
        if (credentials == null) {
            System.out.println("❌ 모든 인증 방법 실패: 서비스 계정 키 파일을 찾을 수 없습니다");
            return null;
        }
        
        // ✅ 필수: OAuth2 scope 설정
        if (credentials.createScopedRequired()) {
            System.out.println("🔧 OAuth2 scope 설정 중...");
            credentials = credentials.createScoped(REQUIRED_SCOPES);
            System.out.println("✅ OAuth2 scope 설정 완료: " + REQUIRED_SCOPES);
        } else {
            System.out.println("ℹ️ 이미 scope가 설정된 credentials입니다");
        }
        
        return credentials;
    }
    
    /**
     * Credentials 유효성 검증 및 refresh
     */
    private void validateAndRefreshCredentials(GoogleCredentials credentials) throws Exception {
        try {
            System.out.println("🔄 Credentials refresh 시도 중...");
            
            // 명시적으로 refresh 시도
            credentials.refresh();
            
            // Access token 확인
            if (credentials.getAccessToken() != null) {
                System.out.println("✅ Access token 획득 성공");
                System.out.println("⏰ Token 만료 시간: " + credentials.getAccessToken().getExpirationTime());
            } else {
                System.err.println("⚠️ Access token이 null입니다");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Credentials refresh 실패: " + e.getMessage());
            
            // 구체적인 오류 정보 출력
            if (e.getMessage().contains("invalid_scope")) {
                System.err.println("🔍 OAuth2 scope 오류가 발생했습니다. 서비스 계정에 다음 권한이 필요합니다:");
                REQUIRED_SCOPES.forEach(scope -> System.err.println("   - " + scope));
            }
            
            throw e;
        }
    }
} 

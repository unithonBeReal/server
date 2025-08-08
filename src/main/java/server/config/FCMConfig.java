package server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FCMConfig {

    // 파이어베이스 엑세스 토큰 갖기
    @PostConstruct
    public void init() {
        try {
            // Firebase 콘솔에서 다운로드한 서비스 계정 키 파일 사용
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource("firebase-account.json").getInputStream());

            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(googleCredentials)
                    .build();

            // Firebase 초기화
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(firebaseOptions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package server.youtube.api;

import server.youtube.service.UserGroupScheduler;
import server.youtube.service.YouTubeDataScheduler;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class YouTubeTestController {

    private final YouTubeDataScheduler youTubeDataScheduler;
    private final UserGroupScheduler userGroupScheduler;

    /**
     * 스케줄러의 YouTube 영상 캐시 업데이트 로직을 수동으로 실행합니다.
     * 유튜브 사용량 때문에 하루에 '한 번'만 호출할 수 있습니다
     */
    @Hidden
    @GetMapping("/youtube/update-cache")
    public ResponseEntity<String> manuallyUpdateYouTubeCache() {
        youTubeDataScheduler.updateDailyYouTubeCache();
        return ResponseEntity.ok("Successfully triggered YouTube cache update.");
    }

    @Hidden
    @GetMapping("/update-cache")
    public ResponseEntity<String> updateGroup() {
        userGroupScheduler.updateAllUserGroups();
        return ResponseEntity.ok("Successfully triggered YouTube cache update.");
    }
} 

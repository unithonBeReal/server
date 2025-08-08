package server.youtube.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserGroupScheduler {

    private final MemberPreferenceService memberPreferenceService;

    /**
     * 매일 새벽 2시에 모든 사용자의 유튜브 그룹을 업데이트합니다.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void updateAllUserGroups() {
        log.info("사용자 유튜브 그룹 업데이트 스케줄러 시작");
        
        try {
            memberPreferenceService.updateAllUserGroups();
            log.info("사용자 유튜브 그룹 업데이트 완료");
        } catch (Exception e) {
            log.error("사용자 유튜브 그룹 업데이트 중 오류 발생", e);
        }
    }
} 

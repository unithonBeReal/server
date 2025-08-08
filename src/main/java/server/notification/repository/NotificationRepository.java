package server.notification.repository;

import server.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    void deleteAllByMemberId(Long memberId);
}

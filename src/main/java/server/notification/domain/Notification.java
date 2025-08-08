package server.notification.domain;

import server.common.BaseTimeEntity;
import server.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;

    @Column(length = 50)
    private String notificationType;

    private String message;

    private LocalDateTime createdTime;
}

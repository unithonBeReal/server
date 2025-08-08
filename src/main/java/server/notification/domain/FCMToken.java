package server.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FCMToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UniqueElements
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public FCMToken(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}

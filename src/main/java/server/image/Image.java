package server.image;

import server.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 이미지는 도메인과 상관없이 Image 엔티티로 저장한다.
 */
@Table(name = "image")
@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ImageCategory imageCategory;

    private Long domainId;

    private String path;

    public void update(Long domainId, String path) {
        this.domainId = domainId;
        this.path = path;
    }
}

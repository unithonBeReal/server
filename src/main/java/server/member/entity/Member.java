package server.member.entity;

import server.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String nickName;

    @Column
    private String profileImage;

    @Column
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column
    private Boolean privacy;

    private String email;

    private String birthYear;

    @Column
    private String providerId;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(length = 300)
    private String introduction;

    public String updateProfileImage(String profileImage){
        this.profileImage = profileImage;
        return this.profileImage;
    }

    public String updateNickname(String nickname) {
        this.nickName = nickname;
        return this.nickName;
    }

    public void updateProfile(String nickName, String profileImage, String introduction) {
        this.nickName = nickName;
        this.profileImage = profileImage;
        this.introduction = introduction;
    }
}

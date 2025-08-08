package server.member.dto.response;

import server.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyProfileResponse {
    private Long id;

    private String nickName;

    private String profileImage;

    private Boolean privacy;

    private String email;

    public static MyProfileResponse of(Member member){
        return new MyProfileResponse(
                member.getId(),
                member.getNickName(),
                member.getProfileImage(),
                member.getPrivacy(),
                member.getEmail()
        );
    }
}

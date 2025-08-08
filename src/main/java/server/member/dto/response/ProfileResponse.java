package server.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponse {
    private Long memberId;
    private int followings;
    private int followers;
    private int books;
    private int collections;
    private int scraps;
    private int reviews;

}

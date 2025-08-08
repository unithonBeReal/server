package server.image;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageCategory {

    MEMBER_PROFILE("사용자 프로필 이미지"),
    DIARY_IMAGE("독서 일지 이미지"),
    CHAT_IMAGE("채팅 사용 이미지")
    ;

    private final String description;

}

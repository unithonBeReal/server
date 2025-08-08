package server.member.domain;

import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("도메인: Member")
class MemberTest {

    @Test
    @DisplayName("프로필 정보를 성공적으로 수정한다")
    void updateProfile_Success() {
        // given
        Member member = MemberFixture.create();
        String newNickname = "새로운닉네임";
        String newProfileImage = "new_image.jpg";
        String newIntroduction = "새로운 자기소개입니다.";

        // when
        member.updateProfile(newNickname, newProfileImage, newIntroduction);

        // then
        assertThat(member.getNickName()).isEqualTo(newNickname);
        assertThat(member.getProfileImage()).isEqualTo(newProfileImage);
        assertThat(member.getIntroduction()).isEqualTo(newIntroduction);
    }
} 

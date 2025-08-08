package server.member.service;

import server.challenge.repository.ReadingDiaryRepository;
import server.follow.repository.FollowRepository;
import server.member.dto.ProfileResponse;
import server.member.dto.UpdateProfileRequest;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("서비스: Profile")
class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private ReadingDiaryRepository diaryRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.create();
    }

    @Nested
    @DisplayName("프로필 조회 시")
    class Describe_getProfile {
        @Test
        @DisplayName("사용자 정보와 팔로워, 팔로잉, 일기 수를 함께 반환한다")
        void it_returns_profile_with_counts() {
            // given
            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
            given(followRepository.countByFollower(member)).willReturn(10L);
            given(followRepository.countByFollowing(member)).willReturn(5L);
            given(diaryRepository.countByReadingChallenge_Member(member)).willReturn(3L);

            // when
            ProfileResponse.WithCounts response = profileService.getProfile(member.getId());

            // then
            assertThat(response.memberId()).isEqualTo(member.getId());
            assertThat(response.nickName()).isEqualTo(member.getNickName());
            assertThat(response.followingCount()).isEqualTo(10L);
            assertThat(response.followerCount()).isEqualTo(5L);
            assertThat(response.diaryCount()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("프로필 수정 시")
    class Describe_updateProfile {
        @Test
        @DisplayName("수정된 프로필 정보를 반환한다")
        void it_returns_updated_profile() {
            // given
            UpdateProfileRequest request = new UpdateProfileRequest("새 닉네임", "new.jpg", "새 소개");
            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);

            // when
            ProfileResponse response = profileService.updateProfile(member.getId(), request);

            // then
            assertThat(response.memberId()).isEqualTo(member.getId());
            assertThat(response.nickName()).isEqualTo("새 닉네임");
            assertThat(response.introduction()).isEqualTo("새 소개");
        }
    }
} 

package server.member.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.config.SecurityTestUtils;
import server.member.dto.UpdateProfileRequest;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("API: Profile")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SecurityTestUtils securityTestUtils;
    @Autowired
    private MemberRepository memberRepository;

    private Member user;
    private RequestPostProcessor mockUser;

    @BeforeEach
    void setUp() {
        user = memberRepository.save(MemberFixture.createWithoutId());
        mockUser = securityTestUtils.mockUser(user);

    }

    @Nested
    @DisplayName("프로필 조회 API [/api/v2/profiles/{memberId}]")
    class Describe_getProfile {
        @Test
        @DisplayName("사용자 프로필 정보를 정상적으로 조회한다")
        void it_returns_profile_successfully() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v2/profiles/{memberId}", user.getId())
                            .with(mockUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.memberId").value(user.getId()))
                    .andExpect(jsonPath("$.data.nickName").value(user.getNickName()))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("프로필 수정 API [/api/v2/profiles/me]")
    class Describe_updateProfile {
        @Test
        @DisplayName("인증된 사용자가 프로필을 수정하면, 수정된 정보가 반환된다")
        void it_returns_updated_profile() throws Exception {
            // given
            UpdateProfileRequest request = new UpdateProfileRequest("수정된 닉네임", "updated.jpg", "수정된 소개");

            // when & then
            mockMvc.perform(put("/api/v2/profiles/me")
                            .with(mockUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.nickName", is("수정된 닉네임")))
                    .andExpect(jsonPath("$.data.profileImageUrl", is("updated.jpg")))
                    .andExpect(jsonPath("$.data.introduction", is("수정된 소개")))
                    .andDo(print());
        }
    }
} 

package server.search.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.config.RedisTestContainer;
import server.config.SecurityTestUtils;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
class SearchApiTest extends RedisTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SecurityTestUtils securityTestUtils;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Member testMember;
    private RequestPostProcessor mockUser;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(MemberFixture.createWithoutId());
        mockUser = securityTestUtils.mockUser(testMember);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        String redisKey = "search_history::" + testMember.getId();
        stringRedisTemplate.delete(redisKey);
    }

    @Test
    @DisplayName("최근 검색 기록을 성공적으로 조회한다")
    void getSearchHistories_Success() throws Exception {
        // given
        String key = "search_history::" + testMember.getId();
        stringRedisTemplate.opsForZSet().add(key, "자바", 1.0);
        stringRedisTemplate.opsForZSet().add(key, "스프링", 2.0);
        stringRedisTemplate.opsForZSet().add(key, "코틀린", 3.0);

        // when & then
        mockMvc.perform(get("/api/v2/search/histories")
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].queries").value("코틀린"))
                .andExpect(jsonPath("$.data[1].queries").value("스프링"))
                .andExpect(jsonPath("$.data[2].queries").value("자바"));

    }
} 

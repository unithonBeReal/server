package server.timer.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.config.SecurityTestUtils;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import server.timer.domain.TimerLog;
import server.timer.dto.TimerRequest;
import server.timer.repository.TimerLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TimerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityTestUtils securityTestUtils;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TimerLogRepository timerLogRepository;

    private Member member;
    private RequestPostProcessor mockUser;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createWithoutId());
        mockUser = securityTestUtils.mockUser(member);
    }

    @Test
    @DisplayName("오늘 처음 타이머 시간을 기록합니다.")
    void addTodayTimer_firstTime() throws Exception {
        // given
        int secondsToAdd = 60;
        TimerRequest request = new TimerRequest(secondsToAdd);

        // when
        ResultActions result = mockMvc.perform(post("/api/v2/timer")
                .with(mockUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andDo(print());

        TimerLog timerLog = timerLogRepository.findByMemberAndLogDate(member, LocalDate.now()).orElseThrow();
        assertThat(timerLog.getTotalSeconds()).isEqualTo(secondsToAdd);
    }

    @Test
    @DisplayName("오늘 기록된 타이머 시간에 시간을 추가합니다.")
    void addTodayTimer_accumulate() throws Exception {
        // given
        int initialSeconds = 120;
        timerLogRepository.saveAndFlush(TimerLog.builder()
                .member(member)
                .logDate(LocalDate.now())
                .totalSeconds(initialSeconds)
                .build());

        int secondsToAdd = 60;
        TimerRequest request = new TimerRequest(secondsToAdd);

        // when
        ResultActions result = mockMvc.perform(post("/api/v2/timer")
                .with(mockUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andDo(print());

        TimerLog timerLog = timerLogRepository.findByMemberAndLogDate(member, LocalDate.now()).orElseThrow();
        assertThat(timerLog.getTotalSeconds()).isEqualTo(initialSeconds + secondsToAdd);
    }

    @Test
    @DisplayName("오늘 기록된 타이머 시간이 없을 때 0초를 반환합니다.")
    void getTodayTimer_noLog() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v2/timer/today")
                .with(mockUser));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSeconds").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("오늘 기록된 타이머 시간을 조회합니다.")
    void getTodayTimer_withLog() throws Exception {
        // given
        int totalSeconds = 300;
        timerLogRepository.saveAndFlush(TimerLog.builder()
                .member(member)
                .logDate(LocalDate.now())
                .totalSeconds(totalSeconds)
                .build());

        // when
        ResultActions result = mockMvc.perform(get("/api/v2/timer/today")
                .with(mockUser));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSeconds").value(totalSeconds))
                .andDo(print());
    }
} 

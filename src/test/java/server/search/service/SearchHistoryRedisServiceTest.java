package server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;

import server.config.RedisTestContainer;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@SpringBootTest
class SearchHistoryRedisServiceTest extends RedisTestContainer {

    @Autowired
    private SearchHistoryRedisService searchHistoryRedisService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private SearchHistoryDbService searchHistoryDbService;

    private static final String TEST_USER_ID = "1";
    private static final String TEST_KEY = "search_history::" + TEST_USER_ID;

    @AfterEach
    void tearDown() {
        // 테스트 후 Redis 데이터 삭제
        redisTemplate.delete(TEST_KEY);
    }


    @Test
    @DisplayName("검색어를 Redis에 성공적으로 저장하고 TTL을 설정한다")
    void saveAndCacheSearchQuery_Success() {
        // given
        String query = "테스트";
        doNothing().when(searchHistoryDbService).save(anyLong(), any(String.class));

        // when
        searchHistoryRedisService.save(Long.valueOf(TEST_USER_ID), query);

        // then
        Set<ZSetOperations.TypedTuple<String>> results = redisTemplate.opsForZSet()
                .rangeWithScores(TEST_KEY, 0, -1);
        assertThat(results).hasSize(1);
        assertThat(Objects.requireNonNull(results).iterator().next().getValue()).isEqualTo(query);

        // TTL이 설정되었는지 확인 (약 30일)
        Long expire = redisTemplate.getExpire(TEST_KEY, TimeUnit.SECONDS);
        assertThat(expire).isNotNull();
        assertThat(expire).isCloseTo(TimeUnit.DAYS.toSeconds(30), org.assertj.core.api.Assertions.within(10L));
    }

    @Test
    @DisplayName("검색 기록이 최대 개수를 초과하면 가장 오래된 기록을 삭제하고 저장한다.")
    void trimOldestSearchQuery_When_ExceedsMaxSize() {
        // given
        int maxSize = 20; // SearchHistoryService의 MAX_HISTORY_SIZE와 동일
        doNothing().when(searchHistoryDbService).save(anyLong(), any(String.class));

        for (int i = 0; i <= maxSize; i++) {
            searchHistoryRedisService.save(Long.valueOf(TEST_USER_ID), "query" + i);
        }

        // then
        Long size = redisTemplate.opsForZSet().zCard(TEST_KEY);
        assertThat(size).isEqualTo(maxSize);

        Set<String> results = redisTemplate.opsForZSet().range(TEST_KEY, 0, -1);
        assertThat(results).doesNotContain("query0");
        assertThat(results).contains("query" + maxSize);
    }

} 

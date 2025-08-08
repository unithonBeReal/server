package server.ai_recommendation.service;

import server.ai_recommendation.external.AiClient;
import server.ai_recommendation.external.AiRecommendationResult;
import server.ai_recommendation.external.AiRecommendationResults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class AiRecommendationCacheServiceTest {

    @Autowired
    private AiRecommendationCacheService aiRecommendationCacheService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private AiClient aiClient;

    @AfterEach
    void tearDown() {
        // 테스트 간 캐시 상태 격리를 위해 매번 캐시를 비웁니다.
        Objects.requireNonNull(cacheManager.getCache("recommendation")).clear();
    }

    @Test
    @DisplayName("추천 도서 ID 목록을 성공적으로 캐싱한다")
    void getRecommendedBookIds_Caching_Success() {
        // given
        Long userId = 1L;
        List<Long> expectedBookIds = List.of(10L, 20L, 30L);

        // 각 bookId를 갖는 AiRecommendationResult 객체 리스트를 생성
        List<AiRecommendationResult> mockResultList = expectedBookIds.stream()
                .map(id -> {
                    AiRecommendationResult result = new AiRecommendationResult();
                    result.setBookId(id);
                    return result;
                })
                .toList();

        AiRecommendationResults mockResults = new AiRecommendationResults(mockResultList);

        when(aiClient.getRecommendation(userId)).thenReturn(mockResults);

        // when & then
        // 1. 첫 번째 호출 - 캐시에 데이터가 없으므로 aiClient를 호출해야 함
        List<Long> firstResult = aiRecommendationCacheService.getRecommendedBookIds(userId);
        
        assertThat(firstResult).isEqualTo(expectedBookIds);
        verify(aiClient, times(1)).getRecommendation(userId);

        // 2. 두 번째 호출 - 캐시에서 데이터를 가져와야 하므로 aiClient를 다시 호출하면 안 됨
        List<Long> secondResult = aiRecommendationCacheService.getRecommendedBookIds(userId);

        assertThat(secondResult).isEqualTo(expectedBookIds);
        // aiClient.getRecommendation(userId)는 여전히 총 1번만 호출되어야 함
        verify(aiClient, times(1)).getRecommendation(userId);
    }
} 

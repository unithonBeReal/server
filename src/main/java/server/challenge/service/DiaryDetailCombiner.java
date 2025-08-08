package server.challenge.service;

import server.challenge.dto.response.DiaryImageResponse;
import server.challenge.dto.response.DiaryResponse;
import server.challenge.repository.ReadingDiaryImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiaryDetailCombiner {

    private final ReadingDiaryImageRepository readingDiaryImageRepository;
    private final ReadingDiaryStatisticService readingDiaryStatisticService;

    /**
     * 독서일지 이미지와, 독서일지 댓글수, 독서일지 좋아요 수를 가져온다
     */
    public List<? extends DiaryResponse.DiaryFeed> combine(List<? extends DiaryResponse.DiaryFeed> diaries) {
        if (diaries == null || diaries.isEmpty()) {
            return diaries;
        }

        List<Long> diaryIds = diaries.stream()
                .map(DiaryResponse.DiaryFeed::getDiaryId)
                .toList();

        Map<Long, List<DiaryImageResponse>> imagesMap = readingDiaryImageRepository.findImagesByDiaryIds(diaryIds);
        Map<Long, Map<CountType, Long>> countsMap = readingDiaryStatisticService.getCounts(diaryIds);

        diaries.forEach(diary -> {
            Long diaryId = diary.getDiaryId();
            diary.setImages(imagesMap.getOrDefault(diaryId, Collections.emptyList()));

            Map<CountType, Long> counts = countsMap.getOrDefault(diaryId, Collections.emptyMap());
            diary.setLikeCount(counts.getOrDefault(CountType.LIKE, 0L).intValue());
            diary.setCommentCount(counts.getOrDefault(CountType.COMMENT, 0L).intValue());
            diary.setViewCount(counts.getOrDefault(CountType.VIEW, 0L).intValue());
        });

        return diaries;
    }
} 

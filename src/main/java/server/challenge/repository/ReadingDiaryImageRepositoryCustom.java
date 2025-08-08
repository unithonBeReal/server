package server.challenge.repository;

import server.challenge.dto.response.DiaryImageResponse;
import java.util.List;
import java.util.Map;

public interface ReadingDiaryImageRepositoryCustom {
    Map<Long, List<DiaryImageResponse>> findImagesByDiaryIds(List<Long> diaryIds);
} 

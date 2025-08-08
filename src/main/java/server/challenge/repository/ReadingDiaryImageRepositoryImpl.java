package server.challenge.repository;

import server.challenge.dto.response.DiaryImageResponse;
import book.book.challenge.dto.response.QDiaryImageResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static book.book.challenge.domain.QReadingDiaryImage.readingDiaryImage;
import static java.util.stream.Collectors.groupingBy;

@RequiredArgsConstructor
public class ReadingDiaryImageRepositoryImpl implements ReadingDiaryImageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, List<DiaryImageResponse>> findImagesByDiaryIds(List<Long> diaryIds) {
        return queryFactory
                .select(new QDiaryImageResponse(
                        readingDiaryImage.diary.id,
                        readingDiaryImage.id,
                        readingDiaryImage.imageUrl,
                        readingDiaryImage.sequence
                ))
                .from(readingDiaryImage)
                .where(readingDiaryImage.diary.id.in(diaryIds))
                .orderBy(readingDiaryImage.sequence.asc())
                .fetch()
                .stream()
                .collect(groupingBy(DiaryImageResponse::getDiaryId));
    }
} 

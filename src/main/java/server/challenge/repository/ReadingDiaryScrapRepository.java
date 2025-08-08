package server.challenge.repository;

import server.challenge.domain.ReadingDiary;
import server.challenge.domain.ReadingDiaryScrap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingDiaryScrapRepository extends JpaRepository<ReadingDiaryScrap, Long>,
        ReadingDiaryScrapRepositoryCustom {


    void deleteByMemberIdAndReadingDiaryId(Long memberId, Long readingDiaryId);

    void deleteAllByReadingDiary(ReadingDiary readingDiary);

    void deleteAllByMemberId(Long memberId);
}

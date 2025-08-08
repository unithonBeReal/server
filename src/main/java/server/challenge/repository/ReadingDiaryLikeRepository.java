package server.challenge.repository;

import server.challenge.domain.ReadingDiaryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import server.challenge.domain.ReadingDiary;

public interface ReadingDiaryLikeRepository extends JpaRepository<ReadingDiaryLike, Long>, ReadingDiaryLikeRepositoryCustom {

    void deleteByMemberIdAndReadingDiaryId(Long memberId, Long readingDiaryId);

    void deleteAllByMemberId(Long memberId);

    void deleteAllByReadingDiary(ReadingDiary readingDiary);
}


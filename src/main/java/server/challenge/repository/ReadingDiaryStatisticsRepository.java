package server.challenge.repository;

import server.challenge.domain.ReadingDiaryStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReadingDiaryStatisticsRepository extends JpaRepository<ReadingDiaryStatistic, Long> {

    List<ReadingDiaryStatistic> findAllByReadingDiaryIdIn(Collection<Long> diaryIds);

    @Modifying
    @Query("delete from ReadingDiaryStatistic rds where rds.readingDiary.id in (select rd.id from ReadingDiary rd where rd.member.id = :memberId)")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
} 

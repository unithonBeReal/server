package server.challenge.repository;

import server.challenge.domain.ReadingChallenge;
import server.challenge.domain.ReadingProgress;
import server.common.CustomException;
import server.common.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {

    void deleteAllByReadingChallenge(ReadingChallenge challenge);

    @Modifying
    @Query("delete from ReadingProgress rp where rp.readingChallenge.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    default ReadingProgress findByIdOrElseThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROGRESS_NOT_FOUND));
    }
}

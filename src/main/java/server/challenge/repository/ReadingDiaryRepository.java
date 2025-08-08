package server.challenge.repository;

import server.challenge.domain.ReadingDiary;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReadingDiaryRepository extends JpaRepository<ReadingDiary, Long>, ReadingDiaryRepositoryCustom {


    void deleteAllByMemberId(Long memberId);

    default ReadingDiary findByIdOrElseThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    }

    long countByMember(Member member);

    List<ReadingDiary> findAllByMemberId(Long memberId);
}

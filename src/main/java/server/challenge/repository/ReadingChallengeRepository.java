package server.challenge.repository;

import server.book.entity.Book;
import server.challenge.domain.ReadingChallenge;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingChallengeRepository extends JpaRepository<ReadingChallenge, Long> {

    boolean existsByMemberAndBook(Member member, Book book);

    List<ReadingChallenge> findByMemberOrderByCreatedDateDesc(Member member);

    Optional<ReadingChallenge> findByMemberAndBookAndCompletedFalseAndAbandonedFalse(Member member, Book book);

    Optional<ReadingChallenge> findTopByMemberAndBookOrderByIdDesc(Member member, Book book);

    List<ReadingChallenge> findByMemberAndCompletedFalseAndAbandonedFalse(Member member);

    List<ReadingChallenge> findByMemberAndAbandonedTrue(Member member);

    List<ReadingChallenge> findByMemberAndCompletedTrue(Member member);

    void deleteAllByMemberId(Long memberId);

    default ReadingChallenge findByIdOrElseThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    }
}

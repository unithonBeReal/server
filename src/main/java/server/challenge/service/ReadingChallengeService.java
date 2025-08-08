package server.challenge.service;

import server.book.entity.Book;
import server.book.repository.BookRepository;
import server.challenge.domain.ReadingChallenge;
import server.challenge.domain.ReadingProgress;
import server.challenge.dto.ChallengeRequest;
import server.challenge.dto.ProgressRequest;
import server.challenge.dto.response.ChallengeProgressResponse;
import server.challenge.dto.response.ChallengeResponse;
import server.challenge.repository.ReadingChallengeRepository;
import server.challenge.repository.ReadingProgressRepository;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ReadingChallengeService {

    private final ReadingChallengeRepository challengeRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getChallenges(Long memberId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        return challengeRepository.findByMemberOrderByCreatedDateDesc(member).stream()
                .map(ChallengeResponse::from)
                .toList();
    }

    @Transactional
    public ChallengeResponse.CreationResponse createChallenge(Long memberId, ChallengeRequest request) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        Book book = bookRepository.findByIdOrElseThrow(request.getBookId());

        validateNoDuplicateChallenge(member, book);

        ReadingChallenge challenge = createAndSaveNewChallenge(member, book, request.getTotalPages());

        ReadingProgress progress = createAndSaveProgress(challenge, request.getStartPage(), request.getEndPage());

        return ChallengeResponse.CreationResponse.of(challenge, progress);
    }

    @Transactional
    public ChallengeProgressResponse addProgress(Long memberId, Long challengeId, ProgressRequest request) {
        ReadingChallenge challenge = challengeRepository.findByIdOrElseThrow(challengeId);
        challenge.validateOwner(memberId);

        ReadingProgress progress = createAndSaveProgress(challenge, request.getStartPage(), request.getEndPage());
        return ChallengeProgressResponse.of(progress.getId());
    }


    private ReadingChallenge createAndSaveNewChallenge(Member member, Book book, int totalPages) {
        ReadingChallenge challenge = ReadingChallenge.builder()
                .member(member)
                .book(book)
                .totalPages(totalPages)
                .build();
        return challengeRepository.save(challenge);
    }

    private ReadingProgress createAndSaveProgress(ReadingChallenge challenge, int startPage, int endPage) {
        ReadingProgress progress = ReadingProgress.builder()
                .readingChallenge(challenge)
                .startPage(startPage)
                .endPage(endPage)
                .build();
        challenge.updateProgress(endPage);
        return readingProgressRepository.save(progress);
    }

    @Transactional
    public void deleteChallenge(Long memberId, Long challengeId) {
        ReadingChallenge challenge = challengeRepository.findByIdOrElseThrow(challengeId);
        challenge.validateOwner(memberId);

        readingProgressRepository.deleteAllByReadingChallenge(challenge);
        challengeRepository.delete(challenge);
    }

    @Transactional
    public void abandonChallenge(Long memberId, Long challengeId) {
        ReadingChallenge challenge = challengeRepository.findByIdOrElseThrow(challengeId);
        challenge.validateOwner(memberId);
        challenge.abandon();
    }

    @Transactional
    public ChallengeResponse restartChallenge(Long memberId, Long challengeId) {
        ReadingChallenge challenge = challengeRepository.findByIdOrElseThrow(challengeId);
        challenge.validateOwner(memberId);
        challenge.restart();

        return ChallengeResponse.from(challenge);
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getOngoingChallenges(Long memberId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        return challengeRepository.findByMemberAndCompletedFalseAndAbandonedFalse(member).stream()
                .map(ChallengeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getAbandonedChallenges(Long memberId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        return challengeRepository.findByMemberAndAbandonedTrue(member).stream()
                .map(ChallengeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getCompletedChallenges(Long memberId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        return challengeRepository.findByMemberAndCompletedTrue(member).stream()
                .map(ChallengeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChallengeResponse.Detail getChallengeByBookId(Long memberId, Long bookId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        Book book = bookRepository.findByIdOrElseThrow(bookId);

        ReadingChallenge challenge = challengeRepository.findTopByMemberAndBookOrderByIdDesc(member, book)
                .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

        return ChallengeResponse.Detail.of(challenge);
    }

    @Transactional(readOnly = true)
    public boolean checkChallengeExists(Long memberId, Long bookId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        Book book = bookRepository.findByIdOrElseThrow(bookId);
        return challengeRepository.existsByMemberAndBook(member, book);
    }

    private void validateNoDuplicateChallenge(Member member, Book book) {
        if (challengeRepository.findByMemberAndBookAndCompletedFalseAndAbandonedFalse(member, book).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_CHALLENGE);
        }
    }
} 

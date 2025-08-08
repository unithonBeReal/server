package server.challenge.service;

import server.challenge.domain.ReadingDiary;
import server.challenge.domain.ReadingDiaryLike;
import server.challenge.dto.response.DiaryResponse;
import server.challenge.event.dto.LikeEvent;
import server.challenge.repository.ReadingDiaryLikeRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.common.CustomException;
import server.common.ErrorCode;
import server.common.response.CursorPageResponse;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadingDiaryLikeService {


    private final MemberRepository memberRepository;
    private final ReadingDiaryLikeRepository readingDiaryLikeRepository;
    private final ReadingDiaryRepository readingDiaryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DiaryDetailCombiner diaryDetailCombiner;

    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.LikedDiaryThumbnail> findLikedDiaryThumbnails(Long memberId, Long cursorId,
                                                                                          int pageSize) {
        List<DiaryResponse.LikedDiaryThumbnail> thumbnails = readingDiaryLikeRepository.findLikedDiariesThumbnail(
                memberId, cursorId, pageSize);
        return CursorPageResponse.of(thumbnails, pageSize, DiaryResponse.LikedDiaryThumbnail::diaryLikeId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.LikedDiaryFeed> findLikedDiaryFeeds(Long memberId, Long cursorId,
                                                                                int pageSize) {
        List<DiaryResponse.LikedDiaryFeed> feeds = readingDiaryLikeRepository.findLikedDiaryFeeds(memberId, cursorId,
                pageSize + 1);
        List<DiaryResponse.LikedDiaryFeed> combinedFeeds = (List<DiaryResponse.LikedDiaryFeed>) diaryDetailCombiner
                .combine(feeds);

        return CursorPageResponse.of(combinedFeeds, pageSize, DiaryResponse.LikedDiaryFeed::getDiaryLikeId);
    }

    @Transactional
    public void createDiaryLike(Long memberId, Long diaryId) {
        final Member member = memberRepository.findByIdOrElseThrow(memberId);
        final ReadingDiary readingDiary = readingDiaryRepository.findByIdOrElseThrow(diaryId);

        try {
            readingDiaryLikeRepository.save(new ReadingDiaryLike(member, readingDiary));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.ALREADY_LIKED_DIARY);
        }

        eventPublisher.publishEvent(
                new LikeEvent(diaryId, readingDiary.getMember().getId(), readingDiary.getBook().getId(),
                        memberId, 1));
    }

    @Transactional
    public void deleteDiaryLike(Long memberId, Long diaryId) {
        final ReadingDiary readingDiary = readingDiaryRepository.findByIdOrElseThrow(diaryId);
        readingDiaryLikeRepository.deleteByMemberIdAndReadingDiaryId(memberId, diaryId);
        eventPublisher.publishEvent(
                new LikeEvent(diaryId, readingDiary.getMember().getId(), readingDiary.getBook().getId(),
                        memberId, -1));
    }

} 

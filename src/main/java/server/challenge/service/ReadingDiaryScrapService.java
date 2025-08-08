package server.challenge.service;

import server.challenge.domain.ReadingDiary;
import server.challenge.domain.ReadingDiaryScrap;
import server.challenge.dto.response.DiaryResponse;
import server.challenge.repository.ReadingDiaryRepository;
import server.challenge.repository.ReadingDiaryScrapRepository;
import server.common.CustomException;
import server.common.ErrorCode;
import server.common.response.CursorPageResponse;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadingDiaryScrapService {

    private final ReadingDiaryScrapRepository readingDiaryScrapRepository;
    private final MemberRepository memberRepository;
    private final ReadingDiaryRepository readingDiaryRepository;
    private final DiaryDetailCombiner diaryDetailCombiner;

    @Transactional
    public void createScrap(Long memberId, Long diaryId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        ReadingDiary readingDiary = readingDiaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

        try {
            readingDiaryScrapRepository.save(new ReadingDiaryScrap(member, readingDiary));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.ALREADY_SCRAPED_DIARY);
        }
    }

    @Transactional
    public void deleteScrap(Long memberId, Long diaryId) {
        readingDiaryScrapRepository.deleteByMemberIdAndReadingDiaryId(memberId, diaryId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.ScrapedDiaryThumbnail> findScrapedDiaryThumbnails(Long memberId, Long cursorId,
                                                                                       int pageSize) {
        List<DiaryResponse.ScrapedDiaryThumbnail> thumbnails = readingDiaryScrapRepository.findScrapedDiariesThumbnail(
                memberId, cursorId, pageSize+1);
        return CursorPageResponse.of(thumbnails, pageSize, DiaryResponse.ScrapedDiaryThumbnail::scrapId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.ScrapedDiaryFeed> findScrapedDiaryFeeds(Long memberId, Long cursorId,
                                                                             int pageSize) {
        List<DiaryResponse.ScrapedDiaryFeed> feeds = readingDiaryScrapRepository.findScrapedDiaryFeeds(memberId, cursorId,
                pageSize + 1);
        
        List<DiaryResponse.ScrapedDiaryFeed> combinedFeeds = (List<DiaryResponse.ScrapedDiaryFeed>) diaryDetailCombiner.combine(
                feeds);

        return CursorPageResponse.of(combinedFeeds, pageSize, DiaryResponse.ScrapedDiaryFeed::getScrapId);
    }
} 

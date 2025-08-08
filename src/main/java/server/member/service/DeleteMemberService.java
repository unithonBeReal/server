package server.member.service;

import server.book.repository.BookLikeRepository;
import server.book.repository.BookRatingRepository;
import server.challenge.domain.ReadingDiary;
import book.book.challenge.repository.*;
import server.challenge.repository.DiaryCommentRepository;
import server.challenge.repository.ReadingChallengeRepository;
import server.challenge.repository.ReadingDiaryLikeRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.challenge.repository.ReadingDiaryScrapRepository;
import server.challenge.repository.ReadingDiaryStatisticsRepository;
import server.challenge.repository.ReadingProgressRepository;
import server.challenge.service.ReadingDiaryService;
import server.chat.repository.ChatMessageRepository;
import server.chat.repository.MemberChatRoomRepository;
import server.follow.repository.FollowRepository;
import server.member.repository.MemberRepository;
import server.member.repository.PolicyRepository;
import server.notification.repository.NotificationRepository;
import server.report.repository.ReportRepository;
import server.search.repository.SearchHistoryRepository;
import server.security.auth.dao.RefreshTokenRepository;
import server.timer.repository.TimerLogRepository;
import server.youtube.repository.MemberPreferenceRepository;
import server.youtube.repository.UserVideoHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteMemberService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ReadingChallengeRepository readingChallengeRepository;
    private final ReadingDiaryRepository readingDiaryRepository;
    private final DiaryCommentRepository diaryCommentRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final ReadingDiaryStatisticsRepository readingDiaryStatisticsRepository;
    private final ReadingDiaryLikeRepository readingDiaryLikeRepository;
    private final BookLikeRepository bookLikeRepository;
    private final BookRatingRepository bookRatingRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final TimerLogRepository timerLogRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberChatRoomRepository memberChatRoomRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final ReadingDiaryScrapRepository readingDiaryScrapRepository;
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final UserVideoHistoryRepository userVideoHistoryRepository;
    private final PolicyRepository policyRepository;
    private final ReadingDiaryService readingDiaryService;

    @Transactional
    public void deleteMember(Long memberId) {
        // 챌린지/일기 관련 데이터 삭제 (외래 키 종속성 역순으로 삭제)
        reportRepository.deleteAllByReporterId(memberId);
        readingDiaryLikeRepository.deleteAllByMemberId(memberId);
        readingDiaryScrapRepository.deleteAllByMemberId(memberId);
        diaryCommentRepository.deleteAllByMemberId(memberId);
        List<ReadingDiary> readingDiaries = readingDiaryRepository.findAllByMemberId(memberId);
        readingDiaries.forEach(diary -> readingDiaryService.deleteDiary(memberId, diary.getId()));
        readingProgressRepository.deleteAllByMemberId(memberId);
        readingChallengeRepository.deleteAllByMemberId(memberId);

        // 채팅 관련 데이터 삭제
        chatMessageRepository.deleteAllBySenderId(memberId);
        memberChatRoomRepository.deleteAllByMemberId(memberId);

        // 좋아요, 별점, 검색 기록 삭제
        bookLikeRepository.deleteAllByMemberId(memberId);
        bookRatingRepository.deleteAllByMemberId(memberId);
        searchHistoryRepository.deleteAllByMemberId(memberId);

        // 취향도 삭제
        memberPreferenceRepository.deleteAllByMemberId(memberId);

        // 팔로우 관계 삭제
        followRepository.deleteAllByMemberId(memberId);

        // 알림 데이터 삭제
        notificationRepository.deleteAllByMemberId(memberId);

        // 타이머 기록 삭제
        timerLogRepository.deleteAllByMemberId(memberId);

        // 인증 관련 데이터 삭제 (리프레시 토큰)
        refreshTokenRepository.deleteByMemberId(memberId);

        // 유튜브 기록 삭제
        userVideoHistoryRepository.deleteByMemberId(memberId);

        // 동의약관 삭제
        policyRepository.deleteByMemberId(memberId);

        // 마지막으로 회원 정보 삭제
        memberRepository.deleteById(memberId);
    }

}

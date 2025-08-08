package server.challenge.api;

import server.challenge.dto.ChallengeRequest;
import server.challenge.dto.ProgressRequest;
import server.challenge.dto.response.ChallengeProgressResponse;
import server.challenge.dto.response.ChallengeResponse;
import server.challenge.service.ReadingChallengeService;
import server.common.response.ResponseForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "reading challenge", description = "독서 챌린지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/reading-challenges")
public class ReadingChallengeController {

    private final ReadingChallengeService challengeService;

    @Operation(summary = "새로운 독서 챌린지 생성")
    @PostMapping
    public ResponseForm<ChallengeResponse.CreationResponse> createChallenge(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid ChallengeRequest request) {
        return new ResponseForm<>(challengeService.createChallenge(memberId, request));
    }

    @PostMapping("/{challengeId}/progress")
    @Operation(summary = "챌린지 진행률 업데이트", description = "특정 챌린지의 진행률을 기록합니다.")
    public ResponseForm<ChallengeProgressResponse> addProgress(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long challengeId,
            @RequestBody @Valid ProgressRequest request) {
        return new ResponseForm<>(challengeService.addProgress(memberId, challengeId, request));
    }

    @Operation(summary = "책 ID로 독서 챌린지 상세 조회")
    @GetMapping("/books/{bookId}")
    public ResponseForm<ChallengeResponse.Detail> getChallengeByBookId(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long bookId) {
        return new ResponseForm<>(challengeService.getChallengeByBookId(memberId, bookId));
    }

    @Operation(summary = "책 ID로 사용자의 독서 챌린지 존재 여부 확인")
    @GetMapping("/exists/books/{bookId}")
    public ResponseForm<Boolean> checkChallengeExists(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long bookId) {
        return new ResponseForm<>(challengeService.checkChallengeExists(memberId, bookId));
    }

    @Operation(summary = "사용자의 모든 독서 챌린지 목록 조회")
    @GetMapping("/members/{memberId}")
    public ResponseForm<List<ChallengeResponse>> getChallenges(
            @PathVariable Long memberId) {
        return new ResponseForm<>(challengeService.getChallenges(memberId));
    }

    @Operation(summary = "현재 진행 중인 챌린지 목록 조회")
    @GetMapping("/ongoing")
    public ResponseForm<List<ChallengeResponse>> getOngoingChallenges(
            @AuthenticationPrincipal Long memberId) {
        return new ResponseForm<>(challengeService.getOngoingChallenges(memberId));
    }

    @Operation(summary = "중단된 챌린지 목록 조회")
    @GetMapping("/abandoned")
    public ResponseForm<List<ChallengeResponse>> getAbandonedChallenges(
            @AuthenticationPrincipal Long memberId) {
        return new ResponseForm<>(challengeService.getAbandonedChallenges(memberId));
    }

    @Operation(summary = "완료된 챌린지 목록 조회")
    @GetMapping("/completed")
    public ResponseForm<List<ChallengeResponse>> getCompletedChallenges(
            @AuthenticationPrincipal Long memberId) {
        return new ResponseForm<>(challengeService.getCompletedChallenges(memberId));
    }

    @Operation(summary = "독서 챌린지 중단")
    @PostMapping("/{challengeId}/abandon")
    public ResponseForm<Void> abandonChallenge(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long challengeId) {
        challengeService.abandonChallenge(memberId, challengeId);
        return ResponseForm.ok();
    }

    @Operation(summary = "독서 챌린지 재시작")
    @PostMapping("/{challengeId}/restart")
    public ResponseForm<ChallengeResponse> restartChallenge(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long challengeId) {
        return new ResponseForm<>(challengeService.restartChallenge(memberId, challengeId));
    }

    @Operation(summary = "독서 챌린지 삭제")
    @DeleteMapping("/{challengeId}")
    public ResponseForm<Void> deleteChallenge(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long challengeId) {
        challengeService.deleteChallenge(memberId, challengeId);
        return ResponseForm.ok();
    }
} 

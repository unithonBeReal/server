package server.challenge.dto.response;

public record ChallengeProgressResponse(
        Long progressId
) {
    public static ChallengeProgressResponse of(Long progressId) {
        return new ChallengeProgressResponse(progressId);
    }
} 

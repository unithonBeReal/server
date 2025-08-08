package server.member.dto;

import server.member.entity.Member;

public record ProfileResponse(
        Long memberId,
        String nickName,
        String profileImageUrl,
        String introduction
) {

    public static ProfileResponse from(Member member) {
        return new ProfileResponse(
                member.getId(),
                member.getNickName(),
                member.getProfileImage(),
                member.getIntroduction()
        );
    }

    public record WithCounts(
            Long memberId,
            String nickName,
            String profileImageUrl,
            String introduction,
            long followingCount,
            long followerCount,
            long diaryCount
    ) {
        public static WithCounts of(Member member, long followingCount, long followerCount, long diaryCount) {
            return new WithCounts(
                    member.getId(),
                    member.getNickName(),
                    member.getProfileImage(),
                    member.getIntroduction(),
                    followingCount,
                    followerCount,
                    diaryCount
            );
        }
    }
} 

package server.member.dto;

public record UpdateProfileRequest(
        String nickName,
        String profileImageUrl,
        String introduction
) {
} 

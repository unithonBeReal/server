package server.youtube.dto;

import server.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberCategoryCountDto {
    private Member member;
    private String categoryName;
    private Long count;
} 

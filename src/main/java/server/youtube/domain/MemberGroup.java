package server.youtube.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum MemberGroup {
    HUMANITIES("인문학", List.of("인문학 책", "철학 책 추천", "고전 인문")),
    NOVEL("소설", List.of("소설 추천", "베스트셀러 소설", "문학작품 리뷰")),
    ECONOMY_SELF_HELP("경제/자기계발", List.of("경제경영 베스트셀러", "돈 공부", "자기계발 책")),
    SCIENCE("과학", List.of("과학책 추천", "SF 소설", "교양 과학"));

    private final String description;
    private final List<String> keywords;

    public static List<String> getAllPersonalizedKeywords() {
        return Arrays.stream(MemberGroup.values())
                .flatMap(group -> group.getKeywords().stream())
                .toList();
    }
} 

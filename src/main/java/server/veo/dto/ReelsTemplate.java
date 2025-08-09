package server.veo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 인기 릴스 템플릿 정보
 *
 * 미리 준비된 인기 릴스 스타일의 프롬프트 템플릿을 관리합니다.
 */
public record ReelsTemplate(
        Long id,

        @NotBlank(message = "템플릿 이름은 필수입니다")
        String name,

        @NotBlank(message = "프롬프트는 필수입니다")
        String promptTemplate,

        String description,
        String category, // 예: "trending", "aesthetic", "cooking", "close-up"
        Integer priority, // 우선순위 (낮을수록 먼저 선택됨)
        Boolean active,
        String[] tags // 검색용 태그들
) {
    /**
     * 기본값이 적용된 생성자
     */
    public ReelsTemplate {
        if (active == null) {
            active = true;
        }
        if (priority == null) {
            priority = 999;
        }
        if (tags == null) {
            tags = new String[0];
        }
    }

    /**
     * 프롬프트 템플릿에 음식점 정보를 적용하여 최종 프롬프트 생성
     */
    public String generatePrompt(String restaurantType, String foodType) {
        return promptTemplate
                .replace("{restaurant_type}", restaurantType)
                .replace("{food_type}", foodType);
    }

    /**
     * 새로운 템플릿 생성을 위한 정적 팩토리 메서드
     */
    public static ReelsTemplate create(String name, String promptTemplate, String description, String category) {
        return new ReelsTemplate(null, name, promptTemplate, description, category, null, null, null);
    }
} 

package server.chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatCategory {
    LITERATURE("문학"),
    HUMANITIES_SOCIETY("인문사회"),
    SCIENCE_TECHNOLOGY("과학기술"),
    ART_HOBBY("예술취미"),
    CHILDREN_YOUTH("아동청소년"),
    BESTSELLER("베스트셀러");

    private final String description;
} 

package server.challenge.dto;

import static book.book.challenge.domain.QReadingDiary.readingDiary;
import static book.book.challenge.domain.QReadingDiaryStatistic.readingDiaryStatistic;

import com.querydsl.core.types.OrderSpecifier;

public enum RelatedDiarySort {
    POPULAR {
        @Override
        public OrderSpecifier<?>[] getOrderSpecifiers() {
            return new OrderSpecifier[]{
                    readingDiaryStatistic.popularityScore.desc(),
                    readingDiary.id.desc()
            };
        }
    },
    LATEST {
        @Override
        public OrderSpecifier<?>[] getOrderSpecifiers() {
            return new OrderSpecifier[]{
                    readingDiary.id.desc()
            };
        }
    };

    public abstract OrderSpecifier<?>[] getOrderSpecifiers();
} 

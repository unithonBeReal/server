package server.common.response;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 이중 커서 기반 페이지네이션 응답을 위한 범용 클래스입니다.
 * 이 클래스는 ID 외에 점수, 날짜 등 추가적인 정렬 기준이 필요한 경우 사용됩니다.
 *
 * @param <T> 응답 데이터의 리스트를 구성하는 요소의 타입
 * @param <S> ID 외의 부가적인 정렬 기준으로 사용될 커서의 타입
 */
@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DualCursorPageResponse<T, S> {

    private final List<T> data;
    private final Long nextCursor;
    private final S nextSubCursor;
    private final Boolean hasNext;

    /**
     * Repository에서 pageSize + 1만큼 조회한 데이터 리스트를 받아,
     * 단일 ID 커서를 사용하는 다음 페이지 정보를 계산하여 응답 객체를 생성합니다.
     * nextSubCursor는 null로 설정됩니다.
     *
     * @param data        Repository에서 조회된 데이터 리스트 (pageSize + 1 크기)
     * @param pageSize    클라이언트에게 반환할 실제 페이지 크기
     * @param idExtractor 데이터 요소에서 Long 타입의 ID(커서)를 추출하는 함수
     * @param <T>         데이터 요소의 타입
     * @return 계산된 페이지네이션 정보가 포함된 응답 객체
     */
    public static <T> DualCursorPageResponse<T, Void> of(
            List<T> data,
            int pageSize,
            ToLongFunction<T> idExtractor
    ) {
        boolean hasNext = data.size() > pageSize;
        long nextCursor = -1L;

        if (hasNext) {
            T lastElement = data.get(pageSize - 1);
            nextCursor = idExtractor.applyAsLong(lastElement);
            data = data.subList(0, pageSize);
        }

        return new DualCursorPageResponse<>(data, nextCursor, null, hasNext);
    }

    /**
     * Repository에서 pageSize + 1만큼 조회한 데이터 리스트를 받아,
     * ID와 부가적인 정렬 기준(sub-cursor)을 사용하는 이중 커서 기반의
     * 다음 페이지 정보를 계산하여 응답 객체를 생성합니다.
     *
     * @param data             Repository에서 조회된 데이터 리스트 (pageSize + 1 크기)
     * @param pageSize         클라이언트에게 반환할 실제 페이지 크기
     * @param idExtractor      데이터 요소에서 Long 타입의 주(main) 커서(ID)를 추출하는 함수
     * @param subCursorExtractor 데이터 요소에서 sub 커서를 추출하는 함수
     * @param <T>              데이터 요소의 타입
     * @param <S>              부가 커서의 타입
     * @return 계산된 페이지네이션 정보가 포함된 응답 객체
     */
    public static <T, S> DualCursorPageResponse<T, S> of(
            List<T> data,
            int pageSize,
            ToLongFunction<T> idExtractor,
            Function<T, S> subCursorExtractor
    ) {
        boolean hasNext = data.size() > pageSize;
        long nextCursor = -1L;
        S nextSubCursor = null;

        if (hasNext) {
            T lastElement = data.get(pageSize - 1);
            nextCursor = idExtractor.applyAsLong(lastElement);
            nextSubCursor = subCursorExtractor.apply(lastElement);
            data = data.subList(0, pageSize);
        }

        return new DualCursorPageResponse<>(data, nextCursor, nextSubCursor, hasNext);
    }
} 

package server.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


/**
 * 클라이언트에게 반환할 에러코드
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 정상 처리
    OK("B000", "요청 정상 처리", HttpStatus.OK),

    // 서버 내부 에러 (5xx 에러)
    INTERNAL_SERVER_ERROR("B100", "서버 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR),
    CONCURRENCY_ERROR("B101", "동시성 에러 발생", HttpStatus.CONFLICT),

    // B2xx: JSon 값 예외
    NOT_VALIDATION("B200", "json 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // B3xx: 인증, 권한에 대한 예외
    AUTH_MEMBER_NOT("B300", "현재 권한으로 접근 불가능합니다.", HttpStatus.FORBIDDEN),
    JWT_DATE_NOT("B301", "JWT토큰이 만료되었습니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_OUTDATED("B302", "새로 발급된 토큰보다 이전의 리프레시 토큰입니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_FOUND("B303", "해당 맴버는 디비에 리프레쉬 토큰을 저장하고 있지 않습니다.", HttpStatus.NOT_FOUND),
    APPLE_FAILED_TO_GET_TOKEN("B304", "애플 토큰을 가져오는데 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_FAILED_TO_GET_USER_INFO("B305", "애플id토큰에서 유저 정보를 가져오는데 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_INVALID_PRIVATE_KEY("B306", "애플 프라이브키의 형식이 올바르지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_REFRESH_TOKEN_NOT_FOUND("B307", "(애플 리프레쉬 토큰을 찾지 못하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_FAILED_TO_REVOKE_TOKEN("B308", "애플리프레쉬 토큰 만료를 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_INVALID_REQUEST("B309", "애플 토큰 요청 중 잘못된 요청입니다. (파라미터 누락 또는 잘못된 파라미터)", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_INVALID_CLIENT("B310", "애플 토큰 요청 중 클라이언트 인증 실패 (client_id나 client_secret이 잘못됨)",
            HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_INVALID_GRANT("B311", "애플 토큰 요청 중 인증 코드가 만료되었거나 이미 사용됨", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_UNAUTHORIZED_CLIENT("B312", "애플 토큰 요청 중 클라이언트가 이 인증 방식을 사용할 권한이 없음", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_UNSUPPORTED_GRANT_TYPE("B313", "애플 토큰 요청 중 지원하지 않는 인증 방식", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLE_INVALID_SCOPE("B314", "애플 토큰 요청 중 잘못된 scope 요청", HttpStatus.INTERNAL_SERVER_ERROR),
    KAKAO_TOKEN_GET_FAILED("B315", "카카오 토큰을 가져오는데 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_NOT_FOUND("B316", "토큰이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_MISMATCH("B317", "디비에 저장되어있는 리프레쉬 토큰과 다른 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    JWT_INVALID_TOKEN("B315", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    JWT_EXPIRED("B315", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    JWT_UNSUPPORTED("B315", "지원하지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED),
    JWT_CLAIMS_EMPTY("B315", "토큰에 클레임이 비어있습니다.", HttpStatus.UNAUTHORIZED),
    GOOGLE_IDTOKEN_VALIDATION_FAIL("B316", "유효하지 않은 구굴 idToken 입니다.", HttpStatus.UNAUTHORIZED),

    // B4xx: 유저 예외
    MEMBER_NOT_FOUND("B400", "존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND),
    IS_NOT_OWNER("B401", "권한이 없습니다.(주인이 아닙니다)", HttpStatus.BAD_REQUEST),
    KAKAO_USER_INFO_FAILED("B402", "카카오 유저 정보 조회를 실패하였습니다.", HttpStatus.BAD_REQUEST),

    // B5xx: book 예외
    BOOK_NOT_FOUND("B500", "존재하지 않는 책입니다.", HttpStatus.NOT_FOUND),
    READING_STATUS_RATING_REQUIRED("B501", "READED상태는 평점이 필수입니다.", HttpStatus.BAD_REQUEST),
    ALREADY_LIKED_BOOK("B502", "이미 좋아요를 누른 책입니다.", HttpStatus.CONFLICT),
    RATING_NOT_FOUND("B503", "평점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // B6xx: 평가 예외
    MEMBERBOOK_NOT_FOUND("B600", "존재하지 않는 유저책입니다.", HttpStatus.NOT_FOUND),

    // B7xx: Collection 예외
    COLLECTION_NOT_FOUND("B700", "존재하지 않는 북콜렉션입니다.", HttpStatus.NOT_FOUND),
    BOOKCOLLECTION_ALREADY("B701", "나의 북콜렉션 중 같은 이름의 북콜렉션이 존재합니다.", HttpStatus.BAD_REQUEST),
    COLLECTION_BOOK_IS_NOT_IN_COLLECTION("B702", "컬렉션에 존재하지 않은 컬렉션북입니다.", HttpStatus.BAD_REQUEST),
    COLLECTION_BOOK_NOT_FOUND("B703", "존재하지 않은 컬렉션 북입니다.", HttpStatus.NOT_FOUND),

    // B8xx: 알라딘 API 관련 에러
    ALADIN_API_ERROR("B800", "알라딘 API 호출 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ALADIN_SEARCH_ERROR("B801", "알라딘 도서 검색 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ALADIN_ITEM_LOOKUP_ERROR("B802", "알라딘 도서 상세 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ALADIN_NETWORK_ERROR("B803", "알라딘 API 서버와의 통신 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    ALADIN_INVALID_RESPONSE("B804", "알라딘 API로부터 유효하지 않은 응답을 받았습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ALADIN_EXTERNAL_API_ERROR("B805", "알라딘 API 응답 파싱 실패", HttpStatus.INTERNAL_SERVER_ERROR),

    //B9xx: 알림 관련 예외
    NOT_FOUND_FCMTOKEN("B900", "본 유저의 fcm토큰이 저장되어 있지 않습니다.", HttpStatus.NOT_FOUND),

    // B10xx: follow 예외
    FOLLOW_AREADY_EXIST("B1001", "이미 팔로우 한 유저입니다.", HttpStatus.CONFLICT),
    UNFOLLOW_MYSELF_FAIL("B1002", "내 자신을 팔로잉할 수는 없습니다.", HttpStatus.BAD_REQUEST),
    UNFOLLOW_FAIL("B1003", "팔로우 취소 실패", HttpStatus.BAD_REQUEST),
    FOLLOWER_DELETE_FAIL("B1004", "팔로워 취소 실패", HttpStatus.BAD_REQUEST),

    // B11xx: 기타 예외
    PARE_DATE_STRING_ERROR("B1101", "String값을 Date로 파싱도중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_PARSE_FAIL("B1102", "JWT 파싱도중 실패하였습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    // B12xx: Reading Challenge 예외
    CHALLENGE_NOT_FOUND("B1200", "존재하지 않는 독서 챌린지입니다.", HttpStatus.NOT_FOUND),
    NOT_CHALLENGE_OWNER("B1201", "독서 챌린지의 소유자가 아닙니다.", HttpStatus.FORBIDDEN),
    CANNOT_UPDATE_FINISHED_CHALLENGE("B1202", "완료되거나 포기한 챌린지는 수정할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_NUMBER("B1203", "페이지 번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    CANNOT_RATE_UNFINISHED_CHALLENGE("B1204", "완료되지 않은 챌린지는 평가할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_RATING_RANGE("B1205", "별점은 0.5점에서 5점 사이여야 합니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_CHALLENGE("B1206", "이미 진행 중인 챌린지가 있습니다.", HttpStatus.CONFLICT),
    CANNOT_RESTART_COMPLETED_CHALLENGE("B1207", "완료된 챌린지는 재시작할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CHALLENGE_NOT_ABANDONED("B1208", "중단된 챌린지만 재시작할 수 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_RATING_UNIT("B1209", "별점은 0.5 단위로만 입력할 수 있습니다.", HttpStatus.BAD_REQUEST),

    // B13xx: Reading Diary 예외
    DIARY_NOT_FOUND("B1300", "존재하지 않는 독서 일기입니다.", HttpStatus.NOT_FOUND),
    PROGRESS_NOT_FOUND("B1301", "존재하지 않는 독서 진행률 기록입니다.", HttpStatus.NOT_FOUND),
    NO_AUTHORITY_TO_DIARY("B1302", "본인의 독서 일기가 아닙니다.", HttpStatus.UNAUTHORIZED),
    ALREADY_LIKED_DIARY("B1303", "이미 좋아요를 누른 독서일지입니다.", HttpStatus.CONFLICT),
    ALREADY_SCRAPED_DIARY("B1304", "이미 스크랩한 독서일지입니다.", HttpStatus.CONFLICT),


    // B14xx: Chat 예외
    CHAT_ROOM_NOT_FOUND("B1400", "존재하지 않는 채팅방입니다.", HttpStatus.NOT_FOUND),
    ALREADY_IN_CHAT_ROOM("B1401", "이미 참여하고 있는 채팅방입니다.", HttpStatus.CONFLICT),

    // B15xx: Diary 예외
    COMMENT_NOT_FOUND("B1501", "댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PARENT_COMMENT("D004", "부모 댓글이 해당 독서일기에 속하지 않습니다.", HttpStatus.BAD_REQUEST),
    FORBIDDEN_MEMBER_ACCESS_COMMENT("B1503", "댓글 접근 권한이 없는 회원입니다.", HttpStatus.FORBIDDEN),

    // B16xx: Timer 예외
    TIMER_LOG_CREATION_CONFLICT("B1600", "타이머 기록 생성 중 충돌이 발생했습니다.", HttpStatus.CONFLICT),

    // B17xx: Youtube 예외
    YOUTUBE_VIDEO_NOT_FOUND("B1700", "해당 유튜브 영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    YOUTUBE_API_QUOTA_EXCEEDED("B1701", "YouTube API 할당량을 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS),
    RECOMMENDATION_RATE_LIMIT_EXCEEDED("B1702", "하루 추천 요청 횟수를 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS),
    YOUTUBE_API_ERROR("B1703", "YouTube API 호출 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    YOUTUBE_API_UNEXPECTED_ERROR("B1704", "YouTube API 연동 중 예기치 않은 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    YOUTUBE_API_FORBIDDEN("B1705", "YouTube API에 대한 접근 권한이 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 외부 API
    DISCORD_NOTIFICATION_FAILED("B1800", "디스코드 알림 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpstatus;

}

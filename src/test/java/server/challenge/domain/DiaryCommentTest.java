package server.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import server.challenge.fixture.ReadingDiaryFixture;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DiaryCommentTest {

    private Member owner;
    private Member otherMember;
    private ReadingDiary diary;
    private ReadingDiary otherDiary;

    @BeforeEach
    void setUp() {
        owner = MemberFixture.create();
        otherMember = MemberFixture.create();
        diary = ReadingDiaryFixture.create();
        otherDiary = ReadingDiaryFixture.create();
    }

    @Nested
    @DisplayName("댓글 및 대댓글 생성")
    class CreateCommentAndReply {

        @Test
        @DisplayName("대댓글을 성공적으로 생성하고 부모-자식 관계를 맺는다")
        void createReply_Success() {
            // given
            DiaryComment parent = DiaryComment.createComment("부모 댓글", owner, diary);
            String replyContent = "테스트 대댓글입니다.";

            // when
            DiaryComment reply = DiaryComment.createReply(replyContent, otherMember, diary, parent);

            // then
            assertThat(reply.getParent()).isEqualTo(parent);
            assertThat(parent.getChildren()).containsExactly(reply);
        }
    }

    @Nested
    @DisplayName("소유권 및 유효성 검증")
    class Validation {

        @Test
        @DisplayName("소유자가 일치하면 예외가 발생하지 않는다")
        void validateOwner_Success() {
            // given
            DiaryComment comment = DiaryComment.createComment("내 댓글", owner, diary);

            // when & then
            comment.validateOwner(owner.getId());
        }

        @Test
        @DisplayName("소유자가 다르면 FORBIDDEN_MEMBER_ACCESS_COMMENT 예외를 던진다")
        void validateOwner_ThrowsException() {
            // given
            DiaryComment comment = DiaryComment.createComment("남의 댓글", otherMember, diary);

            // when & then
            assertThatThrownBy(() -> comment.validateOwner(owner.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_MEMBER_ACCESS_COMMENT);
        }

        @Test
        @DisplayName("부모 댓글이 올바른 다이어리에 속하면 예외가 발생하지 않는다")
        void validateDiary_Success() {
            // given
            DiaryComment parent = DiaryComment.createComment("부모 댓글", owner, diary);

            // when & then
            parent.validateDiary(diary); // No exception
        }

        @Test
        @DisplayName("부모 댓글이 다른 다이어리에 속하면 INVALID_PARENT_COMMENT 예외를 던진다")
        void validateDiary_ThrowsException() {
            // given
            DiaryComment parent = DiaryComment.createComment("부모 댓글", owner, diary);

            // when & then
            assertThatThrownBy(() -> parent.validateDiary(otherDiary))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PARENT_COMMENT);
        }
    }
} 

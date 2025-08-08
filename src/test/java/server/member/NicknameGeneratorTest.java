package server.member;

import server.member.util.NicknameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

class NicknameGeneratorTest {

    @Test
    @DisplayName("형용사 + 작가 랜덤 닉네임을 생성한다")
    void testRandomNicknameGeneration() {
        String nickname = NicknameGenerator.getRandomNickname();
        assertThat(nickname).isNotNull()
                .isNotEmpty()
                .contains(" "); // 형용사와 명사가 결합되었는지 확인
    }

    @RepeatedTest(10)
    @DisplayName("랜덤 닉네임이 다양한 조합으로 생성되는지 테스트")
    void testNicknameUniqueness() {
        Set<String> generatedNicknames = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            String nickname = NicknameGenerator.getRandomNickname();
            generatedNicknames.add(nickname);
        }

        assertThat(generatedNicknames.size()).isGreaterThan(10); // 최소 10개 이상의 닉네임이 다르게 생성되어야 함
    }

    @RepeatedTest(10)
    @DisplayName("닉네임은 11 이하여야 한다.")
    void test1() {
        String nickname = NicknameGenerator.getRandomNickname();
        assertThat(nickname.length()).isLessThan(12);
    }
}


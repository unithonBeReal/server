package server.member.fixture;

import server.member.entity.Member;
import server.member.entity.Member.MemberBuilder;
import server.member.entity.MemberRole;
import server.member.entity.ProviderType;
import java.util.concurrent.ThreadLocalRandom;

public class MemberFixture {

    /**
     * Domain/Service 테스트용 Fixture
     */
    public static Member create() {
        return builder().build();
    }

    public static MemberBuilder builder() {
        long randomId = ThreadLocalRandom.current().nextLong(1, 100000);
        return builderWithoutId().id(randomId);
    }

    /**
     * Repository/API 테스트용 Fixture
     */
    public static Member createWithoutId() {
        return builderWithoutId().build();
    }

    public static MemberBuilder builderWithoutId() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, 100000);
        return Member.builder()
                .id(null)
                .nickName("user" + randomNumber)
                .email("user" + randomNumber + "@example.com")
                .role(MemberRole.USER)
                .providerId("providerId" + randomNumber)
                .providerType(ProviderType.KAKAO)
                .profileImage("profile.jpg")
                .privacy(true)
                .birthYear("randomNumber");
    }
}

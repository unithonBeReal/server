package server.member.util;

import java.util.List;
import java.util.Random;

public class NicknameGenerator {
    private static final List<String> ADJECTIVES = List.of(
            "배고픈", "잠자는", "춤추는", "뛰어난", "멍때리는", "방황하는",
            "수다스러운", "귀차니즘", "흥분한", "뚝딱이는", "재채기하는", "뚜벅이는",
            "헛소리하는", "겁먹은", "버릇없는", "의심스러운", "꿈꾸는", "소심한",
            "엉뚱한", "알쏭달쏭한", "반항적인", "집중 안 하는", "장난꾸러기", "딴청부리는",
            "황당한", "잔망스러운", "수줍은", "쓸데없이 진지한", "호기심 많은", "허둥대는",
            "슬기로운", "자고 싶은", "헝클어진", "엄청난", "제멋대로인", "튕기는", "좌절한",
            "곱창을 사랑하는"
    );

    private static final List<String> NOUNS = List.of(
            "헤밍웨이", "오스틴", "도스토옙스키", "카프카",
            "무라카미 하루키", "롤링 바르트", "베르나르 베르베르", "마르크스",
            "스탕달", "발자크", "사르트르", "헤르만 헤세", "버지니아 울프",
            "디킨스", "빅토르 위고", "괴테", "니체", "하이데거", "셰익스피어", "톨스토이", "조지 오웰", "알베르 카뮈",
            "알랭 드 보통", "쇼펜하우어", "에리히 프롬"

            ,"프루스트", "유지혜", "공지영", "김유정", "박완서",
            "이상", "한강", "정세랑", "손원평", "김영하", "나태주", "공지영"
    );

    private static final Random RANDOM = new Random();

    public static String getRandomNickname() {
        String nickName;
        do{
            String adjective = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
            String noun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
            nickName = adjective + " " + noun;
        }
        while(nickName.length() > 11);

        return nickName;
    }
}

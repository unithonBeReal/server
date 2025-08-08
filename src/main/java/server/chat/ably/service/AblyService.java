package server.chat.ably.service;

import server.chat.dto.ChatMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ably.lib.rest.AblyRest;
import io.ably.lib.rest.Auth;
import io.ably.lib.rest.Channel;
import io.ably.lib.types.AblyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AblyService {

    private final AblyRest ablyRest;
    private final ObjectMapper objectMapper;

    public void publish(Long roomId, ChatMessageResponse message) {
        final Channel channel = ablyRest.channels.get("chat:" + roomId);
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            channel.publish("message", jsonMessage);
        } catch (AblyException | JsonProcessingException e) {
            log.error("Ably 메시지 발행 실패 : ", e);
            throw new RuntimeException("메시지를 실시간으로 전송하는데 실패했습니다.");
        }
    }

    public Auth.TokenDetails createToken(Long memberId) {
        try {
            // clientId를 memberId로 설정하여 특정 사용자를 식별합니다.
            // capability를 설정하여 특정 채널에 대한 publish/subscribe 권한을 부여할 수 있습니다.
            // 여기서는 모든 채널에 대한 권한을 부여합니다. (와일드카드 '*')
            return ablyRest.auth.requestToken(
                    new Auth.TokenParams() {{
                        clientId = String.valueOf(memberId);
                        capability = "{\"*\":[\"*\"]}";
                    }},
                    null
            );
        } catch (AblyException e) {
            throw new RuntimeException("Ably 토큰 발급에 실패했습니다.", e);
        }
    }
} 

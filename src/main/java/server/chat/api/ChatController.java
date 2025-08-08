package server.chat.api;

import server.chat.dto.ChatMessageRequest;
import server.chat.dto.ChatMessageResponse;
import server.chat.dto.ChatRoomResponse;
import server.chat.dto.response.ChatRoomParticipantsResponse;
import server.chat.service.ChatMessageService;
import server.chat.service.ChatRoomService;
import server.common.response.ResponseForm;
import server.common.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

@Tag(name = "chat", description = "채팅 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅방 참여")
    @PostMapping("/rooms/{roomId}/join")
    public ResponseForm<Void> joinChatRoom(@AuthenticationPrincipal Long memberId,
                                        @PathVariable Long roomId) {
        chatRoomService.joinChatRoom(memberId, roomId);
        return ResponseForm.ok();
    }

    @Operation(summary = "채팅방 나가기")
    @DeleteMapping("/rooms/{roomId}")
    public ResponseForm<Void> leaveChatRoom(@AuthenticationPrincipal Long memberId,
                                            @PathVariable Long roomId) {
        chatRoomService.leaveChatRoom(memberId, roomId);
        return ResponseForm.ok();
    }

    @Operation(summary = "참여한 채팅방 목록 조회")
    @GetMapping("/rooms/my")
    public ResponseForm<List<ChatRoomResponse>> getMyChatRooms(@AuthenticationPrincipal Long memberId) {
        return new ResponseForm<>(chatRoomService.getMyChatRooms(memberId));
    }

    @Operation(summary = "채팅방 참여자 목록 조회")
    @GetMapping("/rooms/{roomId}/participants")
    public ResponseForm<ChatRoomParticipantsResponse> getChatRoomParticipants(@PathVariable Long roomId) {
        return new ResponseForm<>(chatRoomService.getChatRoomParticipants(roomId));
    }

    @Operation(summary = "채팅 내역 조회", description = "커서 기반 페이징으로 이전 채팅 내역을 조회합니다. `cursorId`로 마지막으로 수신한 메시지의 ID를, `size`로 조회할 개수를 지정합니다. 첫 조회 시에는 `cursorId`를 보내지 않습니다.")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseForm<CursorPageResponse<ChatMessageResponse>> getChatHistory(@PathVariable Long roomId,
                                                                                @RequestParam(required = false) Long cursorId,
                                                                                @RequestParam(defaultValue = "30") int size) {
        return new ResponseForm<>(chatMessageService.getChatHistory(roomId, cursorId, size));
    }

    @Operation(summary = "메시지 저장", description = "텍스트, 이미지, 비디오, 파일 메시지를 저장합니다. `messageType`에 따라 `content` 또는 `fileUrl`을 전송해야합니다.")
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseForm<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long roomId,
            @Valid @RequestBody ChatMessageRequest request) {
        return new ResponseForm<>(chatMessageService.saveMessage(roomId, memberId, request));
    }
} 

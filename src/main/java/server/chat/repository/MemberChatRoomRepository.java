package server.chat.repository;

import server.chat.domain.ChatRoom;
import server.chat.domain.MemberChatRoom;
import server.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface MemberChatRoomRepository extends JpaRepository<MemberChatRoom, Long> {

    boolean existsByMemberAndChatRoom(Member member, ChatRoom chatRoom);

    void deleteByMemberIdAndChatRoomId(Long memberId, Long chatRoomId);

    void deleteAllByMemberId(Long memberId);
    
    @Query("select mcr from MemberChatRoom mcr join fetch mcr.member where mcr.chatRoom.id = :chatRoomId")
    List<MemberChatRoom> findAllByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("select mcr.chatRoom from MemberChatRoom mcr where mcr.member = :member order by mcr.createdDate desc")
    List<ChatRoom> findChatRoomsByMember(@Param("member") Member member);
} 

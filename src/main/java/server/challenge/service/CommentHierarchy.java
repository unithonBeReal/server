package server.challenge.service;

import server.challenge.domain.DiaryComment;
import server.challenge.dto.response.DiaryCommentResponse;
import server.challenge.dto.response.DiaryReplyResponse;
import server.challenge.repository.DiaryCommentRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentHierarchy {

    private final List<DiaryComment> parentComments;
    private final Map<Long, List<DiaryReplyResponse>> repliesMap;

    // TODO: 변경에 취약
    public static CommentHierarchy of(
            DiaryCommentRepository diaryCommentRepository,
            Long diaryId, Long cursor, int size
    ) {
        List<DiaryComment> parentComments = diaryCommentRepository.findParentCommentsByDiary(diaryId, cursor, size);

        List<Long> parentIds = parentComments.stream()
                .map(DiaryComment::getId)
                .toList();

        List<DiaryComment> replies = diaryCommentRepository.findRepliesByParentIdsIn(parentIds);
        Map<Long, List<DiaryReplyResponse>> repliesMap = replies.stream()
                .collect(Collectors.groupingBy(
                        reply -> reply.getParent().getId(),
                        Collectors.mapping(DiaryReplyResponse::from, Collectors.toList())
                ));

        return new CommentHierarchy(parentComments, repliesMap);
    }

    public List<DiaryCommentResponse> toResponses() {
        return this.parentComments.stream()
                .map(parent -> DiaryCommentResponse.of(parent,
                        this.repliesMap.getOrDefault(parent.getId(), Collections.emptyList())))
                .toList();
    }
} 

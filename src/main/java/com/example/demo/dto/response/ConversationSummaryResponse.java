package com.example.demo.dto.response;

import java.time.Instant;
import java.util.List;

public record ConversationSummaryResponse(
		Long id,
		Instant updatedAt,
		List<Member> members, // 相手一覧（自分も含む）
		LastMessage lastMessage,
		int unreadCount) {
	public record Member(Long id, String name, String avatarUrl) {
	}

	public record LastMessage(Long id, String content, Instant createdAt, Long authorId) {
	}
}

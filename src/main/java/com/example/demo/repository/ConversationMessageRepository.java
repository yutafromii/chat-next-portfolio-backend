package com.example.demo.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.ConversationMessage;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

	/** 会話内メッセージを昇順で。sender → user まで取得（吹き出し表示に必要） */
	@EntityGraph(attributePaths = { "sender", "sender.user" })
	List<ConversationMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

	/** 会話の最新メッセージ（一覧のプレビューなどで使用） */
	@EntityGraph(attributePaths = { "sender", "sender.user" })
	Optional<ConversationMessage> findTop1ByConversationIdOrderByCreatedAtDesc(Long conversationId);
	@Query("""
			  select count(m) from ConversationMessage m
			  where m.conversation.id = :cid
			    and m.createdAt > :ts
			    and m.sender.user.id <> :meId
			""")
			long countUnread(@Param("cid") Long conversationId,
			                 @Param("ts") Instant ts,
			                 @Param("meId") Long meId);

}
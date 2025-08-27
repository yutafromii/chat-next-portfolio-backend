package com.example.demo.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
	@EntityGraph(attributePaths = "author")
	@Query("select m from Message m order by m.createdAt desc")
	List<Message> findAllByOrderByCreatedAtDescWithAuthor();

	@EntityGraph(attributePaths = "author")
	List<Message> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

	// 会話メッセージ（author と author.profile を同時取得）
	@EntityGraph(attributePaths = { "author", "author.profile" })
	@Query("""
			  select m from Message m
			  where m.conversation.id = :cid
			  order by m.createdAt asc
			""")
	List<Message> findByConversationIdWithAuthor(@Param("cid") Long conversationId);

	// 会話の最新メッセージ（会話一覧カードに表示する用）
	@EntityGraph(attributePaths = { "author" })
	Message findTop1ByConversationIdOrderByCreatedAtDesc(Long conversationId);
	
	@Query("""
			  select count(m) from Message m
			  where m.conversation.id = :cid
			    and m.createdAt > :ts
			    and m.author.id <> :meId
			""")
			long countUnread(@Param("cid") Long conversationId,
			                 @Param("ts") Instant ts,
			                 @Param("meId") Long meId);
	
}

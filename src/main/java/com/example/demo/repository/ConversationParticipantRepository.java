package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ConversationParticipant;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

	Optional<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);

	List<ConversationParticipant> findByConversationId(Long conversationId);

	boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
}

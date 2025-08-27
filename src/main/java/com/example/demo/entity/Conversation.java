package com.example.demo.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "conversations", indexes = {
  @Index(name = "idx_conversations_updated_at", columnList = "updatedAt")
})
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class Conversation {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;

  /** 参加者（所有側は ConversationParticipant） */
  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ConversationParticipant> participants = new ArrayList<>();

  /** 会話内メッセージ（所有側は ConversationMessage） */
  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private List<ConversationMessage> messages = new ArrayList<>();

  public void addParticipant(ConversationParticipant p) {
    participants.add(p);
    p.setConversation(this);
  }
}
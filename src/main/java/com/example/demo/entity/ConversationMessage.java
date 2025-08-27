package com.example.demo.entity;


import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "conversation_messages", indexes = {
  @Index(name = "idx_convmsg_conversation_created", columnList = "conversation_id, createdAt"),
  @Index(name = "idx_convmsg_sender", columnList = "sender_id")
})
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class ConversationMessage {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** どの会話のメッセージか */
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id", nullable = false)
  private Conversation conversation;

  /** 送信者（＝会話の参加者） */
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private ConversationParticipant sender;

  /** 本文 */
  @Size(max = 2000)
  @Column(nullable = false, length = 2000)
  private String content;

  /** 将来のモデレーション/削除に備えて */
  @Column(nullable = false)
  private boolean deleted = false;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;

  /** 便利メソッド（ビュー層向け） */
  @Transient
  public User getAuthorUser() {
    return sender != null ? sender.getUser() : null;
  }
}
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "messages", indexes = {
  @Index(columnList = "createdAt"),
  @Index(columnList = "author_id, createdAt")
})
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class Message {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  @Column(nullable = false, length = 2000)
  private String content;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id") // ← 当面は nullable のまま
  private Conversation conversation;

  // 将来の「削除」や「モデレーション」のため
  @Column(nullable = false)
  private boolean deleted = false;
  
  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = java.time.Instant.now();
    }
  }
}
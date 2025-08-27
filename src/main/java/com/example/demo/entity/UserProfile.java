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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_profiles", indexes = {
    @Index(name = "ux_user_profiles_user_id", columnList = "user_id", unique = true)
})
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 本文（自己紹介） */
  @Size(max = 500)
  @Column(length = 500)
  private String bio; // description という名前より bio の方が慣習的

  /** 画像URLは長めに確保（将来CDN等もOKに） */
  @Size(max = 2048)
  @Column(length = 2048)
  private String avatarUrl; // avater_img → avatarUrl に命名調整

  @Size(max = 2048)
  @Column(length = 2048)
  private String headerUrl; // header_img → headerUrl

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;

  /** 所有側（FK: user_id） */
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
    if (updatedAt == null) updatedAt = createdAt;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }
}

package com.example.demo.entity;

import java.time.Instant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "ux_users_name", columnList = "name", unique = true),
    @Index(name = "idx_users_created_at", columnList = "createdAt")
})
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class User {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** ログインID（ユニーク） */
  @NotBlank
  @Size(max = 50)
  @Column(nullable = false, length = 50, unique = true)
  private String name;

  /** パスワードのハッシュ（BCrypt想定なら60文字程度） */
  @NotBlank
  @Size(max = 100)
  @Column(nullable = false, length = 100)
  private String passwordHash;
  
  @Column(nullable = false, unique = true)
  private String email;
  
  @Column(nullable = false)
  private String role;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;

  /** 1:1 プロフィール（Profile側がFKを持つ） */
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private UserProfile profile;

  /** 両方向リンクの整合を取るヘルパー（任意） */
  public void setProfile(UserProfile profile) {
    this.profile = profile;
    if (profile != null) profile.setUser(this);
  }

  /** 監査の保険（監査無効時でも落ちないように） */
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
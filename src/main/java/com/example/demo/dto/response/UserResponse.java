package com.example.demo.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
  private Long id;
  private String name;
  private String email;
  private String role;
  private Instant createdAt;
  private Instant updatedAt;
  private Profile profile;

  @Getter
  @AllArgsConstructor
  public static class Profile {
    private String avatarUrl;
    private String headerUrl;
    private String bio;
  }
}
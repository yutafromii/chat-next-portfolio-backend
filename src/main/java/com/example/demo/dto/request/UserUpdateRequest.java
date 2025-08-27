package com.example.demo.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/** 任意項目のみ。null の項目は更新しません。 */
@Getter @Setter
public class UserUpdateRequest {
  @Nullable
  @Size(min = 3, max = 50)
  private String name;

  @Nullable @Size(max = 255)
  private String avatarUrl;

  @Nullable @Size(max = 255)
  private String headerUrl;

  @Nullable @Size(max = 500)
  private String bio;
}
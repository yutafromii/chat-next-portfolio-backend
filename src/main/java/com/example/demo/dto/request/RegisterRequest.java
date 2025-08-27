package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
  @NotBlank @Size(min = 3, max = 50)
  private String name;

  @NotBlank @Email @Size(max = 255)
  private String email;

  // 平文を受ける。ハッシュは受け取らない
  @NotBlank @Size(min = 8, max = 72) // BCryptは72バイト上限
  private String password;
}
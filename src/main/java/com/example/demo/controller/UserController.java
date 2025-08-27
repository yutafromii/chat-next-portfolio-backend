package com.example.demo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.dto.response.UserSearchResponse;
import com.example.demo.service.UserService;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /** ユーザー検索（自分は除外） */
  @GetMapping("/search")
  public List<UserSearchResponse> search(@RequestParam(value = "q", required = false) String q) {
    return userService.search(q);
  }

  /** 自分の情報取得 */
  @GetMapping("/me")
  public UserResponse me() {
    return userService.getCurrentUser();
  }

  /** 自分の情報更新（部分更新） */
  @PutMapping("/me")
  public UserResponse updateMe(@RequestBody @Valid UserUpdateRequest req) {
    return userService.updateCurrentUser(req);
  }
}

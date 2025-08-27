package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.dto.response.UserSearchResponse;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CurrentUserProvider;

@Service
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  // 任意：アプリ全体で統一したい場合はこれを使う
  private final CurrentUserProvider current;

  public UserService(UserRepository userRepository, CurrentUserProvider current) {
    this.userRepository = userRepository;
    this.current = current;
  }

  /** 現在ログイン中ユーザーを DTO で返す */
  public UserResponse getCurrentUser() {
    User user = getMe(); // ← CurrentUserProvider を使う版
    return toResponse(user);
  }

  /** id でユーザーを取得（プロフィール込みで返す） */
  public UserResponse getById(Long id) {
    User u = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
    return toResponse(u);
  }

  /** ユーザー検索（自分は除外） */
  public List<UserSearchResponse> search(String q) {
    var keyword = (q == null) ? "" : q.trim();
    if (keyword.isEmpty()) return List.of();

    var me = getMe();
    var hits = userRepository.searchUsers(keyword, me.getId());

    return hits.stream()
        .map(u -> new UserSearchResponse(
            u.getId(),
            u.getName(),
            u.getEmail(),
            (u.getProfile() != null) ? u.getProfile().getAvatarUrl() : null
        ))
        .toList();
  }

  /** 現在ログイン中ユーザーを部分更新（null は無視） */
  @Transactional
  public UserResponse updateCurrentUser(UserUpdateRequest req) {
    User user = getMe();

    // name（ユニーク）更新
    if (req.getName() != null) {
      String newName = req.getName().trim();
      if (newName.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name は空にできません");
      }
      if (!newName.equalsIgnoreCase(user.getName())
          && userRepository.existsByNameIgnoreCase(newName)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "そのユーザー名は既に使われています");
      }
      user.setName(newName);
    }

    // プロフィール（なければ生成）
    UserProfile profile = user.getProfile();
    if (profile == null) {
      profile = new UserProfile();
      user.setProfile(profile);
    }
    if (req.getAvatarUrl() != null) profile.setAvatarUrl(req.getAvatarUrl().trim());
    if (req.getHeaderUrl() != null) profile.setHeaderUrl(req.getHeaderUrl().trim());
    if (req.getBio() != null)       profile.setBio(req.getBio().trim());

    return toResponse(user); // 変更検知でflush
  }

  // ===== ヘルパー =====

  // CurrentUserProvider経由（推奨）
  private User getMe() {
    return current.getRequiredUser();
  }

  // 直接 SecurityContext を使う版（既存互換/参考）
  @SuppressWarnings("unused")
  private User requireCurrentUserLegacy() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()
        || "anonymousUser".equals(auth.getPrincipal())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未認証です");
    }
    String email = auth.getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ユーザーが見つかりません"));
  }

  // エンティティ → DTO
  private UserResponse toResponse(User u) {
    UserResponse.Profile p = (u.getProfile() == null)
        ? null
        : new UserResponse.Profile(
            u.getProfile().getAvatarUrl(),
            u.getProfile().getHeaderUrl(),
            u.getProfile().getBio()
          );
    return new UserResponse(
        u.getId(),
        u.getName(),
        u.getEmail(),
        u.getRole(),
        u.getCreatedAt(),
        u.getUpdatedAt(),
        p
    );
  }
}

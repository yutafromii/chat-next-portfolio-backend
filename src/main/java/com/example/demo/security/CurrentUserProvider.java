package com.example.demo.security;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Component
public class CurrentUserProvider {

  private final UserRepository userRepository;

  public CurrentUserProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /** 認証必須で User を取得（見つからなければ 403/401 相当） */
  @Transactional(readOnly = true)
  public User getRequiredUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()
        || "anonymousUser".equals(auth.getPrincipal())) {
      throw new AccessDeniedException("未認証です");
    }
    String email = auth.getName(); // username=email 前提
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
  }

  /** 認証済みなら User、未認証なら empty を返す（エンドポイントの任意公開に便利） */
  @Transactional(readOnly = true)
  public Optional<User> getUserIfPresent() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()
        || "anonymousUser".equals(auth.getPrincipal())) {
      return Optional.empty();
    }
    return userRepository.findByEmail(auth.getName());
  }

  /** ID だけ欲しいとき用（DBヒットを避けたい箇所で） */
  public Optional<Long> getUserIdIfPresent() {
    return getUserIfPresent().map(User::getId);
  }
}
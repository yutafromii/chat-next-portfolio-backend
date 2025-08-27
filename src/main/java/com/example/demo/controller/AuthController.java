package com.example.demo.controller;


import java.time.Duration;
import java.util.Collections;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
public class AuthController {

  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;
  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthController(JwtUtil jwtUtil, AuthenticationManager authenticationManager,
      UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.jwtUtil = jwtUtil;
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getEmail(), loginRequest.getPassword()));

      String token = jwtUtil.generateToken(authentication.getName());

      ResponseCookie cookie = ResponseCookie.from("token", token)
          .httpOnly(true)
          .secure(false)
          .path("/")
          .maxAge(Duration.ofHours(1))
          .sameSite("Strict")
          .build();

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, cookie.toString())
          .body(Collections.singletonMap("token", token));
    } catch (Exception e) {
      logger.warn("認証失敗: {}", loginRequest.getEmail());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ログインに失敗しました");
    }
  }

  @GetMapping("/mypage")
  public ResponseEntity<?> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()
        || authentication.getPrincipal().equals("anonymousUser")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未認証です");
    }

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String email = userDetails.getUsername(); // ここは email が返る

    return ResponseEntity.ok(Collections.singletonMap("email", email));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
    Cookie cookie = new Cookie("token", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    response.addCookie(cookie);
    return ResponseEntity.ok().body("ログアウトしました");
  }


@PostMapping("/register")
public ResponseEntity<?> register(@jakarta.validation.Valid @RequestBody RegisterRequest request) {

  if (userRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Collections.singletonMap("error", "すでに同じメールアドレスが存在します"));
  }

  String hash = passwordEncoder.encode(request.getPassword()); // ★ここを修正

  User newUser = new User();
  newUser.setName(request.getName().trim());
  newUser.setEmail(request.getEmail().trim().toLowerCase());
  newUser.setPasswordHash(hash); // ★エンティティはハッシュを保存
  newUser.setRole("USER");

  userRepository.save(newUser);

  return ResponseEntity.status(HttpStatus.CREATED)
      .body(Collections.singletonMap("message", "ユーザー登録に成功しました"));
}
}
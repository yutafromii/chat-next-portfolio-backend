package com.example.demo.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.MessageRequest;
import com.example.demo.dto.response.ConversationSummaryResponse;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.service.ConversationService;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ConversationController {

  private final ConversationService conversationService;

  public ConversationController(ConversationService conversationService) {
    this.conversationService = conversationService;
  }

  /** 自分の会話一覧（最新更新順） */
  @GetMapping
  public List<ConversationSummaryResponse> myConversations() {
    return conversationService.listMyConversations();
  }

  /** DMを開く（既存があればそれを返す） */
  @PostMapping("/open")
  public ConversationSummaryResponse openDm(@Valid @RequestBody OpenDmRequest req) {
    return conversationService.openDm(req.partnerId());
  }

  /** 会話のメッセージ一覧（古い→新しい） */
  @GetMapping("/{conversationId}/messages")
  public List<MessageResponse> messages(@PathVariable Long conversationId) {
    return conversationService.listMessages(conversationId);
  }

  /** 会話へメッセージ投稿 */
  @PostMapping("/{conversationId}/messages")
  public MessageResponse postMessage(@PathVariable Long conversationId,
                                     @Valid @RequestBody MessageRequest req) {
    return conversationService.postMessage(conversationId, req);
  }

  /** openDm の受け皿 DTO（partnerId をJSONボディで受け取る） */
  public static record OpenDmRequest(@NotNull Long partnerId) {}
}
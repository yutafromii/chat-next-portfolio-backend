package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.MessageRequest;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.entity.Message;
import com.example.demo.repository.MessageRepository;
import com.example.demo.security.CurrentUserProvider;

@Service
@Transactional(readOnly = true)
public class MessageService {

  private final MessageRepository messageRepository;
  private final CurrentUserProvider currentUserProvider;

  public MessageService(MessageRepository messageRepository,
                        CurrentUserProvider currentUserProvider) {
    this.messageRepository = messageRepository;
    this.currentUserProvider = currentUserProvider;
  }

  /** 投稿（ログインユーザー紐づけ） */
  @Transactional
  public MessageResponse create(MessageRequest req) {
    var me = currentUserProvider.getRequiredUser();
    var m = new Message();
    m.setAuthor(me);
    m.setContent(req.content());
    var saved = messageRepository.save(m);
    return toResponse(saved);
  }

  /** 自分のメッセージ一覧（新着順） */
  public List<MessageResponse> listMine() {
    var me = currentUserProvider.getRequiredUser();
    return messageRepository.findByAuthorIdOrderByCreatedAtDesc(me.getId())
        .stream()
        .map(this::toResponse)
        .toList();
  }

  /** 全体タイムライン（新着順）— 必要なら使う */
  public List<MessageResponse> listAll() {
    return messageRepository.findAllByOrderByCreatedAtDescWithAuthor()
        .stream()
        .map(this::toResponse)
        .toList();
  }

  // ---- Mapper（最小実装）----
  private MessageResponse toResponse(Message m) {
    return new MessageResponse(
        m.getId(),
        m.getContent(),
        m.getCreatedAt(),
        new MessageResponse.Author(
            m.getAuthor().getId(),
            m.getAuthor().getName()
        )
    );
  }
}

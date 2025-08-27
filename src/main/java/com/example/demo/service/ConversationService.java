package com.example.demo.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.MessageRequest;
import com.example.demo.dto.response.ConversationSummaryResponse;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.ConversationMessage;
import com.example.demo.entity.ConversationParticipant;
import com.example.demo.repository.ConversationMessageRepository;
import com.example.demo.repository.ConversationParticipantRepository;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CurrentUserProvider;

@Service
@Transactional(readOnly = true)
public class ConversationService {

  private final ConversationRepository conversationRepo;
  private final ConversationParticipantRepository participantRepo;
  private final ConversationMessageRepository messageRepo;
  private final UserRepository userRepo;
  private final CurrentUserProvider current;

  public ConversationService(
      ConversationRepository conversationRepo,
      ConversationParticipantRepository participantRepo,
      ConversationMessageRepository messageRepo,
      UserRepository userRepo,
      CurrentUserProvider current
  ) {
    this.conversationRepo = conversationRepo;
    this.participantRepo = participantRepo;
    this.messageRepo = messageRepo;
    this.userRepo = userRepo;
    this.current = current;
  }

  /** 自分の会話一覧（新着順）＋ 未読件数 */
  public List<ConversationSummaryResponse> listMyConversations() {
    var me = current.getRequiredUser();

    return conversationRepo.findAllByUserId(me.getId())
        .stream()
        .map(c -> {
          // メンバー
          var members = participantRepo.findByConversationId(c.getId()).stream()
              .map(p -> {
                var u = p.getUser();
                var avatar = (u.getProfile() != null) ? u.getProfile().getAvatarUrl() : null;
                return new ConversationSummaryResponse.Member(u.getId(), u.getName(), avatar);
              })
              .toList();

          // 最終メッセージ
          var lastOpt = messageRepo.findTop1ByConversationIdOrderByCreatedAtDesc(c.getId());
          ConversationSummaryResponse.LastMessage lastDto = null;
          if (lastOpt.isPresent()) {
            var last = lastOpt.get();
            lastDto = new ConversationSummaryResponse.LastMessage(
                last.getId(),
                last.getContent(),
                last.getCreatedAt(),
                last.getSender().getUser().getId()
            );
          }

          // 未読件数：自分の lastReadAt 以降 & 自分以外が送信
          var myPart = participantRepo.findByConversationIdAndUserId(c.getId(), me.getId())
              .orElseThrow();
          var ts = (myPart.getLastReadAt() != null) ? myPart.getLastReadAt() : Instant.EPOCH;
          var unread = messageRepo.countUnread(c.getId(), ts, me.getId());

          return new ConversationSummaryResponse(
              c.getId(),
              c.getUpdatedAt(),
              members,
              lastDto,
              (int) unread
          );
        })
        .toList();
  }

  /** 相手ユーザーとのDMを開く（既存があれば再利用） */
  @Transactional
  public ConversationSummaryResponse openDm(Long otherUserId) {
    var me = current.getRequiredUser();
    if (me.getId().equals(otherUserId)) {
      throw new IllegalArgumentException("自分自身とはDMを作成できません");
    }
    var other = userRepo.findById(otherUserId).orElseThrow();

    var existing = conversationRepo.findDmBetween(me.getId(), other.getId());
    Conversation conv;
    if (existing.isEmpty()) {
      conv = new Conversation();
      var p1 = new ConversationParticipant(); p1.setUser(me);    conv.addParticipant(p1);
      var p2 = new ConversationParticipant(); p2.setUser(other); conv.addParticipant(p2);
      conv = conversationRepo.save(conv);
    } else {
      conv = existing.get(0);
    }

    var members = participantRepo.findByConversationId(conv.getId()).stream()
        .map(p -> {
          var u = p.getUser();
          var avatar = (u.getProfile() != null) ? u.getProfile().getAvatarUrl() : null;
          return new ConversationSummaryResponse.Member(u.getId(), u.getName(), avatar);
        })
        .toList();

    // 新規作成時点では未読0なので 0 固定でOK（必要なら直前の countUnread を呼んでもよい）
    return new ConversationSummaryResponse(conv.getId(), conv.getUpdatedAt(), members, null, 0);
  }

  /** 会話のメッセージ一覧（古い→新しい）※取得時に既読へ */
  @Transactional // 既読更新のため readOnly=false
  public List<MessageResponse> listMessages(Long conversationId) {
    var me = current.getRequiredUser();

    var myPart = participantRepo.findByConversationIdAndUserId(conversationId, me.getId())
        .orElseThrow(() -> new AccessDeniedException("この会話の参加者ではありません"));

    var list = messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId)
        .stream()
        .map(this::toMessageResponse)
        .toList();

    // このタイミングで既読にする
    myPart.setLastReadAt(Instant.now());

    return list;
  }

  /** 会話にメッセージ投稿（会話の updatedAt を前に進める） */
  @Transactional
  public MessageResponse postMessage(Long conversationId, MessageRequest req) {
    var me = current.getRequiredUser();

    var myParticipant = participantRepo.findByConversationIdAndUserId(conversationId, me.getId())
        .orElseThrow(() -> new AccessDeniedException("この会話の参加者ではありません"));

    var conv = myParticipant.getConversation(); // 追加SELECTを避ける

    var msg = new ConversationMessage();
    msg.setConversation(conv);
    msg.setSender(myParticipant);
    msg.setContent(req.content());

    var saved = messageRepo.save(msg);

    // タイムライン更新用に updatedAt を前に進める
    conv.setUpdatedAt(saved.getCreatedAt());

    return toMessageResponse(saved);
  }

  // ---- Mapper ----
  private MessageResponse toMessageResponse(ConversationMessage m) {
    var u = m.getSender().getUser();
    return new MessageResponse(
        m.getId(),
        m.getContent(),
        m.getCreatedAt(),
        new MessageResponse.Author(
            u.getId(),
            u.getName()
        )
    );
  }
}
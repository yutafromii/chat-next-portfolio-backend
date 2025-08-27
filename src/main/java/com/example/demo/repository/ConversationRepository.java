package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  /** 自分が参加している会話一覧（新着順）＋ 参加者(Userまで) を一緒取り */
  @EntityGraph(attributePaths = {"participants", "participants.user"})
  @Query("""
    select distinct c
    from Conversation c
      join c.participants p
    where p.user.id = :userId
    order by c.updatedAt desc
  """)
  List<Conversation> findAllByUserId(@Param("userId") Long userId);

  /** 2人DMが既にあるか（※「2人を含む会話」でもヒット：グループ含む） */
  @Query("""
    select c
    from Conversation c
      join c.participants p
    where p.user.id in (:u1, :u2)
    group by c.id
    having count(distinct p.user.id) = 2
  """)
  List<Conversation> findDmBetween(@Param("u1") Long u1, @Param("u2") Long u2);

  /** 2人DMを厳密に「参加者がちょうど2人」のみに限定したい場合はこちらを使用 */
  @Query("""
    select c
    from Conversation c
      join c.participants p
    where p.user.id in (:u1, :u2)
    group by c.id
    having count(distinct p.user.id) = 2
       and (select count(pall) from ConversationParticipant pall where pall.conversation = c) = 2
  """)
  List<Conversation> findStrictDmBetween(@Param("u1") Long u1, @Param("u2") Long u2);

  /** 詳細画面などで participants.user まで一発で取りたいとき */
  @EntityGraph(attributePaths = {"participants", "participants.user"})
  Optional<Conversation> findById(Long id);
}
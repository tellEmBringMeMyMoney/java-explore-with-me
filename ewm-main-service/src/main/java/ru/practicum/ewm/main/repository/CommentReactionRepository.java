package ru.practicum.ewm.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.model.CommentReactionEntity;
import ru.practicum.ewm.main.model.ReactionType;

import java.util.List;
import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReactionEntity, Long> {
    Optional<CommentReactionEntity> findByCommentIdAndUserId(Long commentId, Long userId);

    interface Counts {
        Long getCommentId();

        ReactionType getType();

        Long getCount();
    }

    @Query("select r.comment.id as commentId, r.type as type, count(r) as count from CommentReactionEntity r where r.comment.id in :ids group by r.comment.id, r.type")
    List<Counts> countByComments(@Param("ids") List<Long> ids);
}

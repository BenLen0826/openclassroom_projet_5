package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Accès aux commentaires (Spring Data JPA). */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Commentaires d'un article, du plus ancien au plus récent (l'id départage deux
     * commentaires de la même date). L'auteur est lu dans la même requête.
     */
    @Query("""
            select c from Comment c
            join fetch c.author
            where c.post.id = :postId
            order by c.createdAt asc, c.id asc
            """)
    List<Comment> findByPostIdWithAuthor(@Param("postId") Long postId);
}

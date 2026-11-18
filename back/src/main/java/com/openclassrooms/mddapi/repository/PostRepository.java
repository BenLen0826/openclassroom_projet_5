package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Fil d'actualité : articles des thèmes suivis, du plus récent au plus ancien
     * (l'id départage deux articles de la même date). L'auteur et le thème sont lus
     * dans la même requête, pour éviter une requête par article.
     */
    @Query("""
            select p from Post p
            join fetch p.author
            join fetch p.topic
            where p.topic.id in (select t.id from User u join u.subscriptions t where u.id = :userId)
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findFeed(@Param("userId") Long userId);
}

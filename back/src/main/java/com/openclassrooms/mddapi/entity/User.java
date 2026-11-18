package com.openclassrooms.mddapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilisateur inscrit.
 * <p>
 * Les dates de création et de modification sont remplies par Spring Data JPA
 * Auditing. Seuls le nom, l'e-mail et le mot de passe sont modifiables (profil) :
 * l'identifiant et les dates n'ont pas de setter.
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requis par JPA, inutile ailleurs
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Setter
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** Hachage BCrypt (60 caractères), jamais le mot de passe en clair. */
    @Setter
    @Column(nullable = false, length = 255)
    private String password;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Thèmes suivis (table {@code subscriptions}, clé primaire composée : un double
     * abonnement est impossible en base). Chargés seulement à la demande.
     * <p>
     * {@code Topic} n'a pas d'{@code equals} : les comparaisons se font par id.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subscriptions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id"))
    private Set<Topic> subscriptions = new HashSet<>();

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}

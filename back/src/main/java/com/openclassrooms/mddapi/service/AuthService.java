package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.dto.validation.PasswordValidator;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.AccountAlreadyExistsException;
import com.openclassrooms.mddapi.exception.EmailAlreadyUsedException;
import com.openclassrooms.mddapi.exception.InvalidCredentialsException;
import com.openclassrooms.mddapi.exception.UsernameAlreadyUsedException;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Inscription et connexion : vérifie les identifiants, hache le mot de passe (BCrypt) et
 * renvoie un token JWT. Aucune donnée personnelle n'est écrite dans les logs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Inscrit un utilisateur et le connecte directement (renvoie son token).
     * <p>
     * L'e-mail est enregistré en minuscules et sans espaces autour, le nom
     * d'utilisateur sans espaces autour ; l'unicité ne tient pas compte de la casse.
     *
     * @throws EmailAlreadyUsedException     si l'e-mail est déjà utilisé
     * @throws UsernameAlreadyUsedException  si le nom d'utilisateur est déjà utilisé
     * @throws AccountAlreadyExistsException si une inscription identique a été
     *                                       enregistrée au même moment
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UsernameAlreadyUsedException();
        }

        User user = new User(username, email, passwordEncoder.encode(request.password()));
        User saved;
        try {
            // saveAndFlush : l'insertion a lieu ici, donc une violation d'unicité est levée
            // dans ce bloc et non au moment du commit
            saved = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new AccountAlreadyExistsException();
        }

        log.info("Nouvel utilisateur inscrit (id={})", saved.getId());
        return new AuthResponse(jwtService.generateToken(saved));
    }

    /**
     * Connecte un utilisateur par e-mail ou nom d'utilisateur et renvoie son token.
     * <p>
     * Recherche d'abord par e-mail, puis par nom d'utilisateur (sans tenir compte de
     * la casse) : si un nom d'utilisateur est identique à l'e-mail d'un autre compte,
     * c'est le compte de l'e-mail qui est retenu.
     *
     * @throws InvalidCredentialsException si le compte est inconnu ou le mot de passe
     *                                     faux (même message dans les deux cas)
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        String password = request.password();

        User user = userRepository.findByEmail(identifier.toLowerCase(Locale.ROOT))
                .or(() -> userRepository.findByUsernameIgnoreCase(identifier))
                .orElseThrow(InvalidCredentialsException::new);

        // Au-delà de 72 octets, BCrypt refuserait le mot de passe (erreur 500) : il ne peut
        // de toute façon correspondre à aucun mot de passe enregistré.
        if (password.getBytes(StandardCharsets.UTF_8).length > PasswordValidator.BCRYPT_MAX_BYTES
                || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(jwtService.generateToken(user));
    }
}

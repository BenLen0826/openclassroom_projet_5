package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.AccountAlreadyExistsException;
import com.openclassrooms.mddapi.exception.EmailAlreadyUsedException;
import com.openclassrooms.mddapi.exception.UsernameAlreadyUsedException;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

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
}

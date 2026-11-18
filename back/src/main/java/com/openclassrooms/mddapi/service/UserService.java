package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.UpdateProfileRequest;
import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.AccountAlreadyExistsException;
import com.openclassrooms.mddapi.exception.EmailAlreadyUsedException;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.exception.UsernameAlreadyUsedException;
import com.openclassrooms.mddapi.mapper.UserMapper;
import com.openclassrooms.mddapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Profil de l'utilisateur connecté : consultation et modification. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDto getMe(Long userId) {
        return userMapper.toDto(findUser(userId));
    }

    /**
     * Modifie le profil. Même normalisation qu'à l'inscription ; garder son propre
     * e-mail ou nom n'est pas un conflit, seul un autre compte en est un. Le mot de
     * passe n'est re-haché que s'il est fourni. L'id ne change pas : le token reste valide.
     *
     * @throws EmailAlreadyUsedException     e-mail déjà utilisé par un autre compte
     * @throws UsernameAlreadyUsedException  nom déjà utilisé par un autre compte
     * @throws AccountAlreadyExistsException conflit détecté par la base (modification simultanée)
     * @throws UnknownUserException          compte du token introuvable
     */
    @Transactional
    public UserDto updateMe(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim();

        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new EmailAlreadyUsedException();
        }
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, userId)) {
            throw new UsernameAlreadyUsedException();
        }

        user.setEmail(email);
        user.setUsername(username);
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        try {
            // saveAndFlush : un conflit d'unicité est détecté ici et non au commit
            return userMapper.toDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            throw new AccountAlreadyExistsException();
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(UnknownUserException::new);
    }
}

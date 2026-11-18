package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.AccountAlreadyExistsException;
import com.openclassrooms.mddapi.exception.EmailAlreadyUsedException;
import com.openclassrooms.mddapi.exception.InvalidCredentialsException;
import com.openclassrooms.mddapi.exception.UsernameAlreadyUsedException;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesNormalizedUserWithHashedPasswordAndReturnsToken() {
        // Arrange
        RegisterRequest request = new RegisterRequest(" Bob ", "  Bob@Mail.FR ", "Secret1!");
        when(passwordEncoder.encode("Secret1!")).thenReturn("hash");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("Bob");
        assertThat(saved.getValue().getEmail()).isEqualTo("bob@mail.fr");
        assertThat(saved.getValue().getPassword()).isEqualTo("hash");
        assertThat(response).isEqualTo(new AuthResponse("jwt"));
    }

    @Test
    void register_checksEmailUniquenessOnNormalizedEmail() {
        // Arrange
        when(userRepository.existsByEmail("bob@mail.fr")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.register(new RegisterRequest("Bob", "  Bob@Mail.FR ", "Secret1!")))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(userRepository, never()).saveAndFlush(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void register_throwsWhenUsernameTaken() {
        // Arrange
        when(userRepository.existsByUsernameIgnoreCase("Bob")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.register(new RegisterRequest(" Bob ", "bob@mail.fr", "Secret1!")))
                .isInstanceOf(UsernameAlreadyUsedException.class);
        verify(userRepository, never()).saveAndFlush(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void register_translatesUniqueConstraintViolationToConflict() {
        // Arrange : deux inscriptions simultanées, la base refuse la seconde
        when(passwordEncoder.encode("Secret1!")).thenReturn("hash");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act + Assert
        assertThatThrownBy(() -> authService.register(new RegisterRequest("Bob", "bob@mail.fr", "Secret1!")))
                .isInstanceOf(AccountAlreadyExistsException.class);
        verifyNoInteractions(jwtService);
    }

    @Test
    void registerRequest_toStringNeverContainsThePassword() {
        assertThat(new RegisterRequest("Bob", "bob@mail.fr", "Secret1!").toString()).doesNotContain("Secret1!");
    }

    // --- Connexion ---

    private static final String LOGIN_ERROR = "Identifiant ou mot de passe incorrect.";

    @Test
    void login_withEmail_returnsToken() {
        // Arrange
        User bob = new User("Bob", "bob@mail.fr", "hash");
        when(userRepository.findByEmail("bob@mail.fr")).thenReturn(Optional.of(bob));
        when(passwordEncoder.matches("Secret1!", "hash")).thenReturn(true);
        when(jwtService.generateToken(bob)).thenReturn("jwt");

        // Act
        AuthResponse response = authService.login(new LoginRequest("  BOB@Mail.FR ", "Secret1!"));

        // Assert
        assertThat(response).isEqualTo(new AuthResponse("jwt"));
        verify(userRepository, never()).findByUsernameIgnoreCase(any());
    }

    @Test
    void login_withUsername_returnsToken() {
        // Arrange
        User bob = new User("Bob", "bob@mail.fr", "hash");
        when(userRepository.findByEmail("bob")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("bob")).thenReturn(Optional.of(bob));
        when(passwordEncoder.matches("Secret1!", "hash")).thenReturn(true);
        when(jwtService.generateToken(bob)).thenReturn("jwt");

        // Act
        AuthResponse response = authService.login(new LoginRequest(" bob ", "Secret1!"));

        // Assert
        assertThat(response).isEqualTo(new AuthResponse("jwt"));
    }

    @Test
    void login_prefersEmailAccountWhenUsernameEqualsAnotherEmail() {
        // Arrange : le nom d'utilisateur de « autre » est identique à l'e-mail de « bob »
        User emailUser = new User("bob", "bob@mail.fr", "hash");
        User usernameUser = new User("bob@mail.fr", "autre@mail.fr", "hash");
        when(userRepository.findByEmail("bob@mail.fr")).thenReturn(Optional.of(emailUser));
        lenient().when(userRepository.findByUsernameIgnoreCase("bob@mail.fr")).thenReturn(Optional.of(usernameUser));
        when(passwordEncoder.matches("Secret1!", "hash")).thenReturn(true);

        // Act
        authService.login(new LoginRequest("bob@mail.fr", "Secret1!"));

        // Assert
        verify(jwtService).generateToken(emailUser);
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        // Arrange
        when(userRepository.findByEmail("bob@mail.fr")).thenReturn(Optional.of(new User("bob", "bob@mail.fr", "hash")));
        when(passwordEncoder.matches("Mauvais1!", "hash")).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(new LoginRequest("bob@mail.fr", "Mauvais1!")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(LOGIN_ERROR);
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_withUnknownUser_throwsInvalidCredentials() {
        // Arrange
        when(userRepository.findByEmail("inconnu")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("inconnu")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> authService.login(new LoginRequest("inconnu", "Secret1!")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(LOGIN_ERROR);
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_withPasswordOver72Bytes_throwsInvalidCredentials() {
        // Arrange : 39 caractères mais 74 octets ; BCrypt ne lit que 72 octets
        when(userRepository.findByEmail("bob@mail.fr")).thenReturn(Optional.of(new User("bob", "bob@mail.fr", "hash")));

        // Act + Assert
        assertThatThrownBy(() -> authService.login(new LoginRequest("bob@mail.fr", "Aa1!" + "é".repeat(35))))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(passwordEncoder, never()).matches(any(), any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginRequest_toStringNeverContainsThePassword() {
        assertThat(new LoginRequest("bob", "Secret1!").toString()).doesNotContain("Secret1!");
    }
}

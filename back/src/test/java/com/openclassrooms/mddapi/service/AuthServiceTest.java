package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.AccountAlreadyExistsException;
import com.openclassrooms.mddapi.exception.EmailAlreadyUsedException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
}

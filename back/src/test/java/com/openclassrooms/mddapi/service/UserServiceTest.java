package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.UpdateProfileRequest;
import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.AccountAlreadyExistsException;
import com.openclassrooms.mddapi.exception.EmailAlreadyUsedException;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.exception.UsernameAlreadyUsedException;
import com.openclassrooms.mddapi.mapper.UserMapper;
import com.openclassrooms.mddapi.mapper.UserMapperImpl;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 7L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    /** Vrai mapper généré par MapStruct : on teste aussi la conversion. */
    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @InjectMocks
    private UserService userService;

    private User bob;

    @BeforeEach
    void setUp() {
        bob = new User("bob", "bob@mail.fr", "old-hash");
        ReflectionTestUtils.setField(bob, "id", USER_ID);
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(bob));
        lenient().when(userRepository.saveAndFlush(any(User.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void getMe_returnsProfile() {
        assertThat(userService.getMe(USER_ID)).isEqualTo(new UserDto(USER_ID, "bob", "bob@mail.fr"));
    }

    @Test
    void getMe_unknownUser_throwsUnknownUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(USER_ID)).isInstanceOf(UnknownUserException.class);
    }

    @Test
    void updateMe_withoutPassword_keepsHash() {
        // Act
        UserDto result = userService.updateMe(USER_ID, new UpdateProfileRequest("bobby", "bobby@mail.fr", null));

        // Assert
        assertThat(result).isEqualTo(new UserDto(USER_ID, "bobby", "bobby@mail.fr"));
        assertThat(bob.getPassword()).isEqualTo("old-hash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateMe_withPassword_rehashes() {
        // Arrange
        when(passwordEncoder.encode("Nouveau1!")).thenReturn("new-hash");

        // Act
        userService.updateMe(USER_ID, new UpdateProfileRequest("bob", "bob@mail.fr", "Nouveau1!"));

        // Assert
        assertThat(bob.getPassword()).isEqualTo("new-hash");
    }

    @Test
    void updateMe_normalizesEmailAndKeepsOwnEmail() {
        // Act : son propre e-mail, écrit autrement
        UserDto result = userService.updateMe(USER_ID, new UpdateProfileRequest("bob", "  BOB@Mail.FR ", null));

        // Assert : pas de conflit avec soi-même, e-mail en minuscules
        verify(userRepository).existsByEmailAndIdNot("bob@mail.fr", USER_ID);
        assertThat(result.email()).isEqualTo("bob@mail.fr");
    }

    @Test
    void updateMe_emailOfAnotherAccount_throwsEmailAlreadyUsed() {
        // Arrange
        when(userRepository.existsByEmailAndIdNot("alice@mail.fr", USER_ID)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> userService.updateMe(USER_ID, new UpdateProfileRequest("bob", "alice@mail.fr", null)))
                .isInstanceOf(EmailAlreadyUsedException.class);
        assertThat(bob.getEmail()).isEqualTo("bob@mail.fr");
    }

    @Test
    void updateMe_usernameOfAnotherAccount_throwsUsernameAlreadyUsed() {
        // Arrange
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("alice", USER_ID)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> userService.updateMe(USER_ID, new UpdateProfileRequest("alice", "bob@mail.fr", null)))
                .isInstanceOf(UsernameAlreadyUsedException.class);
        assertThat(bob.getUsername()).isEqualTo("bob");
    }

    @Test
    void updateMe_concurrentDuplicate_throwsAccountAlreadyExists() {
        // Arrange : un autre compte prend le même e-mail au même instant
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act + Assert
        assertThatThrownBy(() -> userService.updateMe(USER_ID, new UpdateProfileRequest("bob", "new@mail.fr", null)))
                .isInstanceOf(AccountAlreadyExistsException.class);
    }

    @Test
    void updateMe_unknownUser_throwsUnknownUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateMe(USER_ID, new UpdateProfileRequest("bob", "bob@mail.fr", null)))
                .isInstanceOf(UnknownUserException.class);
    }

    @Test
    void updateProfileRequest_blankPasswordBecomesNull() {
        assertThat(new UpdateProfileRequest("bob", "bob@mail.fr", "   ").password()).isNull();
        assertThat(new UpdateProfileRequest("bob", "bob@mail.fr", "").password()).isNull();
    }

    @Test
    void updateProfileRequest_toStringHidesPassword() {
        assertThat(new UpdateProfileRequest("bob", "bob@mail.fr", "Secret1!").toString()).doesNotContain("Secret1!");
    }
}

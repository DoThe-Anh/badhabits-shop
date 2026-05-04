package com.shop.service;

import com.shop.entity.Role;
import com.shop.entity.User;
import com.shop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService service;

    private static User user(Long id, String email, Role role) {
        User u = new User(email, "hashed", "User " + id, "", "", role, Instant.now());
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    // ---------- register ----------

    @Test
    void register_happyPath_savesNormalizedEmailAndEncodedPassword() {
        when(userRepository.existsByEmailIgnoreCase("test@bh.vn")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.register("  Test@BH.vn  ", "secret123", "Anh", "0900", "HCM");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.email()).isEqualTo("test@bh.vn");
        assertThat(saved.passwordHash()).isEqualTo("HASH");
        assertThat(saved.fullName()).isEqualTo("Anh");
        assertThat(saved.role()).isEqualTo(Role.USER);
        assertThat(result).isSameAs(saved);
    }

    @Test
    void register_emptyEmail_throws() {
        assertThatThrownBy(() -> service.register("  ", "secret123", "x", "", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_nullEmail_throws() {
        assertThatThrownBy(() -> service.register(null, "secret123", "x", "", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmailIgnoreCase("dup@bh.vn")).thenReturn(true);

        assertThatThrownBy(() -> service.register("dup@bh.vn", "secret123", "x", "", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã được sử dụng");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_passwordTooShort_throws() {
        when(userRepository.existsByEmailIgnoreCase("a@b.vn")).thenReturn(false);

        assertThatThrownBy(() -> service.register("a@b.vn", "12345", "x", "", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ít nhất 6");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_nullPassword_throws() {
        when(userRepository.existsByEmailIgnoreCase("a@b.vn")).thenReturn(false);

        assertThatThrownBy(() -> service.register("a@b.vn", null, "x", "", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- changeRole ----------

    @Test
    void changeRole_promotesUserToAdmin() {
        User target = user(2L, "u@bh.vn", Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        User result = service.changeRole(2L, Role.ADMIN, "admin@bh.vn");

        assertThat(result.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void changeRole_actorEqualsTarget_throws() {
        User self = user(1L, "admin@bh.vn", Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> service.changeRole(1L, Role.USER, "admin@bh.vn"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("chính mình");
    }

    @Test
    void changeRole_demoteLastAdmin_throws() {
        User onlyAdmin = user(1L, "admin@bh.vn", Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(onlyAdmin));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.changeRole(1L, Role.USER, "other@bh.vn"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("admin cuối cùng");
    }

    @Test
    void changeRole_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeRole(99L, Role.USER, "x@y.vn"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("#99");
    }

    // ---------- setEnabled ----------

    @Test
    void setEnabled_disablesUser() {
        User target = user(2L, "u@bh.vn", Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        User result = service.setEnabled(2L, false, "admin@bh.vn");

        assertThat(result.enabled()).isFalse();
    }

    @Test
    void setEnabled_disableSelf_throws() {
        User self = user(1L, "admin@bh.vn", Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> service.setEnabled(1L, false, "admin@bh.vn"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("chính mình");
    }

    // ---------- findByEmail ----------

    @Test
    void findByEmail_nullReturnsEmpty() {
        assertThat(service.findByEmail(null)).isEmpty();
        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    void findByEmail_trimsAndDelegates() {
        User u = user(1L, "x@y.vn", Role.USER);
        when(userRepository.findByEmailIgnoreCase("x@y.vn")).thenReturn(Optional.of(u));

        assertThat(service.findByEmail("  x@y.vn  ")).contains(u);
    }
}

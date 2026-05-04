package com.shop.service;

import com.shop.entity.Role;
import com.shop.entity.User;
import com.shop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email, String rawPassword,
                         String fullName, String phone, String address) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }
        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                fullName == null ? "" : fullName.trim(),
                phone == null ? "" : phone.trim(),
                address == null ? "" : address.trim(),
                Role.USER,
                Instant.now()
        );
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    public List<User> findAll() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public User changeRole(Long userId, Role newRole, String actorEmail) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user #" + userId));

        if (target.email().equalsIgnoreCase(actorEmail)) {
            throw new IllegalArgumentException("Không thể thay đổi vai trò của chính mình.");
        }

        // Prevent demoting the last admin — keep at least one admin in the system.
        if (target.role() == Role.ADMIN && newRole != Role.ADMIN
                && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("Không thể demote admin cuối cùng.");
        }

        target.setRole(newRole);
        return target;
    }

    @Transactional
    public User setEnabled(Long userId, boolean enabled, String actorEmail) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user #" + userId));

        if (target.email().equalsIgnoreCase(actorEmail)) {
            throw new IllegalArgumentException("Không thể khoá tài khoản của chính mình.");
        }

        target.setEnabled(enabled);
        return target;
    }
}

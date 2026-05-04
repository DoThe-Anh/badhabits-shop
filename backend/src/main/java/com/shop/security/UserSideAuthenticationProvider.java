package com.shop.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Authenticates /login (customer side) submissions. Accounts carrying
 * ROLE_ADMIN are rejected so admins must use /admin/login; customer login
 * stays customer-only.
 *
 * Not a Spring-managed bean — constructed in SecurityConfig and wired only
 * into the user chain's ProviderManager.
 */
public class UserSideAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public UserSideAuthenticationProvider(UserDetailsService userDetailsService,
                                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String rawPassword = authentication.getCredentials() == null
                ? ""
                : authentication.getCredentials().toString();

        UserDetails details = userDetailsService.loadUserByUsername(email);

        if (!passwordEncoder.matches(rawPassword, details.getPassword())) {
            throw new BadCredentialsException("Sai email hoặc mật khẩu.");
        }

        if (!details.isEnabled()) {
            throw new DisabledException("Tài khoản đã bị khoá.");
        }
        if (!details.isAccountNonLocked()) {
            throw new LockedException("Tài khoản đã bị khoá.");
        }

        boolean isAdmin = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            throw new BadCredentialsException(
                    "Tài khoản admin — vui lòng đăng nhập tại /admin/login.");
        }

        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

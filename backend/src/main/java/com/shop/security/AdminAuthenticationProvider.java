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
 * Authenticates /admin/login submissions. Only accounts carrying ROLE_ADMIN
 * succeed — users without the admin role get a BadCredentialsException so the
 * admin login form keeps admin-only, separate from the customer /login.
 *
 * NOT a Spring-managed bean — constructed by SecurityConfig and wired only
 * into the admin ProviderManager, so the default user chain's auth flow is
 * unaffected.
 */
public class AdminAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthenticationProvider(UserDetailsService userDetailsService,
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
        if (!isAdmin) {
            throw new BadCredentialsException("Tài khoản này không có quyền admin.");
        }

        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

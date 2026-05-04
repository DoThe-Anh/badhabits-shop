package com.shop.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Two parallel authentication sessions in the SAME browser:
 * - Admin chain  → /admin/**  → uses session attribute "BH_ADMIN_CONTEXT"
 * - User  chain  → everything → uses session attribute "BH_USER_CONTEXT"
 * Same JSESSIONID cookie, different SecurityContext storage. Logging out one
 * side leaves the other side untouched.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN_CTX_KEY = "BH_ADMIN_CONTEXT";
    private static final String USER_CTX_KEY = "BH_USER_CONTEXT";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ---------- Admin chain ----------
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http,
                                                UserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder)
            throws Exception {
        HttpSessionSecurityContextRepository adminRepo = new HttpSessionSecurityContextRepository();
        adminRepo.setSpringSecurityContextKey(ADMIN_CTX_KEY);

        // Build the provider locally so it stays OFF the global AuthenticationManager
        // (otherwise the user chain would also reject non-admin logins).
        AdminAuthenticationProvider adminProvider =
                new AdminAuthenticationProvider(userDetailsService, passwordEncoder);
        AuthenticationManager adminAuthManager = new ProviderManager(adminProvider);

        http
                .securityMatcher("/admin", "/admin/**")
                .securityContext(sc -> sc.securityContextRepository(adminRepo))
                .authenticationManager(adminAuthManager)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/admin/login")))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/admin/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        // Don't invalidate HTTP session — user side may be active
                        .invalidateHttpSession(false)
                        .addLogoutHandler((req, res, authn) -> {
                            HttpSession s = req.getSession(false);
                            if (s != null) s.removeAttribute(ADMIN_CTX_KEY);
                        })
                        .permitAll()
                )
                // CSRF stays on — admin login form embeds the token.
                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")));

        return http.build();
    }

    // ---------- User chain ----------
    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http,
                                               UserDetailsService userDetailsService,
                                               PasswordEncoder passwordEncoder,
                                               CartLogoutHandler cartLogoutHandler) throws Exception {
        HttpSessionSecurityContextRepository userRepo = new HttpSessionSecurityContextRepository();
        userRepo.setSpringSecurityContextKey(USER_CTX_KEY);

        UserSideAuthenticationProvider userProvider =
                new UserSideAuthenticationProvider(userDetailsService, passwordEncoder);
        AuthenticationManager userAuthManager = new ProviderManager(userProvider);

        http
                .securityContext(sc -> sc.securityContextRepository(userRepo))
                .authenticationManager(userAuthManager)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/shop/**", "/product/**",
                                "/login", "/register",
                                "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                "/error",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/cart").permitAll()
                        .requestMatchers(HttpMethod.POST, "/cart/add", "/cart/update", "/cart/remove")
                        .authenticated()
                        .requestMatchers("/checkout", "/checkout/**").authenticated()
                        .requestMatchers("/account/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/login?requireLogin")))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        // Don't kill session — admin side may be active
                        .invalidateHttpSession(false)
                        .addLogoutHandler(cartLogoutHandler)
                        .addLogoutHandler((req, res, authn) -> {
                            HttpSession s = req.getSession(false);
                            if (s != null) s.removeAttribute(USER_CTX_KEY);
                        })
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/cart/add", "/cart/update", "/cart/remove")
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                )
                .headers(h -> h.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }
}

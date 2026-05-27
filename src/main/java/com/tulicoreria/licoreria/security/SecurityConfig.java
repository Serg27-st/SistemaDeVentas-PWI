package com.tulicoreria.licoreria.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.tulicoreria.licoreria.service.impl.UsuarioServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioServiceImpl usuarioServiceImpl;
    // Spring inyectará automáticamente el Bean que definimos en PasswordConfig
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/vendor/**").permitAll()
                .requestMatchers("/login", "/error").permitAll()
                .requestMatchers("/inicio", "/catalogo", "/producto/**").permitAll()
                .requestMatchers("/carrito", "/carrito/**").permitAll()
                .requestMatchers("/dashboard").authenticated()
                .requestMatchers("/ventas/**").hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers("/productos/**").hasAnyRole("ADMIN", "ALMACEN")
                .requestMatchers("/categorias/**").hasRole("ADMIN")
                .requestMatchers("/proveedores/**").hasAnyRole("ADMIN", "ALMACEN")
                .requestMatchers("/clientes/**").hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers("/ordenes/**").hasAnyRole("ADMIN", "ALMACEN")
                .requestMatchers("/kardex/**").hasAnyRole("ADMIN", "ALMACEN")
                .requestMatchers("/reportes/**").hasRole("ADMIN")
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }

    // ── CONFIGURACIÓN CORREGIDA ──────────────────────────────────────────────

@Bean
public DaoAuthenticationProvider authenticationProvider() {
    // 1. Pasamos la variable al constructor para eliminar el primer error
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioServiceImpl);
    
    // 2. Usamos el PasswordEncoder inyectado
    provider.setPasswordEncoder(this.passwordEncoder);
    
    return provider;
}

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
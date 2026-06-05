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
    private final ClienteWebUserDetailsService clienteWebUserDetailsService;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // Los endpoints /api/** son AJAX de mismo origen — excluimos CSRF
                // para que fetch() funcione sin necesidad de token en header/body
                .ignoringRequestMatchers("/api/**")
            )
            .authenticationProvider(authenticationProvider())
            .authenticationProvider(clienteAuthProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/vendor/**").permitAll()
                .requestMatchers("/login", "/error").permitAll()
                .requestMatchers("/registro", "/registro/**").permitAll()
                .requestMatchers("/inicio", "/catalogo", "/producto/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/carrito", "/carrito/**").permitAll()
                .requestMatchers("/combos", "/combos/**").permitAll()
                .requestMatchers("/checkout", "/checkout/**").permitAll()
                .requestMatchers("/promociones/**").hasRole("ADMIN")
                .requestMatchers("/reclamaciones", "/reclamaciones/").permitAll()
                .requestMatchers("/reclamaciones/admin", "/reclamaciones/*/atender").hasRole("ADMIN")
                .requestMatchers("/mi-cuenta", "/mi-cuenta/**").hasRole("CLIENTE")
                .requestMatchers("/dashboard").hasAnyRole("ADMIN", "VENDEDOR", "ALMACEN")
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
                .successHandler(customAuthSuccessHandler)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/inicio?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioServiceImpl);
        provider.setPasswordEncoder(this.passwordEncoder);
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider clienteAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(clienteWebUserDetailsService);
        provider.setPasswordEncoder(this.passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
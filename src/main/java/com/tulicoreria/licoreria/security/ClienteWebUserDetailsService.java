package com.tulicoreria.licoreria.security;

import com.tulicoreria.licoreria.model.ClienteWeb;
import com.tulicoreria.licoreria.repository.ClienteWebRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteWebUserDetailsService implements UserDetailsService {

    private final ClienteWebRepository clienteWebRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ClienteWeb cw = clienteWebRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado: " + email));

        if (!cw.isActivo()) {
            throw new UsernameNotFoundException("Cuenta de cliente inactiva: " + email);
        }

        return new User(cw.getEmail(), cw.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }
}

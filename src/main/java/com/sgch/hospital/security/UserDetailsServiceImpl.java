package com.sgch.hospital.security;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sgch.hospital.model.entity.Usuario;
import com.sgch.hospital.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService{

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
        
        // Construimos la lista de roles/permisos
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(usuario.getRol().name())
        );
        
        // Retornamos el objeto UserDetails que Spring Security necesita
        return new User(
            usuario.getEmail(),
            usuario.getPassword(),
            authorities
        );
    }

    
}

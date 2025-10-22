package com.sgch.hospital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sgch.hospital.security.UserDetailsServiceImpl;
import com.sgch.hospital.security.jwt.AuthEntryPointJwt;
import com.sgch.hospital.security.jwt.AuthTokenFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity // Permite seguridad basada en anotaciones (@PreAuthorize)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    
    // Inyección del filtro JWT (se creará más adelante)
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
      return new AuthTokenFilter();
    }

    // Bean para cifrar contraseñas (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Proveedor de autenticación que usa nuestro servicio de usuario y el cifrador
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Gestor de autenticación
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
      return authConfig.getAuthenticationManager();
    }

    // Cadena de Filtros de Seguridad (Define qué URLs proteger y cómo)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para APIs REST
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) // Manejo de errores de autenticación
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Usamos JWT, no sesiones
            .authorizeHttpRequests(auth -> 
                auth.requestMatchers("/api/auth/**").permitAll() // Permitir registro y login sin autenticar
                    .requestMatchers("/api/admin/**").hasAuthority("ADMINISTRADOR")
                    .requestMatchers("/api/admin/horario-doctor").hasAuthority("ADMINISTRADOR")  // Solo Administradores
                    .requestMatchers("/api/doctor/**").hasAnyAuthority("DOCTOR", "ADMINISTRADOR") // Doctores y Admins
                    .anyRequest().authenticated() // Cualquier otra petición requiere autenticación
            );
        
        http.authenticationProvider(authenticationProvider());
        
        // Añadir el filtro JWT antes del filtro estándar
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

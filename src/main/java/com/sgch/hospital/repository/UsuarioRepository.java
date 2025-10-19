package com.sgch.hospital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgch.hospital.model.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{ 


    // Método necesario para la autenticación (Spring Security)
    Optional<Usuario> findByEmail(String email);

    // Método para verificar la existencia del DNI (Administrador y registro)
    boolean existsByDni(String dni);
}

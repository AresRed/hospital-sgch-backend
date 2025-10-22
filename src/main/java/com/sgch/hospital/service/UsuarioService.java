package com.sgch.hospital.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sgch.hospital.model.DTO.RegistroRequest;
import com.sgch.hospital.model.entity.Usuario;
import com.sgch.hospital.model.entity.Usuario.Rol;
import com.sgch.hospital.repository.UsuarioRepository;
import com.sgch.hospital.util.mail.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; 
    private final EmailService emailService; 

    public Usuario registrarNuevoUsuario(Usuario usuario, Rol rol) throws Exception {
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new Exception("El DNI ya está registrado.");
        }
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new Exception("El email ya está registrado.");
        }

        usuario.setRol(rol);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        String token = "TOKEN_DE_EJEMPLO";
        String mensaje = String.format(
                "Bienvenido a SGCH. Por favor verifica tu cuenta: http://localhost:8080/api/auth/verify?token=%s",
                token);
        emailService.enviarCorreo(nuevoUsuario.getEmail(), "Verificación de Cuenta SGCH", mensaje);

        return nuevoUsuario;
    }

    public Usuario findByEmail(String email) { 
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Usuario convertirYRegistrar(RegistroRequest req) throws Exception {
        Usuario.Rol rol;
        try {
            rol = Rol.valueOf(req.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Rol no válido.");
        }

        Usuario usuario = new Usuario(); 
        usuario.setDni(req.getDni());
        usuario.setNombre(req.getNombre());
        usuario.setEmail(req.getEmail());
        usuario.setPassword(req.getPassword());

        return registrarNuevoUsuario(usuario, rol);
    }
}

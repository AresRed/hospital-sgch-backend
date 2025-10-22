package com.sgch.hospital.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sgch.hospital.model.DTO.JwtResponse;
import com.sgch.hospital.model.DTO.LoginRequest;
import com.sgch.hospital.model.DTO.RegistroRequest;
import com.sgch.hospital.model.entity.Usuario;
import com.sgch.hospital.security.jwt.JwtUtils;
import com.sgch.hospital.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        // 1. Autenticar las credenciales
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // 2. Establecer el contexto de seguridad y generar el JWT
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 3. Obtener detalles del usuario para la respuesta
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        try{  Usuario usuario = usuarioService.findByEmail(userDetails.getUsername()); 

            // 4. Devolver el JWT y los datos del usuario al cliente
            return ResponseEntity.ok(new JwtResponse(
                jwt,
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol().name(),
                "Bearer"
            ));}catch(Exception e){
                return ResponseEntity
                    .status(500)
                    .body("error interno al obtener los datos del usuario"+e.getMessage());
            }
       
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registerUser(@RequestBody RegistroRequest registroRequest) {
        try {
            // Convertir DTO a la entidad Usuario y determinar el rol
            Usuario nuevoUsuario = usuarioService.convertirYRegistrar(registroRequest); // Asumimos un método auxiliar para esto
            
            return ResponseEntity.ok("Usuario registrado exitosamente. Se ha enviado un correo de verificación.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar usuario: " + e.getMessage());
        }
    }
    // Pendiente: Endpoint para verificar el correo /api/auth/verify?token=...

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // En una implementación avanzada, podrías añadir el token a una 'lista negra' 
        // (blacklist) de Redis para invalidarlo inmediatamente antes de que expire.
        
        // Para este proyecto, simplemente confirmamos la acción al cliente.
        return ResponseEntity
            .status(HttpStatus.OK)
            .body("Sesión cerrada exitosamente. Por favor, elimine su token JWT.");
    }
}

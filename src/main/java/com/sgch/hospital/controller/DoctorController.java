package com.sgch.hospital.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.Usuario;
import com.sgch.hospital.service.CitaService;
import com.sgch.hospital.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DOCTOR') or hasAuthority('ADMINISTRADOR')") 
public class DoctorController {

    private final CitaService citaService;
    private final UsuarioService usuarioService;
    // private final ExpedienteService expedienteService; // Lo usaremos para el análisis con Python

    private Usuario getAuthenticatedUser() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioService.findByEmail(auth.getName());
    }

    /**
     * ENDPOINT: Ver mi agenda (citas pendientes)
     */
    @GetMapping("/agenda")
    public ResponseEntity<?> obtenerMiAgenda() {
        try {
            Usuario usuario = getAuthenticatedUser();
            if (!(usuario instanceof Doctor)) {
                return ResponseEntity.status(403).body("Acceso denegado: No es un doctor.");
            }
            
            var agenda = citaService.obtenerCitasPorDoctor(usuario.getId());
            return ResponseEntity.ok(agenda);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

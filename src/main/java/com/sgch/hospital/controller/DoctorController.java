package com.sgch.hospital.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sgch.hospital.model.DTO.ExpedienteUpdateDTO;
import com.sgch.hospital.model.DTO.FinalizarCitaRequest;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.Expediente;
import com.sgch.hospital.model.entity.NotaMedica;
import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.model.entity.Receta;
import com.sgch.hospital.model.entity.Usuario;
import com.sgch.hospital.repository.PacienteRepository;
import com.sgch.hospital.service.CitaService;
import com.sgch.hospital.service.ExpedienteService;
import com.sgch.hospital.service.RecetaPdfService;
import com.sgch.hospital.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DOCTOR') or hasAuthority('ADMINISTRADOR')")
public class DoctorController {

    private final CitaService citaService;
    private final UsuarioService usuarioService;
    private final ExpedienteService expedienteService;
    private final RecetaPdfService recetaPdfService;
    private final PacienteRepository pacienteRepository;

    private Usuario getAuthenticatedUser() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioService.findByEmail(auth.getName());
    }

    private Doctor getAuthenticatedDoctor() throws Exception {
        Usuario usuario = getAuthenticatedUser();
        if (!(usuario instanceof Doctor)) {
            throw new Exception("El usuario autenticado no es un doctor.");
        }
        return (Doctor) usuario;
    }

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

    @PostMapping("/citas/{citaId}/finalizar")
    public ResponseEntity<?> finalizarCita(
            @PathVariable Long citaId,
            @Valid @RequestBody FinalizarCitaRequest request) {
        try {
            Doctor doctor = getAuthenticatedDoctor();
            NotaMedica nota = expedienteService.finalizarYGuardarExpediente(citaId, doctor, request);

            if (nota == null) {
                return ResponseEntity.ok("Cita marcada como AUSENTE.");
            }
            return ResponseEntity.ok(nota); // Devuelve la nota médica creada
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al finalizar la cita: " + e.getMessage());
        }
    }

    @GetMapping("/expediente/{pacienteId}")
    public ResponseEntity<?> obtenerHistorialPaciente(@PathVariable Long pacienteId) {
        try {
            // Validación de rol (ya cubierta por @PreAuthorize)
            // Se puede añadir lógica para verificar si el paciente existe.

            // Lógica en ExpedienteService para obtener el Expediente completo
            Expediente expediente = expedienteService.obtenerExpedientePorPacienteId(pacienteId);

            return ResponseEntity.ok(expediente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/receta/{recetaId}/pdf")
    public ResponseEntity<?> imprimirRecetaPdf(@PathVariable Long recetaId) {
        try {
            Doctor doctor = getAuthenticatedDoctor();

            // Obtener la receta (con validación de que pertenece a una cita del doctor)
            Receta receta = expedienteService.obtenerRecetaPorId(recetaId);

            // Validar que el doctor tiene acceso a esta receta
            if (!receta.getNotaMedica().getDoctor().getId().equals(doctor.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tiene permisos para acceder a esta receta.");
            }

            // Generar PDF
            byte[] pdfBytes = recetaPdfService.generarPdfReceta(receta);

            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "receta_" + receta.getId() + "_" + receta.getNotaMedica().getExpediente().getPaciente().getDni()
                            + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar PDF: " + e.getMessage());
        }
    }

    @GetMapping("/receta/{recetaId}/html")
    public ResponseEntity<?> verRecetaHtml(@PathVariable Long recetaId) {
        try {
            Doctor doctor = getAuthenticatedDoctor();

            // Obtener la receta
            Receta receta = expedienteService.obtenerRecetaPorId(recetaId);

            // Validar acceso
            if (!receta.getNotaMedica().getDoctor().getId().equals(doctor.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tiene permisos para acceder a esta receta.");
            }

            // Generar HTML
            String html = recetaPdfService.generarHtmlReceta(receta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(html);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar vista HTML: " + e.getMessage());
        }
    }

    @GetMapping("/pacientes")
    public ResponseEntity<?> listarPacientes() {
        List<Paciente> pacientes = pacienteRepository.findAll();

        // NOTA: En producción, usar DTOs para evitar exponer contraseñas/detalles.
        if (pacientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pacientes);
    }

    @PutMapping("/expediente/{pacienteId}/datos-fijos")
    public ResponseEntity<?> actualizarDatosFijosExpediente(
            @PathVariable Long pacienteId,
            @RequestBody ExpedienteUpdateDTO dto) {
        try {
            // Validación: Solo el Doctor o Admin puede modificar esta info
            getAuthenticatedDoctor();

            Expediente expediente = expedienteService.actualizarDatosFijos(pacienteId, dto);
            return ResponseEntity.ok(expediente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar expediente: " + e.getMessage());
        }
    }

}
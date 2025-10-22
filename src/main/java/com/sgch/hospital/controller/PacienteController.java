package com.sgch.hospital.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sgch.hospital.model.DTO.AgendarCitaRequest;
import com.sgch.hospital.model.DTO.CitaReprogramarRequest;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.model.entity.Usuario;
import com.sgch.hospital.service.CitaService;
import com.sgch.hospital.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/paciente")
@RequiredArgsConstructor
public class PacienteController {

    private final CitaService citaService;
    private final UsuarioService usuarioService;

    // Método auxiliar para obtener el usuario autenticado (se usará en todos los
    // controladores)
    private Usuario getAuthenticatedUser() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioService.findByEmail(auth.getName()); // 'auth.getName()' es el email en este caso
    }

    /**
     * ENDPOINT: Agendar una cita.
     * Aquí se activa la lógica del Paradigma Lógico (Prolog)
     */
    @PostMapping("/citas/agendar")
    public ResponseEntity<?> agendarCita(@RequestBody AgendarCitaRequest request) {
        try {
            Usuario usuario = getAuthenticatedUser();
            if (!(usuario instanceof Paciente)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo pacientes.");
            }

            // Llama al servicio que delega en Prolog para encontrar el horario óptimo
            var nuevaCita = citaService.agendarCita(
                    request.getDoctorId(),
                    (Paciente) usuario,
                    request.getFechaCandidata(),
                    request.getMotivo(),
                    request.getHoraSeleccionada());

            return ResponseEntity.ok(nuevaCita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al agendar cita: " + e.getMessage());
        }
    }

    /**
     * ENDPOINT: Ver citas del paciente.
     */
    @GetMapping("/citas")
    public ResponseEntity<?> obtenerMisCitas() {
        try {
            Usuario usuario = getAuthenticatedUser();
            if (!(usuario instanceof Paciente)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
            }

            var citas = citaService.obtenerCitasPorPaciente(usuario.getId());
            return ResponseEntity.ok(citas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/citas/horarios")
    public ResponseEntity<?> getHorariosDisponibles(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            // Aseguramos que el usuario esté logueado (Spring Security ya lo hizo)
            // Y que el Doctor exista (lo verifica el service)

            List<String> horarios = citaService.obtenerHorariosDisponibles(doctorId, fecha);

            if (horarios.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("No hay horarios disponibles para esta fecha.");
            }

            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al buscar horarios: " + e.getMessage());
        }
    }

    @DeleteMapping("/citas/cancelar/{citaId}")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<?> cancelarCita(@PathVariable Long citaId) {
        try {
            Cita citaCancelada = citaService.cancelarCita(citaId);

            // Retornamos la cita cancelada con su nuevo estado
            return ResponseEntity.ok(citaCancelada);
        } catch (IllegalArgumentException e) {
            // Captura el error si intentan cancelar una cita ya realizada
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cancelar la cita: " + e.getMessage());
        }
    }

    @PutMapping("/citas/postergar/{citaId}")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<?> postergarCita(
            @PathVariable Long citaId,
            @Valid @RequestBody CitaReprogramarRequest request) {
        try {
            // El servicio maneja la lógica: cancelar la antigua y agendar la nueva usando
            // Prolog
            Cita nuevaCita = citaService.postergarCita(
                    citaId,
                    request.getNuevoDoctorId(),
                    request.getNuevaFecha(),
                    request.getNuevoMotivo(),
                    request.getNuevaHora());
            // Retornamos la nueva cita programada
            return ResponseEntity.ok(nuevaCita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al postergar la cita: " + e.getMessage());
        }
    }
}

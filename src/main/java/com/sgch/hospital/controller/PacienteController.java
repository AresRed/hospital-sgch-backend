package com.sgch.hospital.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.sgch.hospital.model.DTO.DoctorListDTO;
import com.sgch.hospital.model.DTO.PacienteUpdateDTO;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Especialidad;
import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.model.entity.Receta;
import com.sgch.hospital.model.entity.Usuario;
import com.sgch.hospital.repository.EspecialidadRepository;
import com.sgch.hospital.service.CitaService;
import com.sgch.hospital.service.DoctorService;
import com.sgch.hospital.service.ExpedienteService;
import com.sgch.hospital.service.RecetaPdfService;
import com.sgch.hospital.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/paciente")
@RequiredArgsConstructor
public class PacienteController {

    private final CitaService citaService;
    private final UsuarioService usuarioService;
    private final ExpedienteService expedienteService;
    private final RecetaPdfService recetaPdfService;
    private final DoctorService doctorService;
    private final EspecialidadRepository especialidadRepository;

    private Usuario getAuthenticatedUser() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioService.findByEmail(auth.getName()); // 'auth.getName()' es el email en este caso
    }

    @PostMapping("/citas/agendar")
    public ResponseEntity<?> agendarCita(@RequestBody AgendarCitaRequest request) {
        try {
            Usuario usuario = getAuthenticatedUser();
            if (!(usuario instanceof Paciente)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo pacientes.");
            }

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

            return ResponseEntity.ok(citaCancelada);
        } catch (IllegalArgumentException e) {
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
            Cita nuevaCita = citaService.postergarCita(
                    citaId,
                    request.getNuevoDoctorId(),
                    request.getNuevaFecha(),
                    request.getNuevoMotivo(),
                    request.getNuevaHora());
            return ResponseEntity.ok(nuevaCita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al postergar la cita: " + e.getMessage());
        }
    }

    @GetMapping("/historial/ultima-receta")
    public ResponseEntity<?> obtenerUltimaReceta() {
        try {
            Usuario usuario = getAuthenticatedUser();

            if (!(usuario instanceof Paciente)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo pacientes.");
            }

            // La lógica en ExpedienteService busca la receta más reciente
            Receta receta = expedienteService.obtenerUltimaReceta(usuario.getId());

            return ResponseEntity.ok(receta);
        } catch (Exception e) {
            // Si no hay expediente o receta en el historial
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(@Valid @RequestBody PacienteUpdateDTO dto) {
        try {
            Usuario usuario = getAuthenticatedUser();
            // Llamada al servicio que actualiza en la DB
            usuarioService.actualizarPerfilPaciente(usuario.getId(), dto);
            return ResponseEntity.ok("Perfil actualizado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar perfil: " + e.getMessage());
        }
    }

    @GetMapping("/receta/{recetaId}/pdf")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<?> descargarRecetaPdf(@PathVariable Long recetaId) {
        try {
            Usuario usuario = getAuthenticatedUser();
            if (!(usuario instanceof Paciente)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo pacientes.");
            }

            Paciente paciente = (Paciente) usuario;

            // Obtener la receta
            Receta receta = expedienteService.obtenerRecetaPorId(recetaId);

            // Validar que la receta pertenece al paciente autenticado
            if (!receta.getNotaMedica().getExpediente().getPaciente().getId().equals(paciente.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tiene permisos para acceder a esta receta.");
            }

            // Generar PDF
            byte[] pdfBytes = recetaPdfService.generarPdfReceta(receta);

            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "mi_receta_" + receta.getId() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar PDF: " + e.getMessage());
        }
    }

    @GetMapping("/receta/{recetaId}/html")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<?> verRecetaHtml(@PathVariable Long recetaId) {
        try {
            Usuario usuario = getAuthenticatedUser();
            if (!(usuario instanceof Paciente)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo pacientes.");
            }

            Paciente paciente = (Paciente) usuario;

            // Obtener la receta
            Receta receta = expedienteService.obtenerRecetaPorId(recetaId);

            // Validar que la receta pertenece al paciente autenticado
            if (!receta.getNotaMedica().getExpediente().getPaciente().getId().equals(paciente.getId())) {
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

    @GetMapping("/recetas")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<?> obtenerMisRecetas() {
        try {
            Usuario usuario = getAuthenticatedUser();
            if (!(usuario instanceof Paciente)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo pacientes.");
            }

            Paciente paciente = (Paciente) usuario;

            // Obtener todas las recetas del paciente
            List<Receta> recetas = expedienteService.obtenerTodasLasRecetasPorPaciente(paciente.getId());

            return ResponseEntity.ok(recetas);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener recetas: " + e.getMessage());
        }
    }

    @GetMapping("/doctores")
    public ResponseEntity<List<DoctorListDTO>> listarDoctores() {
        List<DoctorListDTO> doctores = doctorService.listarDoctoresDisponibles();

        if (doctores.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(doctores);
    }

    @GetMapping("/especialidades")
    public ResponseEntity<List<Especialidad>> listarEspecialidades() {
        List<Especialidad> especialidades = especialidadRepository.findAll();
        return especialidades.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(especialidades);
    }

    @GetMapping("/historial/recetas")
    public ResponseEntity<?> obtenerTodasLasRecetas() {
        try {
            Usuario usuario = getAuthenticatedUser();
            if (!(usuario instanceof Paciente)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
            }

            List<Receta> recetas = expedienteService.obtenerTodasLasRecetasPorPaciente(usuario.getId());

            if (recetas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay historial de recetas.");
            }

            return ResponseEntity.ok(recetas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/receta/{recetaId}")
    public ResponseEntity<?> obtenerDetalleReceta(@PathVariable Long recetaId) {
        try {
            // En un sistema real, verificaríamos que esta receta pertenezca al usuario
            // autenticado.

            Receta receta = expedienteService.obtenerRecetaPorId(recetaId);
            return ResponseEntity.ok(receta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receta no encontrada.");
        }
    }
}

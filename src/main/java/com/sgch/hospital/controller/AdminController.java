package com.sgch.hospital.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sgch.hospital.model.DTO.BloqueoHorarioRequest;
import com.sgch.hospital.model.DTO.RegistroRequest;
import com.sgch.hospital.model.entity.BloqueoHorario;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.DoctorHorarioUpdate;
import com.sgch.hospital.service.BloqueoHorarioService;
import com.sgch.hospital.service.CitaService;
import com.sgch.hospital.service.DoctorService;
import com.sgch.hospital.service.PythonAnalyticsService;
import com.sgch.hospital.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public class AdminController {

    private final UsuarioService usuarioService;
    private final CitaService citaService;
    private final PythonAnalyticsService analyticsService;
    private final DoctorService doctorService;
    private final BloqueoHorarioService bloqueoHorarioService;
    @PutMapping("/usuario/{userId}/estado")
    public ResponseEntity<?> actualizarEstadoUsuario(
            @PathVariable Long userId,
            @RequestParam boolean activo) {
        try {
            usuarioService.actualizarEstado(userId, activo);
            return ResponseEntity
                    .ok("Estado del usuario " + userId + " actualizado a: " + (activo ? "ACTIVO" : "INACTIVO"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/personal/registrar")
    public ResponseEntity<?> registrarPersonal(@RequestBody RegistroRequest request) {
        try {

            usuarioService.convertirYRegistrar(request);
            return ResponseEntity.ok("Personal registrado y correo de verificación enviado.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar personal: " + e.getMessage());
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<?> getEstadisticas(
            @RequestParam(defaultValue = "cancelaciones") String metric // Nuevo parámetro
    ) {
        try {
            var todasLasCitas = citaService.obtenerTodasLasCitas();

            if (todasLasCitas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay datos para analizar.");
            }

            String outputFileName = "reporte_" + metric + "_" + System.currentTimeMillis() + ".png";

            // 1. Delegar al servicio de Python, pasando el nombre de la métrica
            String rutaImagen = analyticsService.generarGrafico(
                    todasLasCitas,
                    outputFileName,
                    metric // Pasamos la métrica
            );

            return ResponseEntity.ok("Gráfico " + metric + " generado con éxito. Ruta: " + rutaImagen);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en el análisis de datos: " + e.getMessage());
        }
    }

    /*
     * @GetMapping("/estadisticas/cancelaciones")
     * public ResponseEntity<?> getEstadisticasCancelaciones() {
     * try {
     * var todasLasCitas = citaService.obtenerTodasLasCitas();
     * 
     * if (todasLasCitas.isEmpty()) {
     * return ResponseEntity.status(HttpStatus.NO_CONTENT).
     * body("No hay datos de citas para analizar.");
     * }
     * 
     * String outputFileName = "reporte_cancelaciones_" + System.currentTimeMillis()
     * + ".png";
     * 
     * String rutaImagen = analyticsService.generarGraficoCancelaciones(
     * todasLasCitas,
     * outputFileName);
     * 
     * return ResponseEntity.ok("Gráfico generado con éxito. Ruta: " + rutaImagen);
     * 
     * } catch (Exception e) {
     * return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
     * .body("Error en el análisis de datos: " + e.getMessage());
     * }
     * }
     */

    @PostMapping("/bloqueo-horario")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public ResponseEntity<?> crearBloqueoHorario(@Valid @RequestBody BloqueoHorarioRequest request) {
    try {
        BloqueoHorario bloqueo = bloqueoHorarioService.crearBloqueo(request);
        return ResponseEntity.ok(bloqueo);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error al crear bloqueo: " + e.getMessage());
    }
}


    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @PostMapping("/horario-doctor")
    public ResponseEntity<?> asignarHorarioDoctor(@Valid @RequestBody DoctorHorarioUpdate updateDto) {
        try {
            Doctor doctorActualizado = doctorService.actualizarHorarioDoctor(updateDto);
            return ResponseEntity.ok(doctorActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar el horario: " + e.getMessage());
        }
    }

    @GetMapping("/citas/buscar")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<List<Cita>> buscarCitas(
            // Parámetros opcionales
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<Cita> citas = citaService.buscarCitas(doctorId, fecha);

        if (citas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(citas);
    }

}

package com.sgch.hospital.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sgch.hospital.model.DTO.RegistroRequest;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.DoctorHorarioUpdate;
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
    private final CitaService citaService; // Necesario para obtener todos los datos
    private final PythonAnalyticsService analyticsService;
    private final DoctorService doctorService;

    /**
     * ENDPOINT: Registro de personal (Doctor o Admin)
     */
    @PostMapping("/personal/registrar")
    public ResponseEntity<?> registrarPersonal(@RequestBody RegistroRequest request) {
        try {
            // El servicio maneja la lógica de crear el tipo de usuario correcto
            // (Doctor/Admin)
            usuarioService.convertirYRegistrar(request);
            return ResponseEntity.ok("Personal registrado y correo de verificación enviado.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar personal: " + e.getMessage());
        }
    }

    @GetMapping("/estadisticas/cancelaciones")
    public ResponseEntity<?> getEstadisticasCancelaciones() {
        try {
            // 1. Obtener todos los datos de citas (o un subconjunto relevante)
            var todasLasCitas = citaService.obtenerTodasLasCitas();

            if (todasLasCitas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay datos de citas para analizar.");
            }

            String outputFileName = "reporte_cancelaciones_" + System.currentTimeMillis() + ".png";

            // 2. Delegar la manipulación y el análisis a Python (PARADIGMA
            // VECTORIAL/FUNCIONAL)
            String rutaImagen = analyticsService.generarGraficoCancelaciones(
                    todasLasCitas,
                    outputFileName);

            // 3. Devolver la ruta donde Angular puede acceder a la imagen generada
            return ResponseEntity.ok("Gráfico generado con éxito. Ruta: " + rutaImagen);

            // NOTA: En un sistema real, aquí se devolvería el archivo binario de la imagen.

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en el análisis de datos: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @PostMapping("/horario-doctor")
    public ResponseEntity<?> asignarHorarioDoctor(@Valid @RequestBody DoctorHorarioUpdate updateDto) {
        try {
            Doctor doctorActualizado = doctorService.actualizarHorarioDoctor(updateDto);
            return ResponseEntity.ok(doctorActualizado);
        } catch (IllegalArgumentException e) {
            // Maneja el error de validación de hora de inicio vs hora de fin
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Maneja error de Doctor no encontrado
            return ResponseEntity.internalServerError().body("Error al actualizar el horario: " + e.getMessage());
        }
    }

    @GetMapping("/citas/buscar")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')") // Solo un Admin puede ver todas las citas
    public ResponseEntity<List<Cita>> buscarCitas(
            // Parámetros opcionales
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<Cita> citas = citaService.buscarCitas(doctorId, fecha);

        if (citas.isEmpty()) {
            return ResponseEntity.noContent().build(); // Código 204 No Content si no se encuentran
        }

        return ResponseEntity.ok(citas);
    }
}

package com.sgch.hospital.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sgch.hospital.model.entity.Especialidad;
import com.sgch.hospital.repository.EspecialidadRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadRepository especialidadRepository;

    @GetMapping
    public ResponseEntity<List<Especialidad>> obtenerTodasLasEspecialidades() {
        try {
            List<Especialidad> especialidades = especialidadRepository.findAll();
            return ResponseEntity.ok(especialidades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerEspecialidadPorId(@PathVariable Long id) {
        try {
            Especialidad especialidad = especialidadRepository.findById(id)
                    .orElseThrow(() -> new Exception("Especialidad no encontrada"));
            return ResponseEntity.ok(especialidad);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Especialidad no encontrada: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<?> crearEspecialidad(@Valid @RequestBody Especialidad especialidad) {
        try {
            if (especialidadRepository.findByNombre(especialidad.getNombre()) != null) {
                return ResponseEntity.badRequest()
                        .body("Ya existe una especialidad con ese nombre");
            }
            Especialidad nuevaEspecialidad = especialidadRepository.save(especialidad);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaEspecialidad);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al crear especialidad: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<?> actualizarEspecialidad(
            @PathVariable Long id,
            @Valid @RequestBody Especialidad especialidadActualizada) {
        try {
            Especialidad especialidad = especialidadRepository.findById(id)
                    .orElseThrow(() -> new Exception("Especialidad no encontrada"));

            Especialidad existente = especialidadRepository.findByNombre(especialidadActualizada.getNombre());
            if (existente != null && !existente.getId().equals(id)) {
                return ResponseEntity.badRequest()
                        .body("Ya existe otra especialidad con ese nombre");
            }

            especialidad.setNombre(especialidadActualizada.getNombre());
            especialidad.setDescripcion(especialidadActualizada.getDescripcion());

            Especialidad guardada = especialidadRepository.save(especialidad);
            return ResponseEntity.ok(guardada);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al actualizar especialidad: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<?> eliminarEspecialidad(@PathVariable Long id) {
        try {
            Especialidad especialidad = especialidadRepository.findById(id)
                    .orElseThrow(() -> new Exception("Especialidad no encontrada"));
            especialidadRepository.delete(especialidad);
            return ResponseEntity.ok("Especialidad eliminada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al eliminar especialidad: " + e.getMessage());
        }
    }
}
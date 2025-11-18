package com.sgch.hospital.model.DTO;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class BloqueoHorarioRequest {
    private Long id;
    @NotNull private Long doctorId;
    @NotNull private LocalDateTime inicioBloqueo;
    @NotNull private LocalDateTime finBloqueo;
    @NotBlank private String motivo;
    private boolean esRecurrente; // Añadir este campo
}

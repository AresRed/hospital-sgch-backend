package com.sgch.hospital.model.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CitaReprogramarRequest {

    @NotNull private Long nuevoDoctorId;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate nuevaFecha;
    @NotNull private LocalTime nuevaHora;
    @NotBlank private String nuevoMotivo; // Puedes añadir validaciones con @Pattern si es necesario
}

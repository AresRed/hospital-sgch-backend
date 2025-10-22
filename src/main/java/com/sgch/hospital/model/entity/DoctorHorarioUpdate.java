package com.sgch.hospital.model.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DoctorHorarioUpdate {

    @NotNull(message = "El ID del doctor es obligatorio.")
    private Long doctorId;

    @Pattern(regexp = "^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$", message = "El formato de la hora de inicio debe ser HH:mm (24h).")
    private String horarioAtencionInicio; // Ej: "08:00"

    @Pattern(regexp = "^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$", message = "El formato de la hora de fin debe ser HH:mm (24h).")
    private String horarioAtencionFin;    // Ej: "21:00"

    @NotNull(message = "La duración de la cita es obligatoria.")
    @Min(value = 5, message = "La duración mínima de la cita debe ser 5 minutos.")
    @Max(value = 120, message = "La duración máxima de la cita es 120 minutos.")
    private Integer duracionCitaMinutos; // Ej: 60
}

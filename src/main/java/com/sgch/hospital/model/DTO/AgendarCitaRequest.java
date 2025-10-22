package com.sgch.hospital.model.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgendarCitaRequest {

    @NotNull(message = "El ID del doctor es obligatorio")
    private Long doctorId;
    
    @NotNull(message = "La fecha de inicio de búsqueda es obligatoria")
    private LocalDate fechaCandidata;
    
    @NotNull(message = "La hora seleccionada es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaSeleccionada;
    
    @NotBlank(message = "El motivo de la cita es obligatorio")
    private String motivo;
}

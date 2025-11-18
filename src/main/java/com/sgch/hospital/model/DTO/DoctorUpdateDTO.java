package com.sgch.hospital.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DoctorUpdateDTO {

    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener 8 dígitos")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email incorrecto")
    private String email;

    private String telefono;
    
    private String direccion;

    private String password;

    @NotNull(message = "La especialidad es obligatoria")
    private Long especialidadId;

    @NotBlank(message = "Horario de inicio es obligatorio")
    private String horarioAtencionInicio;

    @NotBlank(message = "Horario de fin es obligatorio")
    private String horarioAtencionFin;

    @NotNull(message = "Duración de cita es obligatoria")
    private Integer duracionCitaMinutos;
}
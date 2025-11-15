package com.sgch.hospital.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistroRequest {

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email incorrecto")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String telefono;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    // Campo opcional, pero crucial para que el administrador pueda registrar otros roles
    private Long especialidadId;  // Puede ser "PACIENTE", "DOCTOR", o "ADMINISTRADOR"
    private String especialidad; // Solo para Doctor
    private String seguroMedico; // Solo para Paciente
}

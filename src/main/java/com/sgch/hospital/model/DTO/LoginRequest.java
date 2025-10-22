package com.sgch.hospital.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Formato de email incorrecto")
    private String email;
    
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}

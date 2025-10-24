package com.sgch.hospital.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PacienteUpdateDTO {


    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre; // Requerido para la actualización
    
    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "Formato de email inválido.")
    private String email; 
    
    private String telefono;
    
    private String direccion;
}

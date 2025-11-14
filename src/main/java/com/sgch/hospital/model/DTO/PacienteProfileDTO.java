package com.sgch.hospital.model.DTO;

import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.model.entity.Usuario;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PacienteProfileDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private String rol;
    private String seguroMedico; // Añadir el campo específico de Paciente

    public PacienteProfileDTO(Paciente paciente) {
        this.id = paciente.getId();
        this.nombre = paciente.getNombre();
        this.apellido = paciente.getApellido();
        this.email = paciente.getEmail();
        this.telefono = paciente.getTelefono();
        this.direccion = paciente.getDireccion();
        this.rol = paciente.getRol().name();
        this.seguroMedico = paciente.getSeguroMedico(); // Asignar el campo específico
    }
}

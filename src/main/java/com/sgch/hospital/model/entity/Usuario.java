package com.sgch.hospital.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) 
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dni; // Usado para roles y búsqueda
    
    @Column(unique = true, nullable = false)
    private String email; // Usado para verificación de Gmail y login
    
    @Column(nullable = false)
    private String password; // Hash de la contraseña
    
    private String nombre;
    private String apellido;
    private String telefono;
    
    private String direccion;
    // Spring Security usará este campo para la gestión de roles
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    // Enum para los roles
    public enum Rol {
        PACIENTE, DOCTOR, ADMINISTRADOR
    }


}

package com.sgch.hospital.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class BloqueoHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Muchos a Uno con el Doctor que tendrá el bloqueo
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDateTime inicioBloqueo;

    @Column(nullable = false)
    private LocalDateTime finBloqueo;
    
    private String motivo; // Ej: "Almuerzo", "Congreso", "Día Libre"
    
    // Un campo que indica si el bloqueo es recurrente (ej., cada martes)
    private boolean esRecurrente = false;
}

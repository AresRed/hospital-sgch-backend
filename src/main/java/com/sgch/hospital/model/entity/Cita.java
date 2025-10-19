package com.sgch.hospital.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relación con el Paciente
    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
    
    // Relación con el Doctor
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    
    @Column(nullable = false)
    private LocalDateTime fechaHora; // La fecha y hora exacta de la cita
    
    @Column(nullable = false)
    private String motivo;
    
    @Enumerated(EnumType.STRING)
    private EstadoCita estado = EstadoCita.PENDIENTE; // Por defecto: PENDIENTE
    
    // Enum para el estado de la cita
    public enum EstadoCita {
        PENDIENTE, CONFIRMADA, CANCELADA, REALIZADA
    }

}

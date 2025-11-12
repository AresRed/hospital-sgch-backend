package com.sgch.hospital.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "doctor_id")
public class Doctor extends Usuario {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;
    // Opcional: Horario de atención (se puede modelar como otra entidad más
    // compleja)
    private String horarioAtencion;

    @Column(nullable = false) // Ahora son obligatorios para la lógica de citas
    private String horarioAtencionInicio = "09:00"; // Default para evitar null en DB

    @Column(nullable = false)
    private String horarioAtencionFin = "17:00"; // Default

    @Column(nullable = false)
    private Integer duracionCitaMinutos = 30;

}

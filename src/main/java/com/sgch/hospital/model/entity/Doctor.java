package com.sgch.hospital.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "doctor_id")
public class Doctor extends Usuario{

    @Column(nullable = false)
    private String especialidad;
    
    // Opcional: Horario de atención (se puede modelar como otra entidad más compleja)
    private String horarioAtencion; 
}

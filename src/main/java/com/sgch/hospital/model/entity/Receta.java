package com.sgch.hospital.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Receta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Uno a Uno con la Nota Médica (la razón de la receta)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_id", unique = true, nullable = false)
    private NotaMedica notaMedica;
    
    private LocalDateTime fechaEmision = LocalDateTime.now();

    // Detalle de la prescripción: la lista de medicamentos y dosis
    @JsonIgnore
    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReceta> detalles;
}

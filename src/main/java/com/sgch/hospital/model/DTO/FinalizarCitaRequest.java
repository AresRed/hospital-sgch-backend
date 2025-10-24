package com.sgch.hospital.model.DTO;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinalizarCitaRequest {

    @NotNull
    private boolean asistio; 

    // Campos del Expediente
    private String diagnostico;
    private String recomendaciones;

    // Campos de la Receta (Lista de medicamentos)
    private List<MedicamentoRecetaDTO> medicamentos;
}

package com.sgch.hospital.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicamentoRecetaDTO {

    @NotBlank
    private String nombre;
    @NotBlank
    private String dosis;
    private String instrucciones;
}

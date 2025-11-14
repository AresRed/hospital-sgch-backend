package com.sgch.hospital.model.DTO;

import com.sgch.hospital.model.entity.DetalleReceta;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DetalleRecetaDTO {
    private Long id;
    private String nombreMedicamento;
    private String dosis;
    private String instrucciones;

    public DetalleRecetaDTO(DetalleReceta detalleReceta) {
        this.id = detalleReceta.getId();
        this.nombreMedicamento = detalleReceta.getNombreMedicamento();
        this.dosis = detalleReceta.getDosis();
        this.instrucciones = detalleReceta.getInstrucciones();
    }
}

package com.sgch.hospital.model.DTO;

import com.sgch.hospital.model.entity.Receta;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class RecetaResponseDTO {
    private Long id;
    private Long notaMedicaId;
    private LocalDateTime fechaEmision;
    private List<DetalleRecetaDTO> detalles;

    public RecetaResponseDTO(Receta receta) {
        this.id = receta.getId();
        this.notaMedicaId = receta.getNotaMedica().getId();
        this.fechaEmision = receta.getFechaEmision();
        this.detalles = receta.getDetalles().stream()
                .map(DetalleRecetaDTO::new)
                .collect(Collectors.toList());
    }
}

package com.sgch.hospital.model.DTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DoctorListDTO {

    private final Long id;
    private final String nombreCompleto;
    private final String especialidadNombre;
}

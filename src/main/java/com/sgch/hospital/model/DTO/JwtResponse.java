package com.sgch.hospital.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private Long id;
    private String email;
    private String rol;
    private String tipo = "Bearer";
}

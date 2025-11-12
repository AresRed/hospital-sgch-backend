package com.sgch.hospital.service;

import org.springframework.stereotype.Service;

import com.sgch.hospital.model.DTO.BloqueoHorarioRequest;
import com.sgch.hospital.model.entity.BloqueoHorario;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.repository.BloqueoHorarioRepository;
import com.sgch.hospital.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BloqueoHorarioService {

    private final BloqueoHorarioRepository bloqueoHorarioRepository;
    private final DoctorRepository doctorRepository;

    public BloqueoHorario crearBloqueo(BloqueoHorarioRequest request) throws Exception {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new Exception("Doctor no encontrado."));

        BloqueoHorario bloqueo = new BloqueoHorario();
        bloqueo.setDoctor(doctor);
        bloqueo.setInicioBloqueo(request.getInicioBloqueo());
        bloqueo.setFinBloqueo(request.getFinBloqueo());
        bloqueo.setMotivo(request.getMotivo());
        bloqueo.setEsRecurrente(request.isEsRecurrente()); // ¡Añadir esta línea!
        
        return bloqueoHorarioRepository.save(bloqueo);
    }
}

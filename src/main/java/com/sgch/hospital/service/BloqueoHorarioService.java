package com.sgch.hospital.service;

import java.util.List;

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

    public List<BloqueoHorario> obtenerBloqueosPorDoctor(Long doctorId) {
    System.out.println("🔍 Buscando bloqueos para doctor ID: " + doctorId);
    
    try {
        // Verificar si el doctor existe
        if (!doctorRepository.existsById(doctorId)) {
            System.err.println("❌ Doctor no encontrado con ID: " + doctorId);
            throw new RuntimeException("Doctor no encontrado con ID: " + doctorId);
        }
        
        // Usar el método del repositorio directamente
        List<BloqueoHorario> bloqueos = bloqueoHorarioRepository.findByDoctorId(doctorId);
        System.out.println("✅ Bloqueos encontrados: " + bloqueos.size());
        
        return bloqueos;
        
    } catch (Exception e) {
        System.err.println("❌ Error en obtenerBloqueosPorDoctor: " + e.getMessage());
        throw e;
    }
}

    public void eliminarBloqueo(Long bloqueoId) throws Exception {
        BloqueoHorario bloqueo = bloqueoHorarioRepository.findById(bloqueoId)
            .orElseThrow(() -> new Exception("Bloqueo no encontrado."));
        bloqueoHorarioRepository.delete(bloqueo);
    }
}
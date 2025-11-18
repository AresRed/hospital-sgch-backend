package com.sgch.hospital.service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sgch.hospital.model.DTO.DoctorListDTO;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.DoctorHorarioUpdate;
import com.sgch.hospital.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public Doctor findDoctorById(Long id) throws Exception {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new Exception("Doctor no encontrado con ID: " + id));
    }

    public Doctor actualizarHorarioDoctor(DoctorHorarioUpdate updateDto) throws Exception {

        Doctor doctor = findDoctorById(updateDto.getDoctorId());

        LocalTime inicio = LocalTime.parse(updateDto.getHorarioAtencionInicio());
        LocalTime fin = LocalTime.parse(updateDto.getHorarioAtencionFin());

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La hora de inicio (" + updateDto.getHorarioAtencionInicio() +
                    ") no puede ser posterior a la hora de fin (" + updateDto.getHorarioAtencionFin() + ").");
        }

        doctor.setHorarioAtencionInicio(updateDto.getHorarioAtencionInicio());
        doctor.setHorarioAtencionFin(updateDto.getHorarioAtencionFin());
        doctor.setDuracionCitaMinutos(updateDto.getDuracionCitaMinutos());

        return doctorRepository.save(doctor);
    }

    public List<DoctorListDTO> listarDoctoresDisponibles() {
        List<Doctor> doctores = doctorRepository.findAll(); // Obtiene todas las entidades

        // Mapeo funcional (Java Streams): Transforma Entidad -> DTO
        return doctores.stream()
                .map(d -> new DoctorListDTO(
                        d.getId(),
                        d.getNombre() + " " + d.getApellido(), // Nombre completo
                        d.getEspecialidad().getNombre() // Nombre de la especialidad (corregido)
                ))
                .collect(Collectors.toList());
    }
}
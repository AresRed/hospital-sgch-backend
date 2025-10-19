package com.sgch.hospital.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgch.hospital.model.entity.Administrador;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Doctor;

public interface AdministradorRepository extends JpaRepository<Administrador, Long>{



    // Método clave que usará el servicio Prolog para obtener todas las citas de un doctor
    List<Cita> findByDoctorAndFechaHoraBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);
    
    // Método para verificar si ya existe una cita en un momento exacto para un doctor
    boolean existsByDoctorAndFechaHora(Doctor doctor, LocalDateTime fechaHora);

    // Método para que el paciente vea sus citas
    List<Cita> findByPacienteId(Long pacienteId);
}

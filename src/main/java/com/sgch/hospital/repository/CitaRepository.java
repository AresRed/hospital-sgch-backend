package com.sgch.hospital.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Doctor;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByDoctorAndFechaHoraBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);

    boolean existsByDoctorAndFechaHora(Doctor doctor, LocalDateTime fechaHora);

    List<Cita> findByPacienteId(Long pacienteId);

    List<Cita> findByDoctorId(Long doctorId);

    List<Cita> findByFechaHoraBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Cita> findByDoctorIdAndFechaHoraBetween(Long doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}

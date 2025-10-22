package com.sgch.hospital.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Doctor;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Recupera todas las citas de un Doctor dentro de un rango de tiempo.
     * Crucial para que el servicio pueda obtener los "hechos" para Prolog.
     */
    List<Cita> findByDoctorAndFechaHoraBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);

    /**
     * Verifica si ya existe una cita en un momento exacto para un doctor.
     */
    boolean existsByDoctorAndFechaHora(Doctor doctor, LocalDateTime fechaHora);

    /**
     * Recupera todas las citas de un Paciente por su ID.
     */
    List<Cita> findByPacienteId(Long pacienteId);

    List<Cita> findByDoctorId(Long doctorId);

    // 2. Buscar por Rango de Fecha (Día)
    List<Cita> findByFechaHoraBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    // 3. Buscar por Doctor Y Rango de Fecha (Día)
    List<Cita> findByDoctorIdAndFechaHoraBetween(Long doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}

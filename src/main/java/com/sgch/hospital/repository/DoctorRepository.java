package com.sgch.hospital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgch.hospital.model.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

     List<Doctor> findByEspecialidad_Nombre(String nombreEspecialidad);

    Optional<Doctor> findByDni(String dni);
}

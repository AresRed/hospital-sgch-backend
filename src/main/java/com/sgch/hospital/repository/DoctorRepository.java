package com.sgch.hospital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgch.hospital.model.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long>{

    // Método para buscar doctores por especialidad (útil en la lógica de citas)
    List<Doctor> findByEspecialidad(String especialidad);
    
    // Método para buscar un doctor por su DNI (útil para el rol de administrador)
    Optional<Doctor> findByDni(String dni);
}

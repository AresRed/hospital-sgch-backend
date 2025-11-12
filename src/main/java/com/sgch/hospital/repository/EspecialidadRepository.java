package com.sgch.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgch.hospital.model.entity.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long>{

    Especialidad findByNombre(String nombre);
}

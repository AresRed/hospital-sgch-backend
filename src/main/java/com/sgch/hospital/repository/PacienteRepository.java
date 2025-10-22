package com.sgch.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sgch.hospital.model.entity.Paciente;


public interface PacienteRepository extends JpaRepository<Paciente, Long> {

}

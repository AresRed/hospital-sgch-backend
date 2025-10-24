package com.sgch.hospital.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgch.hospital.model.entity.Expediente;
import com.sgch.hospital.model.entity.Paciente;

public interface ExpedienteRepository extends JpaRepository<Expediente, Long>{


    Optional<Expediente> findByPaciente(Paciente paciente);
}

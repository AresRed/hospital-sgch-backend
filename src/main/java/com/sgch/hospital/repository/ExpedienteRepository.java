package com.sgch.hospital.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sgch.hospital.model.entity.Expediente;
import com.sgch.hospital.model.entity.Paciente;

public interface ExpedienteRepository extends JpaRepository<Expediente, Long>{


    Optional<Expediente> findByPaciente(Paciente paciente);

    @Query("SELECT e FROM Expediente e JOIN FETCH e.notas WHERE e.paciente = :paciente")
    Optional<Expediente> findByPacienteWithNotas(@Param("paciente") Paciente paciente);
}

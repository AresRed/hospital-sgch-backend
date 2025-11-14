package com.sgch.hospital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sgch.hospital.model.entity.Receta;

public interface RecetaRepository extends JpaRepository<Receta, Long>{

    @Query("SELECT r FROM Receta r JOIN FETCH r.detalles JOIN FETCH r.notaMedica nm JOIN FETCH nm.expediente WHERE r.id = :id")
    Optional<Receta> findByIdWithDetallesAndNotaMedicaAndExpediente(@Param("id") Long id);
}

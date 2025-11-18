package com.sgch.hospital.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sgch.hospital.model.entity.BloqueoHorario;
import com.sgch.hospital.model.entity.Doctor;

@Repository
public interface BloqueoHorarioRepository extends JpaRepository<BloqueoHorario, Long>{
    List<BloqueoHorario> findByDoctorAndFinBloqueoAfterAndInicioBloqueoBefore(
        Doctor doctor, 
        LocalDateTime inicioRango, 
        LocalDateTime finRango
    );

    List<BloqueoHorario> findByDoctorAndEsRecurrenteTrue(Doctor doctor);

    List<BloqueoHorario> findByDoctorAndEsRecurrenteFalseAndFinBloqueoAfterAndInicioBloqueoBefore(
        Doctor doctor, 
        LocalDateTime inicioRango, 
        LocalDateTime finRango
    );

    List<BloqueoHorario> findByDoctorId(Long doctorId);
}

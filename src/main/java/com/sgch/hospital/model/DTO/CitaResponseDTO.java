package com.sgch.hospital.model.DTO;

import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.Paciente;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CitaResponseDTO {
    private Long id;
    private Long pacienteId;
    private String pacienteNombreCompleto;
    private DoctorDTO doctor; // Usar un DTO para el doctor
    private LocalDateTime fechaHora;
    private String motivo;
    private Cita.EstadoCita estado;

    public CitaResponseDTO(Cita cita) {
        this.id = cita.getId();
        this.pacienteId = cita.getPaciente().getId();
        this.pacienteNombreCompleto = cita.getPaciente().getNombre() + " " + cita.getPaciente().getApellido();
        this.doctor = new DoctorDTO(cita.getDoctor());
        this.fechaHora = cita.getFechaHora();
        this.motivo = cita.getMotivo();
        this.estado = cita.getEstado();
    }

    @Data
    @NoArgsConstructor
    public static class DoctorDTO {
        private Long id;
        private String nombreCompleto;
        private String especialidad;

        public DoctorDTO(Doctor doctor) {
            this.id = doctor.getId();
            this.nombreCompleto = doctor.getNombre() + " " + doctor.getApellido();
            this.especialidad = doctor.getEspecialidad().getNombre();
        }
    }
}

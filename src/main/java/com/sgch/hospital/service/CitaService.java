package com.sgch.hospital.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sgch.hospital.external.prolog.PrologService;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Cita.EstadoCita;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.repository.CitaRepository;
import com.sgch.hospital.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final DoctorRepository doctorRepository;
    private final PrologService prologService; 
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public Cita agendarCita(Long doctorId, Paciente paciente, LocalDate fecha, String motivo, LocalTime hora)
            throws Exception {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new Exception("Doctor no encontrado."));


        LocalDateTime fechaHoraSeleccionada = fecha.atTime(hora);
        String horaStr = hora.format(timeFormatter); 

        List<String> horariosDisponibles = obtenerHorariosDisponibles(doctorId, fecha);



        if (!horariosDisponibles.contains(horaStr)) {

            throw new IllegalArgumentException(
                    "La hora seleccionada (" + horaStr
                            + ") no está disponible, está fuera de horario, o se solapa con otra cita.");
        }
        Cita nuevaCita = new Cita();
        nuevaCita.setDoctor(doctor);
        nuevaCita.setPaciente(paciente);
        nuevaCita.setFechaHora(fechaHoraSeleccionada); 
        nuevaCita.setMotivo(motivo);
        nuevaCita.setEstado(Cita.EstadoCita.CONFIRMADA);


        return citaRepository.save(nuevaCita);
    }

    public List<Cita> obtenerCitasPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    public List<Cita> obtenerCitasPorDoctor(Long doctorId) throws Exception {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new Exception("Doctor no encontrado para agenda."));

        return citaRepository.findAll()
                .stream()
                .filter(c -> c.getDoctor().getId().equals(doctorId) && c.getEstado() == Cita.EstadoCita.PENDIENTE)
                .toList();
    }

    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAll(); 
    }

    public List<String> obtenerHorariosDisponibles(Long doctorId, LocalDate fecha) throws Exception {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new Exception("Doctor no encontrado."));

        List<Cita> citasOcupadas = citaRepository.findByDoctorAndFechaHoraBetween(
                doctor,
                fecha.atStartOfDay(),
                fecha.atTime(LocalTime.MAX));

        prologService.limpiarHechosTemporales();
        prologService.assertFacts(doctor, citasOcupadas); 

        return prologService.obtenerTodasHorasDisponibles(doctor, fecha);
    }

    public Cita cancelarCita(Long citaId) throws Exception {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new Exception("Cita no encontrada."));

        if (cita.getEstado() == EstadoCita.REALIZADA) {
            throw new IllegalArgumentException("No se puede cancelar una cita ya realizada.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public Cita postergarCita(Long citaId, Long nuevoDoctorId, LocalDate nuevaFecha, String nuevoMotivo,LocalTime nuevaHora)
            throws Exception {

        Cita citaAntigua = cancelarCita(citaId);

        Paciente paciente = citaAntigua.getPaciente();

        return agendarCita(nuevoDoctorId, paciente, nuevaFecha, nuevoMotivo,nuevaHora);
    }

    public List<Cita> buscarCitas(Long doctorId, LocalDate fecha) {

        LocalDateTime startOfDay = (fecha != null) ? fecha.atStartOfDay() : null;
        LocalDateTime endOfDay = (fecha != null) ? fecha.atTime(LocalTime.MAX) : null;

        if (doctorId != null && fecha != null) {
            return citaRepository.findByDoctorIdAndFechaHoraBetween(doctorId, startOfDay, endOfDay);

        } else if (doctorId != null) {
            return citaRepository.findByDoctorId(doctorId);

        } else if (fecha != null) {
            return citaRepository.findByFechaHoraBetween(startOfDay, endOfDay);

        } else {
            return citaRepository.findAll();
        }
    }
}

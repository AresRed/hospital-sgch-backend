package com.sgch.hospital.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sgch.hospital.model.DTO.FinalizarCitaRequest;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.DetalleReceta;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.Expediente;
import com.sgch.hospital.model.entity.NotaMedica;
import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.model.entity.Receta;
import com.sgch.hospital.repository.CitaRepository;
import com.sgch.hospital.repository.ExpedienteRepository;
import com.sgch.hospital.repository.NotaMedicaRepository;
import com.sgch.hospital.repository.PacienteRepository;
import com.sgch.hospital.repository.RecetaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpedienteService {

    private final ExpedienteRepository expedienteRepository;
    private final NotaMedicaRepository notaMedicaRepository;
    private final CitaRepository citaRepository;
    private final RecetaRepository recetaRepository;
    private final PacienteRepository pacienteRepository;

  
    @Transactional
    public NotaMedica finalizarYGuardarExpediente(Long citaId, Doctor doctor, FinalizarCitaRequest request) throws Exception {
        
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new Exception("Cita no encontrada."));
        
        Paciente paciente = cita.getPaciente();

        // 1. CONFIRMAR ASISTENCIA (Actualización de la Cita)
        cita.setEstado(request.isAsistio() ? Cita.EstadoCita.REALIZADA : Cita.EstadoCita.AUSENTE);
        citaRepository.save(cita);

        if (!request.isAsistio()) {
            // Si no asistió, solo actualizamos la cita y terminamos el proceso.
            return null;
        }

        // 2. OBTENER O CREAR EXPEDIENTE (Lógica condicional)
        Expediente expediente = expedienteRepository.findByPaciente(paciente)
                .orElseGet(() -> {
                    // Si el paciente es nuevo, creamos un nuevo expediente
                    Expediente nuevoExpediente = new Expediente();
                    nuevoExpediente.setPaciente(paciente);
                    return expedienteRepository.save(nuevoExpediente);
                });
        
        // 3. CREAR NOTA MÉDICA (Registro de la Visita)
        NotaMedica nota = new NotaMedica();
        nota.setExpediente(expediente);
        nota.setCita(cita);
        nota.setDoctor(doctor);
        nota.setFechaHora(LocalDateTime.now());
        nota.setMotivoConsulta(cita.getMotivo()); // Usamos el motivo original de la cita
        nota.setDiagnostico(request.getDiagnostico());
        nota.setRecomendaciones(request.getRecomendaciones());
        
        NotaMedica savedNota = notaMedicaRepository.save(nota);

        // 4. CREAR RECETA MÉDICA (Si hay medicamentos en el request)
        if (request.getMedicamentos() != null && !request.getMedicamentos().isEmpty()) {
            Receta receta = new Receta();
            receta.setNotaMedica(savedNota);
            
            // Mapeo de DTO a DetalleReceta
            List<DetalleReceta> detalles = request.getMedicamentos().stream()
                .map(dto -> {
                    DetalleReceta detalle = new DetalleReceta();
                    detalle.setReceta(receta);
                    detalle.setNombreMedicamento(dto.getNombre());
                    detalle.setDosis(dto.getDosis());
                    detalle.setInstrucciones(dto.getInstrucciones());
                    return detalle;
                }).collect(Collectors.toList());
            
            receta.setDetalles(detalles);
            recetaRepository.save(receta);
        }
        
        return savedNota;
    }
    
    public Expediente obtenerExpedientePorPacienteId(Long pacienteId) throws Exception {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new Exception("Paciente no encontrado."));
        
        return expedienteRepository.findByPaciente(paciente)
                .orElseThrow(() -> new Exception("Expediente no iniciado para este paciente."));
    }
    public Receta obtenerUltimaReceta(Long pacienteId) throws Exception {
         Paciente paciente = pacienteRepository.findById(pacienteId)
                 .orElseThrow(() -> new Exception("Paciente no encontrado."));
         
         Expediente expediente = expedienteRepository.findByPaciente(paciente)
                 .orElseThrow(() -> new Exception("Expediente no encontrado para este paciente."));

         return expediente.getNotas().stream()
                 .sorted((n1, n2) -> n2.getFechaHora().compareTo(n1.getFechaHora())) // Ordenar por fecha descendente
                 .map(NotaMedica::getReceta)
                 .filter(receta -> receta != null)
                 .findFirst()
                 .orElseThrow(() -> new Exception("No hay recetas en el historial."));
    }

    public Receta obtenerRecetaPorId(Long recetaId) throws Exception {
        return recetaRepository.findById(recetaId)
                .orElseThrow(() -> new Exception("Receta no encontrada."));
    }

    public List<Receta> obtenerTodasLasRecetasPorPaciente(Long pacienteId) throws Exception {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new Exception("Paciente no encontrado."));
        
        Expediente expediente = expedienteRepository.findByPaciente(paciente)
                .orElseThrow(() -> new Exception("Expediente no encontrado para este paciente."));

        return expediente.getNotas().stream()
                .map(NotaMedica::getReceta)
                .filter(receta -> receta != null)
                .sorted((r1, r2) -> r2.getFechaEmision().compareTo(r1.getFechaEmision())) // Ordenar por fecha descendente
                .collect(Collectors.toList());
    }

    
}

package com.sgch.hospital.external.prolog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jpl7.Query;
import org.jpl7.Term;
import org.springframework.stereotype.Service;

import com.sgch.hospital.model.entity.BloqueoHorario;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.repository.BloqueoHorarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrologService {

    // Constantes para los predicados dinámicos (hechos inyectados por Java)
    private static final String PREDICADO_CITA = "cita_reservada_temp"; 
    private static final String PREDICADO_DOCTOR = "info_doctor"; 
    private static final String PREDICADO_BLOQUEO = "bloqueo_horario_temp"; // Para los almuerzos/descansos

    // Formateadores de tiempo
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // REPOSITORIO INYECTADO (OO)
    private final BloqueoHorarioRepository bloqueoHorarioRepository; // <-- Correcto

    static {
        // Cargar el archivo Prolog al iniciar la aplicación
        String prologFilePath = "src/main/resources/prolog/reglas_citas.pl";
        try {
            Query q1 = new Query("consult('" + prologFilePath + "')");
            if (!q1.hasSolution()) {
                System.err.println("Error: No se pudo cargar el archivo Prolog: " + prologFilePath);
            } else {
                System.out.println("Archivo Prolog cargado exitosamente: " + prologFilePath);
            }
        } catch (Exception e) {
            System.err.println("Excepción al cargar el archivo Prolog: " + e.getMessage());
        }
    }

    public void limpiarHechosTemporales() {
        new Query("retractall(" + PREDICADO_CITA + "(_,_,_,_))").hasSolution();
        new Query("retractall(" + PREDICADO_DOCTOR + "(_,_,_,_,_))").hasSolution();
        new Query("retractall(" + PREDICADO_BLOQUEO + "(_,_,_,_))").hasSolution(); 
    }

    /**
     * Inyecta los hechos (datos) del Doctor, citas ocupadas y bloqueos en el motor de Prolog.
     * @param doctor La entidad Doctor.
     * @param fechaCandidata La fecha para filtrar los bloqueos.
     * @param citasOcupadas Las citas existentes del día.
     */
    public void assertFacts(Doctor doctor, LocalDate fechaCandidata, List<Cita> citasOcupadas) {

    // ====== 1. Datos del doctor ======
    String nombreEspecialidad = doctor.getEspecialidad().getNombre();
    int duracionMinutos = doctor.getDuracionCitaMinutos();
    String horaInicio = doctor.getHorarioAtencionInicio();
    String horaFin = doctor.getHorarioAtencionFin();

    String queryDoctor = String.format(
            "assertz(%s(%d, '%s', %d, '%s', '%s'))",
            PREDICADO_DOCTOR, doctor.getId(), nombreEspecialidad, duracionMinutos, horaInicio, horaFin);
    new Query(queryDoctor).hasSolution();

    // ====== 2. Citas del día ======
    for (Cita cita : citasOcupadas) {
        String fecha = cita.getFechaHora().format(dateFormatter);
        String horaInicioCita = cita.getFechaHora().format(timeFormatter);

        String queryCita = String.format(
                "assertz(%s(%d, '%s', '%s', %d))",
                PREDICADO_CITA, doctor.getId(), fecha, horaInicioCita, duracionMinutos);
        new Query(queryCita).hasSolution();
    }

    // ====== 3. BLOQUEOS DE HORARIO ======

    LocalDateTime inicioDia = fechaCandidata.atStartOfDay();
    LocalDateTime finDia = fechaCandidata.atTime(LocalTime.MAX);

    // 🔹 Bloqueos por día (no recurrentes)
    List<BloqueoHorario> bloqueosNoRecurrentes =
            bloqueoHorarioRepository.findByDoctorAndEsRecurrenteFalseAndFinBloqueoAfterAndInicioBloqueoBefore(
                    doctor, inicioDia, finDia);

    for (BloqueoHorario b : bloqueosNoRecurrentes) {
        if (b.getInicioBloqueo() != null && b.getFinBloqueo() != null) {
            String fechaBloqueo = b.getInicioBloqueo().format(dateFormatter);
            String horaInicioBloqueo = b.getInicioBloqueo().format(timeFormatter);
            String horaFinBloqueo = b.getFinBloqueo().format(timeFormatter);

            System.out.println("Inyectando bloqueo por día → " + fechaBloqueo);
            String queryBloqueo = String.format(
                    "assertz(%s(%d, 'por_dia', '%s', '%s', '%s'))",
                    PREDICADO_BLOQUEO, doctor.getId(), fechaBloqueo, horaInicioBloqueo, horaFinBloqueo);
            new Query(queryBloqueo).hasSolution();
        }
    }

    // 🔹 Bloqueos recurrentes (todos los días)
    List<BloqueoHorario> bloqueosRecurrentes =
            bloqueoHorarioRepository.findByDoctorAndEsRecurrenteTrue(doctor);

    for (BloqueoHorario b : bloqueosRecurrentes) {
        if (b.getInicioBloqueo() != null && b.getFinBloqueo() != null) {
            String horaInicioBloqueo = b.getInicioBloqueo().format(timeFormatter);
            String horaFinBloqueo = b.getFinBloqueo().format(timeFormatter);

            System.out.println("Inyectando bloqueo recurrente → " + horaInicioBloqueo + " - " + horaFinBloqueo);
            String queryBloqueo = String.format(
                    "assertz(%s(%d, 'recurrente', '-', '%s', '%s'))",
                    PREDICADO_BLOQUEO, doctor.getId(), horaInicioBloqueo, horaFinBloqueo);
            new Query(queryBloqueo).hasSolution();
        }
    }
}
    
    // MÉTODOS DE CONSULTA (encontrarHorarioDisponible y obtenerTodasHorasDisponibles)
    
    public String encontrarHorarioDisponible(Doctor doctor, LocalDateTime fechaInicio) {
        String doctorId = doctor.getId().toString();
        String especialidad = doctor.getEspecialidad().getNombre();
        String fechaBase = fechaInicio.format(dateFormatter);

        String predicado = String.format(
                        "horario_optimo(%s, '%s', '%s', HorarioEncontrado)",
                        doctorId, especialidad, fechaBase);

        Query consulta = new Query(predicado);

        try {
            if (consulta.hasSolution()) {
                Term horario = consulta.oneSolution().get("HorarioEncontrado");
                return horario.toString().replace("'", "");
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error en la consulta de Prolog: " + e.getMessage());
            return null;
        } finally {
            consulta.close();
        }
    }

    public List<String> obtenerTodasHorasDisponibles(Doctor doctor, LocalDate fecha) {
        String doctorId = doctor.getId().toString();
        String especialidad = doctor.getEspecialidad().getNombre(); 
        String fechaBase = fecha.format(dateFormatter);

        String predicado = String.format(
                        "horario_optimo(%s, '%s', '%s', HorarioEncontrado)",
                        doctorId, especialidad, fechaBase);

        Query consulta = new Query(predicado);
        List<String> horasDisponibles = new ArrayList<>();

        try {
            for (Map<String, Term> solution : consulta.allSolutions()) {
                String hora = solution.get("HorarioEncontrado").toString().replace("'", "");
                horasDisponibles.add(hora);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo todas las soluciones de Prolog: " + e.getMessage());
        } finally {
            consulta.close();
        }
        return horasDisponibles;
    }
}

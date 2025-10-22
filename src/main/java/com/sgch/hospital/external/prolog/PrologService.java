package com.sgch.hospital.external.prolog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jpl7.Query;
import org.jpl7.Term;
import org.springframework.stereotype.Service;

import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Doctor;

@Service
public class PrologService {
    private static final String PREDICADO_CITA = "cita_temporal";
    private static final String PREDICADO_DOCTOR = "doctor_temporal";
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static {

        String consultaCarga = "consult('src/main/resources/prolog/reglas_citas.pl')";
        try {
            Query q = new Query(consultaCarga);
            if (q.hasSolution()) {
                System.out.println("Prolog: Archivo de reglas cargado correctamente.");
            } else {
                System.err.println("Prolog: Falló la carga del archivo de reglas. Revisar la sintaxis y la ruta.");
            }
            q.close();
        } catch (Exception e) {
            System.err.println(
                    "ERROR CRÍTICO: No se pudo inicializar JPL o cargar el archivo de reglas. " + e.getMessage());
        }
    }

    public void limpiarHechosTemporales() {
        new Query("retractall(" + PREDICADO_CITA + "(_,_,_,_))").hasSolution();
        new Query("retractall(" + PREDICADO_DOCTOR + "(_,_,_,_,_))").hasSolution();
    }

    public void assertFacts(Doctor doctor, List<Cita> citasOcupadas) {

        int duracionMinutos = doctor.getDuracionCitaMinutos();
        String horaInicio = doctor.getHorarioAtencionInicio();
        String horaFin = doctor.getHorarioAtencionFin();

        String queryDoctor = String.format(
                "assertz(%s(%d, '%s', %d, '%s', '%s'))",
                PREDICADO_DOCTOR, doctor.getId(), doctor.getEspecialidad(), duracionMinutos, horaInicio, horaFin);
        new Query(queryDoctor).hasSolution();

        for (Cita cita : citasOcupadas) {
            String fecha = cita.getFechaHora().format(dateFormatter);
            String horaInicioCita = cita.getFechaHora().format(timeFormatter);

            String queryCita = String.format(
                    "assertz(%s(%d, '%s', '%s', %d))",
                    PREDICADO_CITA, doctor.getId(), fecha, horaInicioCita, duracionMinutos);
            new Query(queryCita).hasSolution();
        }
    }

    public String encontrarHorarioDisponible(Doctor doctor, LocalDateTime fechaInicio) {

        String doctorId = doctor.getId().toString();
        String especialidad = doctor.getEspecialidad();
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
        String especialidad = doctor.getEspecialidad();
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
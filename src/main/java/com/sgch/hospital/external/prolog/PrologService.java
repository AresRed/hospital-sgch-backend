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
    // Constantes para los predicados dinámicos usados en Prolog
    private static final String PREDICADO_CITA = "cita_temporal";
    private static final String PREDICADO_DOCTOR = "doctor_temporal";
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Bloque de inicialización estático para asegurar que el archivo de reglas se
    // cargue una sola vez
    static {
        // La ruta debe ser relativa al CLASSPATH de Spring Boot
        String consultaCarga = "consult('src/main/resources/prolog/reglas_citas.pl')";
        try {
            // Inicializa el motor de Prolog si es necesario y ejecuta la consulta de carga
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
            // En un entorno de producción, aquí se lanzaría una excepción que detenga el
            // arranque
        }
    }

    /**
     * Limpia los hechos dinámicos insertados previamente por Java.
     * Es crucial para evitar que los datos de una sesión interfieran con la
     * siguiente.
     */
    public void limpiarHechosTemporales() {
        // Retractall elimina todos los hechos que coincidan con el patrón
        new Query("retractall(" + PREDICADO_CITA + "(_,_,_,_))").hasSolution();
        new Query("retractall(" + PREDICADO_DOCTOR + "(_,_,_,_,_))").hasSolution();
    }

    /**
     * Inyecta dinámicamente los datos del Doctor y sus citas ocupadas en el motor
     * de Prolog
     * como hechos (assertz/1), leyendo el horario desde la Entidad Doctor.
     * 
     * @param doctor        El doctor a consultar (contiene el horario real de la
     *                      DB).
     * @param citasOcupadas Las citas existentes del doctor obtenidas de la DB.
     */
    public void assertFacts(Doctor doctor, List<Cita> citasOcupadas) {

        // OBTENEMOS LOS VALORES DINÁMICOS DEL OBJETO DOCTOR (Paradigma OO)
        int duracionMinutos = doctor.getDuracionCitaMinutos();
        String horaInicio = doctor.getHorarioAtencionInicio();
        String horaFin = doctor.getHorarioAtencionFin();

        // 1. Inyectar el hecho del Doctor: doctor_temporal(ID, Especialidad, Duracion,
        // H_inicio, H_fin)
        // Usamos los campos reales de la DB (horaInicio, horaFin, duracionMinutos)
        String queryDoctor = String.format(
                "assertz(%s(%d, '%s', %d, '%s', '%s'))",
                PREDICADO_DOCTOR, doctor.getId(), doctor.getEspecialidad(), duracionMinutos, horaInicio, horaFin);
        new Query(queryDoctor).hasSolution();

        // 2. Inyectar las citas ocupadas: cita_temporal(ID, Fecha, HoraInicio,
        // Duracion)
        for (Cita cita : citasOcupadas) {
            String fecha = cita.getFechaHora().format(dateFormatter);
            String horaInicioCita = cita.getFechaHora().format(timeFormatter);

            // También usamos la duración dinámica aquí
            String queryCita = String.format(
                    "assertz(%s(%d, '%s', '%s', %d))", // Corrección: Añadir comillas a 'fecha' y 'horaInicioCita'
                    PREDICADO_CITA, doctor.getId(), fecha, horaInicioCita, duracionMinutos);
            new Query(queryCita).hasSolution();
        }
    }

    /**
     * Consulta a Prolog para encontrar la primera hora disponible.
     * Es el corazón de la lógica de agendamiento (PARADIGMA LÓGICO).
     * 
     * @param doctor      El doctor para el cual se busca la cita.
     * @param fechaInicio La fecha inicial para comenzar la búsqueda.
     * @return String con la hora disponible (HH:MM), o null si no hay.
     */
    public String encontrarHorarioDisponible(Doctor doctor, LocalDateTime fechaInicio) {

        // La consulta de Prolog se realiza sobre los hechos dinámicos que inyectamos.
        // horario_optimo(+IdDoctor, +Especialidad, +FechaBase, -HorarioEncontrado)
        String doctorId = doctor.getId().toString();
        String especialidad = doctor.getEspecialidad();
        String fechaBase = fechaInicio.format(dateFormatter); // Solo necesitamos la fecha para la consulta

        String predicado = String.format(
                "horario_optimo(%s, '%s', '%s', HorarioEncontrado)",
                doctorId, especialidad, fechaBase);

        Query consulta = new Query(predicado);

        try {
            if (consulta.hasSolution()) {
                // Si Prolog encuentra una solución (por unificación y backtracking)
                Term horario = consulta.oneSolution().get("HorarioEncontrado");
                return horario.toString().replace("'", ""); // Limpiamos las comillas de Prolog
            }
            return null; // No se encontró horario
        } catch (Exception e) {
            System.err.println("Error en la consulta de Prolog: " + e.getMessage());
            return null;
        } finally {
            consulta.close(); // Siempre cerrar la consulta para liberar recursos de Prolog
        }
    }

    public List<String> obtenerTodasHorasDisponibles(Doctor doctor, LocalDate fecha) {
        // Preparar el predicado de consulta
        String doctorId = doctor.getId().toString();
        String especialidad = doctor.getEspecialidad();
        String fechaBase = fecha.format(dateFormatter);

        // Predicado que Prolog resolverá
        String predicado = String.format(
                "horario_optimo(%s, '%s', '%s', HorarioEncontrado)",
                doctorId, especialidad, fechaBase);

        Query consulta = new Query(predicado);
        List<String> horasDisponibles = new ArrayList<>();

        try {
            // Query.allSolutions() obtiene todas las soluciones
            for (Map<String, Term> solution : consulta.allSolutions()) {
                // Extrae la variable 'HorarioEncontrado' de cada solución
                String hora = solution.get("HorarioEncontrado").toString().replace("'", "");
                horasDisponibles.add(hora);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo todas las soluciones de Prolog: " + e.getMessage());
        } finally {
            consulta.close(); // Siempre cerrar la consulta
        }
        return horasDisponibles;
    }

}
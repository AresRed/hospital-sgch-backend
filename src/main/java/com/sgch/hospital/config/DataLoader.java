package com.sgch.hospital.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sgch.hospital.model.entity.Administrador;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.DetalleReceta;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.Especialidad;
import com.sgch.hospital.model.entity.Expediente;
import com.sgch.hospital.model.entity.NotaMedica;
import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.model.entity.Receta;
import com.sgch.hospital.model.entity.Usuario.Rol;
import com.sgch.hospital.repository.BloqueoHorarioRepository;
import com.sgch.hospital.repository.CitaRepository;
import com.sgch.hospital.repository.DetalleRecetaRepository;
import com.sgch.hospital.repository.DoctorRepository;
import com.sgch.hospital.repository.EspecialidadRepository;
import com.sgch.hospital.repository.ExpedienteRepository;
import com.sgch.hospital.repository.NotaMedicaRepository;
import com.sgch.hospital.repository.PacienteRepository;
import com.sgch.hospital.repository.RecetaRepository;
import com.sgch.hospital.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CitaRepository citaRepository;
    private final DoctorRepository doctorRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ExpedienteRepository expedienteRepository;
    private final NotaMedicaRepository notaMedicaRepository;
    private final RecetaRepository recetaRepository;
    private final DetalleRecetaRepository detalleRecetaRepository;
    private final BloqueoHorarioRepository bloqueoHorarioRepository;

    @Override
    public void run(String... args) throws Exception {
        
        if (usuarioRepository.count() == 0) {
            System.out.println("Inicializando datos de prueba...");
            
            String testPassword = passwordEncoder.encode("test1234"); 
            
            // 1. Crear Especialidades
            Especialidad cardiologia = new Especialidad();
            cardiologia.setNombre("Cardiologia"); // Corregido a "Cardiologia"
            cardiologia.setDescripcion("Especialidad en el corazón y sistema circulatorio.");
            especialidadRepository.save(cardiologia);

            Especialidad pediatria = new Especialidad();
            pediatria.setNombre("Pediatria"); // Corregido a "Pediatria"
            pediatria.setDescripcion("Especialidad en la salud de niños y adolescentes.");
            especialidadRepository.save(pediatria);

            Especialidad dermatologia = new Especialidad();
            dermatologia.setNombre("Dermatologia"); // Corregido a "Dermatologia"
            dermatologia.setDescripcion("Especialidad en la piel, cabello y uñas.");
            especialidadRepository.save(dermatologia);

            Especialidad neurologia = new Especialidad();
            neurologia.setNombre("Neurologia"); // Corregido a "Neurologia"
            neurologia.setDescripcion("Especialidad en el sistema nervioso.");
            especialidadRepository.save(neurologia);

            // 2. Crear Administrador
            Administrador admin = new Administrador();
            admin.setDni("00000001A");
            admin.setNombre("Super");
            admin.setApellido("Admin");
            admin.setEmail("admin@test.com");
            admin.setPassword(testPassword);
            admin.setRol(Rol.ADMINISTRADOR);
            usuarioRepository.save(admin);
            
            // 3. Crear Doctores
            Doctor doctor1 = new Doctor();
            doctor1.setDni("00000002B");
            doctor1.setNombre("Dr.");
            doctor1.setApellido("Prolog");
            doctor1.setEmail("doctor@test.com");
            doctor1.setPassword(testPassword);
            doctor1.setRol(Rol.DOCTOR);
            doctor1.setEspecialidad(cardiologia); // Asignar especialidad
            doctor1.setHorarioAtencionInicio("09:00");
            doctor1.setHorarioAtencionFin("18:00");
            doctor1.setDuracionCitaMinutos(20);
            usuarioRepository.save(doctor1);
            
            Doctor doctor2 = new Doctor();
            doctor2.setDni("00000004D");
            doctor2.setNombre("Dra.");
            doctor2.setApellido("Java");
            doctor2.setEmail("doctor2@test.com");
            doctor2.setPassword(testPassword);
            doctor2.setRol(Rol.DOCTOR);
            doctor2.setEspecialidad(pediatria); // Asignar especialidad
            doctor2.setHorarioAtencionInicio("08:00");
            doctor2.setHorarioAtencionFin("17:00");
            doctor2.setDuracionCitaMinutos(30);
            usuarioRepository.save(doctor2);

            Doctor doctor3 = new Doctor();
            doctor3.setDni("00000005E");
            doctor3.setNombre("Dr.");
            doctor3.setApellido("Python");
            doctor3.setEmail("doctor3@test.com");
            doctor3.setPassword(testPassword);
            doctor3.setRol(Rol.DOCTOR);
            doctor3.setEspecialidad(dermatologia); // Asignar especialidad
            doctor3.setHorarioAtencionInicio("10:00");
            doctor3.setHorarioAtencionFin("19:00");
            doctor3.setDuracionCitaMinutos(15);
            usuarioRepository.save(doctor3);
            
            // 4. Crear Pacientes
            Paciente paciente1 = new Paciente();
            paciente1.setDni("00000003C");
            paciente1.setNombre("Juan");
            paciente1.setApellido("Perez");
            paciente1.setEmail("paciente@test.com");
            paciente1.setPassword(testPassword);
            paciente1.setRol(Rol.PACIENTE);
            paciente1.setSeguroMedico("RIMAC");
            usuarioRepository.save(paciente1);

            Paciente paciente2 = new Paciente();
            paciente2.setDni("00000006F");
            paciente2.setNombre("Maria");
            paciente2.setApellido("Gomez");
            paciente2.setEmail("paciente2@test.com");
            paciente2.setPassword(testPassword);
            paciente2.setRol(Rol.PACIENTE);
            paciente2.setSeguroMedico("PACIFICO");
            usuarioRepository.save(paciente2);

            Paciente paciente3 = new Paciente();
            paciente3.setDni("00000007G");
            paciente3.setNombre("Carlos");
            paciente3.setApellido("Ruiz");
            paciente3.setEmail("paciente3@test.com");
            paciente3.setPassword(testPassword);
            paciente3.setRol(Rol.PACIENTE);
            paciente3.setSeguroMedico("MAPFRE");
            usuarioRepository.save(paciente3);
            
            // 5. Crear Citas
            LocalDateTime now = LocalDateTime.now();

            // Cita Confirmada
            Cita cita1 = new Cita();
            cita1.setDoctor(doctor1);
            cita1.setPaciente(paciente1);
            cita1.setFechaHora(now.plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));
            cita1.setMotivo("Dolor de pecho");
            cita1.setEstado(Cita.EstadoCita.CONFIRMADA);
            citaRepository.save(cita1);

            // Cita Realizada para NotaMedica nm2
            Cita cita2 = new Cita();
            cita2.setDoctor(doctor2);
            cita2.setPaciente(paciente2);
            cita2.setFechaHora(now.minusDays(10).withHour(11).withMinute(0).withSecond(0).withNano(0)); // Fecha en el pasado
            cita2.setMotivo("Chequeo pediátrico");
            cita2.setEstado(Cita.EstadoCita.REALIZADA); // Cambiado a REALIZADA
            citaRepository.save(cita2);

            // Cita Finalizada
            Cita cita3 = new Cita();
            cita3.setDoctor(doctor1);
            cita3.setPaciente(paciente3);
            cita3.setFechaHora(now.minusDays(5).withHour(9).withMinute(0).withSecond(0).withNano(0));
            cita3.setMotivo("Revisión general");
            cita3.setEstado(Cita.EstadoCita.REALIZADA);
            citaRepository.save(cita3);

            // Cita Cancelada
            Cita cita4 = new Cita();
            cita4.setDoctor(doctor3);
            cita4.setPaciente(paciente1);
            cita4.setFechaHora(now.plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0));
            cita4.setMotivo("Consulta dermatológica");
            cita4.setEstado(Cita.EstadoCita.CANCELADA);
            citaRepository.save(cita4);

            Cita cita5 = new Cita();
            cita5.setDoctor(doctor1);
            cita5.setPaciente(paciente2);
            cita5.setFechaHora(now.plusDays(2).withHour(11).withMinute(30).withSecond(0).withNano(0));
            cita5.setMotivo("Seguimiento");
            cita5.setEstado(Cita.EstadoCita.PENDIENTE);
            citaRepository.save(cita5);
            
            // Cita 6: Doctor 2 (Pediatria) - CONFIRMADA (La próxima semana)
            Cita cita6 = new Cita();
            cita6.setDoctor(doctor2);
            cita6.setPaciente(paciente3);
            cita6.setFechaHora(now.plusDays(7).withHour(9).withMinute(0).withSecond(0).withNano(0));
            cita6.setMotivo("Control de niño sano");
            cita6.setEstado(Cita.EstadoCita.CONFIRMADA);
            citaRepository.save(cita6);

            

            // 6. Crear Expedientes
            Expediente exp1 = new Expediente();
            exp1.setPaciente(paciente1);
            // CORRECCIÓN: Inicializar campos
            exp1.setHistorialAlergias("Penicilina");
            exp1.setGrupoSanguineo("A+");
            exp1.setObservacionesGenerales("Paciente propenso a migrañas.");
            // Nota: La fechaCreacion es un timestamp que debería inicializarse al guardar en JPA/DB.
            expedienteRepository.save(exp1);

            Expediente exp2 = new Expediente();
            exp2.setPaciente(paciente2);
            exp2.setFechaCreacion(now.minusMonths(3));
            expedienteRepository.save(exp2);

            

            // 7. Crear Notas Médicas
            NotaMedica nm1 = new NotaMedica();
            nm1.setExpediente(exp1);
            nm1.setCita(cita3); // Asociar a una cita finalizada
            nm1.setDoctor(doctor1); // Asignar doctor
            nm1.setFechaHora(cita3.getFechaHora().plusMinutes(doctor1.getDuracionCitaMinutos()));
            nm1.setMotivoConsulta("Revisión general post-cita.");
            nm1.setDiagnostico("Paciente estable.");
            nm1.setRecomendaciones("Seguimiento anual y dieta balanceada.");
            notaMedicaRepository.save(nm1);

            NotaMedica nm2 = new NotaMedica();
            nm2.setExpediente(exp2);
            nm2.setCita(cita2); // Asociar a cita2
            nm2.setDoctor(doctor2); // Asignar doctor
            nm2.setFechaHora(cita2.getFechaHora().plusMinutes(doctor2.getDuracionCitaMinutos())); // Fecha posterior a la cita
            nm2.setMotivoConsulta("Primera consulta pediátrica.");
            nm2.setDiagnostico("Desarrollo normal para la edad.");
            nm2.setRecomendaciones("Vacunas al día y control en 6 meses.");
            notaMedicaRepository.save(nm2);

            // 8. Crear Recetas y Detalles de Receta
            Receta rec1 = new Receta();
            rec1.setNotaMedica(nm1); // Asociar a la nota médica
            rec1.setFechaEmision(nm1.getFechaHora()); // Usar fechaHora de NotaMedica
            recetaRepository.save(rec1);

            DetalleReceta detRec1 = new DetalleReceta();
            detRec1.setReceta(rec1);
            detRec1.setNombreMedicamento("Paracetamol");
            detRec1.setDosis("500mg cada 8 horas");
            detRec1.setInstrucciones("Tomar con alimentos.");
            detalleRecetaRepository.save(detRec1);

            DetalleReceta detRec2 = new DetalleReceta();
            detRec2.setReceta(rec1);
            detRec2.setNombreMedicamento("Amoxicilina");
            detRec2.setDosis("250mg cada 12 horas");
            detRec2.setInstrucciones("Completar el ciclo de 7 días.");
            detalleRecetaRepository.save(detRec2);

            // Añadir Receta para paciente2
            Receta rec2 = new Receta();
            rec2.setNotaMedica(nm2); // Asociar a la nota médica de paciente2
            rec2.setFechaEmision(nm2.getFechaHora());
            recetaRepository.save(rec2);

            DetalleReceta detRec3 = new DetalleReceta();
            detRec3.setReceta(rec2);
            detRec3.setNombreMedicamento("Ibuprofeno");
            detRec3.setDosis("400mg cada 6 horas");
            detRec3.setInstrucciones("Tomar si es necesario para el dolor.");
            detalleRecetaRepository.save(detRec3);

            System.out.println("¡Datos iniciales creados con éxito!");
        }
    }
}

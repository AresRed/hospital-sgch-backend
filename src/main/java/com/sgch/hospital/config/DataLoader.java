package com.sgch.hospital.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sgch.hospital.model.entity.Administrador;
import com.sgch.hospital.model.entity.Cita;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.model.entity.Usuario.Rol;
import com.sgch.hospital.repository.CitaRepository;
import com.sgch.hospital.repository.DoctorRepository;
import com.sgch.hospital.repository.PacienteRepository;
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
    @Override
    public void run(String... args) throws Exception {
        // Ejecutamos la lógica de inicialización solo si la base de datos está vacía
        if (usuarioRepository.count() == 0) {
            System.out.println("Inicializando datos de roles de prueba...");
            
            // Contraseña común para simplificar las pruebas
            String testPassword = passwordEncoder.encode("test1234"); 
            
            // 1. Crear Administrador
            Administrador admin = new Administrador();
            admin.setDni("00000001A");
            admin.setNombre("Super");
            admin.setApellido("Admin");
            admin.setEmail("admin@test.com");
            admin.setPassword(testPassword);
            admin.setRol(Rol.ADMINISTRADOR);
            usuarioRepository.save(admin);
            
            // 2. Crear Doctor
            Doctor doctor = new Doctor();
            doctor.setDni("00000002B");
            doctor.setNombre("Dr.");
            doctor.setApellido("Prolog");
            doctor.setEmail("doctor@test.com");
            doctor.setPassword(testPassword);
            doctor.setRol(Rol.DOCTOR);
            doctor.setEspecialidad("Cardiologia");
            usuarioRepository.save(doctor);
            
            // 3. Crear Paciente
            Paciente paciente = new Paciente();
            paciente.setDni("00000003C");
            paciente.setNombre("Juan");
            paciente.setApellido("Perez");
            paciente.setEmail("paciente@test.com");
            paciente.setPassword(testPassword);
            paciente.setRol(Rol.PACIENTE);
            paciente.setSeguroMedico("RIMAC");
            usuarioRepository.save(paciente);
            
            Doctor doctor2 = doctorRepository.findById(2L).orElse(null);
        if (doctor2 != null) {
            System.out.println("Cargando horario inicial para Doctor 2...");
            
            // Valores iniciales que el Administrador suele establecer
            doctor2.setHorarioAtencionInicio("09:00");
            doctor2.setHorarioAtencionFin("18:00");
            doctor2.setDuracionCitaMinutos(20); // Citas de 20 minutos por defecto
            
            doctorRepository.save(doctor2);
            System.out.println("Doctor 2 horario: 09:00 a 18:00, citas de 20 minutos.");
        }
        
        // 3. REGISTRAR UNA CITA DE PRUEBA (Bloqueo para Prolog)
        Doctor doctorBloqueo = doctorRepository.findById(2L).orElse(null);
        Paciente pacienteBloqueo = pacienteRepository.findById(3L).orElse(null); // Asumiendo ID 3 es un Paciente

        if (doctorBloqueo != null && pacienteBloqueo != null) {
            
            // Elegimos una fecha y hora futura para la cita inicial
            LocalDateTime fechaHoraCitaInicial = LocalDateTime.now()
                .plusDays(7) // La cita será dentro de 7 días
                .withHour(10) // A las 10:00 AM
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

            Cita citaInicial = new Cita();
            citaInicial.setDoctor(doctorBloqueo);
            citaInicial.setPaciente(pacienteBloqueo);
            citaInicial.setFechaHora(fechaHoraCitaInicial);
            citaInicial.setMotivo("Cita inicial de prueba para bloqueo.");
            citaInicial.setEstado(Cita.EstadoCita.CONFIRMADA);
            
            citaRepository.save(citaInicial);
            System.out.println("Cita de prueba programada para Doctor 2: " + fechaHoraCitaInicial.toString());
        }
            System.out.println("¡Datos iniciales creados con éxito!");
        }
    }
}

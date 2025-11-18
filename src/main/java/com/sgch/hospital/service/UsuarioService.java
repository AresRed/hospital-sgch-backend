package com.sgch.hospital.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sgch.hospital.model.DTO.AdminUpdateDTO;
import com.sgch.hospital.model.DTO.DoctorUpdateDTO;
import com.sgch.hospital.model.DTO.PacienteUpdateDTO;
import com.sgch.hospital.model.DTO.RegistroRequest;
import com.sgch.hospital.model.entity.Administrador;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.Especialidad;
import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.model.entity.Usuario;
import com.sgch.hospital.model.entity.Usuario.Rol;
import com.sgch.hospital.repository.EspecialidadRepository;
import com.sgch.hospital.repository.UsuarioRepository;
import com.sgch.hospital.util.mail.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EspecialidadRepository especialidadRepository; // <-- Nuevo Repositorio Inyectado

    @Transactional
    public Usuario registrarNuevoUsuario(Usuario usuario, Rol rol) throws Exception {
        System.out.println("DEBUG: Iniciando registrarNuevoUsuario para DNI: " + usuario.getDni());
        System.out.println("DEBUG: Verificando si existe DNI...");
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new Exception("El DNI ya está registrado.");
        }
        System.out.println("DEBUG: DNI no existe. Verificando si existe email...");
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new Exception("El email ya está registrado.");
        }

        // 1. Asignar Rol y cifrar contraseña
        System.out.println("DEBUG: Email no existe. Asignando rol y cifrando contraseña...");
        usuario.setRol(rol);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);

        // 2. Guardar en la DB
        Usuario nuevoUsuario;
        try {
            System.out.println("DEBUG: Intentando guardar el usuario en la BBDD...");
            nuevoUsuario = usuarioRepository.save(usuario);
            System.out.println("DEBUG: Usuario guardado exitosamente con ID: " + nuevoUsuario.getId());
        } catch (Exception e) {
            System.err.println("DEBUG: ¡ERROR AL GUARDAR EN LA BBDD! Causa: " + e.getMessage());
            e.printStackTrace(); // Imprime el stack trace completo para más detalles
            throw new Exception("Error en la base de datos al guardar el usuario.", e);
        }


        // 3. Enviar correo de verificación de forma asíncrona
        enviarCorreoDeVerificacion(nuevoUsuario);

        return nuevoUsuario;
    }

    @Async
    public void enviarCorreoDeVerificacion(Usuario usuario) {
        System.out.println("DEBUG: [ASYNC] Intentando enviar correo de verificación...");
        try {
            String token = "TOKEN_DE_EJEMPLO";
            String mensaje = String.format(
                    "Bienvenido a SGCH. Por favor verifica tu cuenta: http://localhost:8080/api/auth/verify?token=%s",
                    token);
            emailService.enviarCorreo(usuario.getEmail(), "Verificación de Cuenta SGCH", mensaje);
            System.out.println("DEBUG: [ASYNC] Correo de verificación enviado (simulado) a " + usuario.getEmail());
        } catch (Exception e) {
            System.err.println("DEBUG: [ASYNC] ¡ERROR AL ENVIAR CORREO! Causa: " + e.getMessage());
        }
    }

    // ===================================================================================
    // LÓGICA DE CONVERSIÓN Y REGISTRO (Maneja la lógica de Especialidad/Herencia)
    // ===================================================================================

    @Transactional
    public Usuario convertirYRegistrar(RegistroRequest req) throws Exception {
        System.out.println("DEBUG: Iniciando convertirYRegistrar...");
        System.out.println("DEBUG: Datos recibidos: " + req.toString());
        Rol rol;
        try {
            rol = Rol.valueOf(req.getRol().toUpperCase());
            System.out.println("DEBUG: Rol determinado: " + rol);
        } catch (IllegalArgumentException e) {
            throw new Exception("Rol no válido.");
        }

        // 1. Crear la instancia de la entidad específica (Herencia)
        Usuario usuario;
        if (rol == Rol.DOCTOR) {
            System.out.println("DEBUG: Creando entidad Doctor...");
            Doctor doctor = new Doctor();

            // Asignar valores por defecto que son NOT NULL en la BBDD
            System.out.println("DEBUG: Asignando valores por defecto al doctor...");
            doctor.setHorarioAtencionInicio("09:00");
            doctor.setHorarioAtencionFin("17:00");
            doctor.setDuracionCitaMinutos(30);

            Especialidad especialidad = null;

            if (req.getEspecialidadId() != null) {
                System.out.println("DEBUG: Buscando especialidad por ID: " + req.getEspecialidadId());
                especialidad = especialidadRepository.findById(req.getEspecialidadId())
                        .orElse(null);
            } else if (req.getEspecialidad() != null) {
                // Fallback: Buscar por nombre (compatibilidad con versión anterior)
                System.out.println("DEBUG: Buscando especialidad por nombre: " + req.getEspecialidad());
                especialidad = especialidadRepository.findByNombre(req.getEspecialidad());
            }

            if (especialidad == null) {
                throw new IllegalArgumentException(
                    "Especialidad no válida o no registrada. ID: " + req.getEspecialidadId() + 
                    ", Nombre: " + req.getEspecialidad()
                );
            }

            System.out.println("DEBUG: Especialidad encontrada: " + especialidad.getNombre());
            doctor.setEspecialidad(especialidad);
            usuario = doctor;

        } else if (rol == Rol.PACIENTE) {
            System.out.println("DEBUG: Creando entidad Paciente...");
            Paciente paciente = new Paciente();
            paciente.setSeguroMedico(req.getSeguroMedico());
            usuario = paciente;
            System.out.println("DEBUG: Entidad Paciente creada.");

        } else if (rol == Rol.ADMINISTRADOR) {
            System.out.println("DEBUG: Creando entidad Administrador...");
            Administrador administrador = new Administrador();
            usuario = administrador;

        } else {
            throw new Exception("Rol no soportado: " + rol);
        }

        // 2. Mapear campos comunes
        System.out.println("DEBUG: Mapeando campos comunes...");
        usuario.setDni(req.getDni());
        usuario.setNombre(req.getNombre());
        usuario.setApellido(req.getApellido());
        usuario.setEmail(req.getEmail());
        usuario.setPassword(req.getPassword()); // Será cifrada en registrarNuevoUsuario
        usuario.setTelefono(req.getTelefono());
        System.out.println("DEBUG: Campos comunes mapeados.");

        // 3. Pasar a la lógica central de registro
        System.out.println("DEBUG: Llamando a la lógica central de registro (registrarNuevoUsuario)...");
        return registrarNuevoUsuario(usuario, rol);
    }

    // ===================================================================================
    // LÓGICA DE BÚSQUEDA Y ACTUALIZACIÓN
    // ===================================================================================

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional
    public void actualizarEstado(Long userId, boolean activo) throws Exception {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new Exception("Usuario no encontrado."));

        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void actualizarDoctor(Long doctorId, DoctorUpdateDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findById(doctorId)
                .orElseThrow(() -> new Exception("Doctor no encontrado."));

        if (!(usuario instanceof Doctor)) {
            throw new Exception("El ID de usuario no corresponde a un Doctor.");
        }

        Doctor doctor = (Doctor) usuario;

        // Actualizar DNI si viene en el DTO
        if (dto.getDni() != null && !dto.getDni().isEmpty()) {
            // Verificar que no exista otro usuario con ese DNI
            if (!doctor.getDni().equals(dto.getDni()) && usuarioRepository.existsByDni(dto.getDni())) {
                throw new Exception("El DNI ya está registrado por otro usuario.");
            }
            doctor.setDni(dto.getDni());
        }

        // Actualizar campos básicos
        doctor.setNombre(dto.getNombre());
        doctor.setApellido(dto.getApellido());
        
        // Verificar si el email ya existe (si cambió)
        if (!doctor.getEmail().equals(dto.getEmail())) {
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new Exception("El email ya está registrado.");
            }
            doctor.setEmail(dto.getEmail());
        }

        if (dto.getTelefono() != null) {
            doctor.setTelefono(dto.getTelefono());
        }

        if (dto.getDireccion() != null) {
            doctor.setDireccion(dto.getDireccion());
        }

        // Actualizar contraseña solo si se proporciona
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            doctor.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Actualizar especialidad
        Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new Exception("Especialidad no encontrada."));
        doctor.setEspecialidad(especialidad);

        // Actualizar horarios
        doctor.setHorarioAtencionInicio(dto.getHorarioAtencionInicio());
        doctor.setHorarioAtencionFin(dto.getHorarioAtencionFin());
        doctor.setDuracionCitaMinutos(dto.getDuracionCitaMinutos());

        usuarioRepository.save(doctor);
    }

    @Transactional
    public void actualizarPerfilPaciente(Long pacienteId, PacienteUpdateDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findById(pacienteId)
                .orElseThrow(() -> new Exception("Usuario no encontrado."));

        if (!(usuario instanceof Paciente)) {
            throw new Exception("El ID de usuario no corresponde a un Paciente.");
        }

        Paciente paciente = (Paciente) usuario;

        // Actualizar DNI si viene en el DTO
        if (dto.getDni() != null && !dto.getDni().isEmpty()) {
            if (!paciente.getDni().equals(dto.getDni()) && usuarioRepository.existsByDni(dto.getDni())) {
                throw new Exception("El DNI ya está registrado por otro usuario.");
            }
            paciente.setDni(dto.getDni());
        }

        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        
        // Verificar email
        if (!paciente.getEmail().equals(dto.getEmail())) {
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new Exception("El email ya está registrado.");
            }
            paciente.setEmail(dto.getEmail());
        }

        if (dto.getTelefono() != null) {
            paciente.setTelefono(dto.getTelefono());
        }
        
        if (dto.getDireccion() != null) {
            paciente.setDireccion(dto.getDireccion());
        }

        if (dto.getSeguroMedico() != null) {
            paciente.setSeguroMedico(dto.getSeguroMedico());
        }

        // Actualizar contraseña solo si se proporciona
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            paciente.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuarioRepository.save(paciente);
    }

    public void actualizarAdministrador(Long adminId, AdminUpdateDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new Exception("Administrador no encontrado."));

        if (!(usuario instanceof Administrador)) {
            throw new Exception("El ID de usuario no corresponde a un Administrador.");
        }

        Administrador admin = (Administrador) usuario;

        // Actualizar DNI si viene en el DTO
        if (dto.getDni() != null && !dto.getDni().isEmpty()) {
            if (!admin.getDni().equals(dto.getDni()) && usuarioRepository.existsByDni(dto.getDni())) {
                throw new Exception("El DNI ya está registrado por otro usuario.");
            }
            admin.setDni(dto.getDni());
        }

        // Actualizar campos básicos
        admin.setNombre(dto.getNombre());
        admin.setApellido(dto.getApellido());
        
        // Verificar si el email ya existe (si cambió)
        if (!admin.getEmail().equals(dto.getEmail())) {
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new Exception("El email ya está registrado.");
            }
            admin.setEmail(dto.getEmail());
        }

        if (dto.getTelefono() != null) {
            admin.setTelefono(dto.getTelefono());
        }

        if (dto.getDireccion() != null) {
            admin.setDireccion(dto.getDireccion());
        }

        // Actualizar contraseña solo si se proporciona
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuarioRepository.save(admin);
    }

}

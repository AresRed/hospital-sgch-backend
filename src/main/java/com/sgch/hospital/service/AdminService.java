package com.sgch.hospital.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sgch.hospital.model.DTO.BloqueoHorarioRequest;
import com.sgch.hospital.model.entity.BloqueoHorario;
import com.sgch.hospital.model.entity.Doctor;
import com.sgch.hospital.model.entity.Paciente;
import com.sgch.hospital.model.entity.Usuario;
import com.sgch.hospital.repository.DoctorRepository;
import com.sgch.hospital.repository.PacienteRepository;
import com.sgch.hospital.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para gestión administrativa
 * Maneja operaciones CRUD de usuarios, doctores y pacientes
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final DoctorRepository doctorRepository;
    private final PacienteRepository pacienteRepository;

    // =================== GESTIÓN DE USUARIOS ===================
    
    /**
     * Obtiene todos los usuarios del sistema
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene un usuario por ID
     */
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Long id) throws Exception {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));
    }

    /**
     * Obtiene usuarios filtrados por rol
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosPorRol(Usuario.Rol rol) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol().equals(rol))
                .collect(Collectors.toList());
    }

    /**
     * Elimina un usuario del sistema
     */
    @Transactional
    public void eliminarUsuario(Long userId) throws Exception {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + userId));
        
        usuarioRepository.delete(usuario);
    }

    // =================== GESTIÓN DE DOCTORES ===================
    
    /**
     * Obtiene todos los doctores
     */
    @Transactional(readOnly = true)
    public List<Doctor> obtenerTodosLosDoctores() {
        return doctorRepository.findAll();
    }

    /**
     * Obtiene un doctor por ID
     */
    @Transactional(readOnly = true)
    public Doctor obtenerDoctorPorId(Long id) throws Exception {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new Exception("Doctor no encontrado con ID: " + id));
    }

    // =================== GESTIÓN DE PACIENTES ===================
    
    /**
     * Obtiene todos los pacientes
     */
    @Transactional(readOnly = true)
    public List<Paciente> obtenerTodosLosPacientes() {
        return pacienteRepository.findAll();
    }

    /**
     * Obtiene un paciente por ID
     */
    @Transactional(readOnly = true)
    public Paciente obtenerPacientePorId(Long id) throws Exception {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new Exception("Paciente no encontrado con ID: " + id));
    }

    // =================== ESTADÍSTICAS ===================
    
    /**
     * Obtiene conteo de usuarios por rol
     */
    @Transactional(readOnly = true)
    public long contarUsuariosPorRol(Usuario.Rol rol) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol().equals(rol))
                .count();
    }

    /**
     * Obtiene conteo de usuarios activos
     */
    @Transactional(readOnly = true)
    public long contarUsuariosActivos() {
        return usuarioRepository.findAll().stream()
                .filter(Usuario::isActivo)
                .count();
    }
}
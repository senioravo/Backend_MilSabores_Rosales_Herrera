package com.milsabores.usuario.service;

import com.milsabores.usuario.dto.*;
import com.milsabores.usuario.exception.EmailAlreadyExistsException;
import com.milsabores.usuario.exception.InvalidCredentialsException;
import com.milsabores.usuario.exception.UsuarioNotFoundException;
import com.milsabores.usuario.model.Usuario;
import com.milsabores.usuario.repository.UsuarioRepository;
import com.milsabores.usuario.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Registra un nuevo usuario
     */
    public AuthResponseDTO registrar(UsuarioRegistroDTO registroDTO) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(registroDTO.getNombre());
        usuario.setEmail(registroDTO.getEmail().toLowerCase().trim());
        // Hashear la contraseña con BCrypt
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));

        Usuario savedUsuario = usuarioRepository.save(usuario);
        
        // Generar JWT token
        String token = jwtUtil.generateToken(
            savedUsuario.getEmail(), 
            savedUsuario.getId(), 
            savedUsuario.getNombre()
        );

        // Preparar respuesta
        UsuarioResponseDTO usuarioResponse = convertToResponseDTO(savedUsuario);
        return new AuthResponseDTO(true, "Usuario registrado exitosamente", usuarioResponse, token);
    }

    /**
     * Inicia sesión de un usuario
     */
    public AuthResponseDTO login(UsuarioLoginDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new InvalidCredentialsException("Email o contraseña incorrectos"));

        // Verificar contraseña hasheada con BCrypt
        if (!passwordEncoder.matches(loginDTO.getPassword(), usuario.getPassword())) {
            throw new InvalidCredentialsException("Email o contraseña incorrectos");
        }

        if (!usuario.getActivo()) {
            throw new InvalidCredentialsException("Usuario inactivo");
        }
        
        // Generar JWT token
        String token = jwtUtil.generateToken(
            usuario.getEmail(), 
            usuario.getId(), 
            usuario.getNombre()
        );

        UsuarioResponseDTO usuarioResponse = convertToResponseDTO(usuario);
        return new AuthResponseDTO(true, "Inicio de sesión exitoso", usuarioResponse, token);
    }

    /**
     * Obtiene todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        return convertToResponseDTO(usuario);
    }

    /**
     * Actualiza un usuario
     */
    public UsuarioResponseDTO actualizar(Long id, UsuarioRegistroDTO updateDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

        // Verificar si el nuevo email ya existe (y no es el mismo usuario)
        if (!usuario.getEmail().equals(updateDTO.getEmail()) 
            && usuarioRepository.existsByEmail(updateDTO.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        usuario.setNombre(updateDTO.getNombre());
        usuario.setEmail(updateDTO.getEmail().toLowerCase().trim());
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            // Hashear la nueva contraseña
            usuario.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return convertToResponseDTO(updatedUsuario);
    }
    
    /**
     * Actualiza parcialmente un usuario (PATCH)
     */
    public UsuarioResponseDTO actualizarParcial(Long id, UsuarioRegistroDTO updateDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

        // Solo actualizar campos no nulos
        if (updateDTO.getNombre() != null && !updateDTO.getNombre().isEmpty()) {
            usuario.setNombre(updateDTO.getNombre());
        }
        
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().isEmpty()) {
            // Verificar si el nuevo email ya existe (y no es el mismo usuario)
            if (!usuario.getEmail().equals(updateDTO.getEmail()) 
                && usuarioRepository.existsByEmail(updateDTO.getEmail())) {
                throw new EmailAlreadyExistsException("El email ya está registrado");
            }
            usuario.setEmail(updateDTO.getEmail().toLowerCase().trim());
        }
        
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return convertToResponseDTO(updatedUsuario);
    }

    /**
     * Elimina (desactiva) un usuario
     */
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Convierte Usuario a UsuarioResponseDTO
     */
    private UsuarioResponseDTO convertToResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getFechaRegistro(),
                usuario.getActivo()
        );
    }
}

package com.verodata.retail.services;

import com.verodata.retail.entities.Rol;
import com.verodata.retail.entities.Usuario;
import com.verodata.retail.repositories.RolRepository;
import com.verodata.retail.repositories.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Inyección de dependencias por constructor unificado
    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public Usuario registrarUsuario(Usuario usuario, String rolFinal) {
        // 1. Validar que el correo electrónico no esté registrado (Mantiene tu regla de negocio original)
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya se encuentra registrado.");
        }

        // 2. Sanitizar el rol final procesado de forma limpia
        String nombreParaBuscar = (rolFinal != null && !rolFinal.trim().isEmpty())
                ? rolFinal.trim().toUpperCase()
                : "ROLE_CLIENTE";

        // 3. Buscar si el Rol determinado existe en PostgreSQL, si no, lo creamos dinámicamente
        Rol rol = rolRepository.findByNombre(nombreParaBuscar)
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombre(nombreParaBuscar);
                    return rolRepository.save(nuevoRol);
                });

        // 4. ENCRIPTAR la contraseña usando el BCryptPasswordEncoder corporativo
        String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);

        // 5. Vincular el rol al grafo de objetos del usuario
        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    // Método general para Guardar/Actualizar
    @Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Obtener la lista completa de todos los usuarios de la base de datos
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // Buscar un usuario específico usando su ID único
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // DELETE - Eliminar el registro físicamente de PostgreSQL en cascada por su ID
    @Transactional
    public void eliminarPorId(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Consulta personalizada usando tu consulta nativa conceptual JPQL para filtrar por Rol
    @Transactional(readOnly = true)
    public List<Usuario> buscarPorRol(String nombreRol) {
        return usuarioRepository.buscarUsuariosPorNombreDeRol(nombreRol);
    }
}
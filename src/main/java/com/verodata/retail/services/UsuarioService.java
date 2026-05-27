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

    // Inyección de dependencias por constructor
    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public Usuario registrarUsuario(Usuario usuario, String nombreRol) {
        // 1. Validar que el correo electrónico no esté registrado (Mantiene tu lógica original)
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya se encuentra registrado.");
        }
        // Establecemos el rol predeterminado como ROLE_CLIENTE
        String rolFinal = "ROLE_CLIENTE";

        // Regla A: Si se definió un rol a través de la URL (?nombreRol=...)
        if (nombreRol != null && !nombreRol.trim().isEmpty()) {
            rolFinal = nombreRol.trim();
        }
        // Regla B: Si no vino en la URL, pero sí vino estructurado dentro del Body JSON
        else if (usuario.getRol() != null && usuario.getRol().getNombre() != null && !usuario.getRol().getNombre().trim().isEmpty()) {
            rolFinal = usuario.getRol().getNombre().trim();
        }
        // 2. Buscar si el Rol determinado existe, si no, lo creamos dinámicamente
        String nombreParaBuscar = rolFinal;
        Rol rol = rolRepository.findByNombre(nombreParaBuscar)
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombre(nombreParaBuscar);
                    return rolRepository.save(nuevoRol);
                });
        // 3. ENCRIPTAR la contraseña usando BCrypt
        String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);
        usuario.setRol(rol); // Asignamos el rol al usuario
        return usuarioRepository.save(usuario); // 4. Guardar en la base de datos de PostgreSQL
    }

    // 1. Método general para Guardar/Actualizar
    @Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // 2. Obtener la lista completa de todos los usuarios de la base de datos
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // 3. Buscar un usuario específico usando su ID único
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // 4. DELETE - Eliminar el registro físicamente de PostgreSQL por su ID
    @Transactional
    public void eliminarPorId(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    // Consulta personalizada usando JPQL para filtrar por Rol
    @Transactional(readOnly = true)
    public List<Usuario> buscarPorRol(String nombreRol) {
        return usuarioRepository.buscarUsuariosPorNombreDeRol(nombreRol);
    }
}
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

    // Inyección de dependencias por constructor (SE MANTIENE EXACTAMENTE IGUAL)
    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // --- TU LÓGICA ACTUAL (INTACTA Y PROTEGIDA) ---
    @Transactional
    public Usuario registrarUsuario(Usuario usuario, String nombreRol) {
        // 1. Validar que el correo electrónico no esté registrado
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya se encuentra registrado.");
        }

        // 2. Buscar si el Rol existe, si no, lo creamos dinámicamente
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombre(nombreRol);
                    return rolRepository.save(nuevoRol);
                });

        // 3. ENCRIPTAR la contraseña usando BCrypt (Requerimiento de la rúbrica)
        String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);

        // Asignamos el rol al usuario
        usuario.setRol(rol);

        // 4. Guardar en la base de datos de PostgreSQL
        return usuarioRepository.save(usuario);
    }


    // --- NUEVOS MÉTODOS ACOPLADOS PARA EL CRUD COMPLETO ---

    // 1. Método general para Guardar/Actualizar (Esencial para la lógica de UPDATE del Controlador)
    @Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // 2. READ - Obtener la lista completa de todos los usuarios de la base de datos
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // 3. READ - Buscar un usuario específico usando su ID único
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // 4. DELETE - Eliminar el registro físicamente de PostgreSQL por su ID
    @Transactional
    public void eliminarPorId(Long id) {
        usuarioRepository.deleteById(id);
    }
}
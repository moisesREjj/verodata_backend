package com.verodata.retail.controllers;

import com.verodata.retail.entities.LoginRequest;
import com.verodata.retail.entities.Usuario;
import com.verodata.retail.services.JwtService;
import com.verodata.retail.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;


    public AuthController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    // Ruta final: http://localhost:8081/api/auth/registrar
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(
            @RequestBody Map<String, Object> request, // Cambiado a Object para soportar JSON anidado
            @RequestParam(required = false) String nombreRol // Atrapa el parámetro de la URL si existe
    ) {
        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre((String) request.get("nombre"));
            nuevoUsuario.setEmail((String) request.get("email"));
            nuevoUsuario.setPassword((String) request.get("password"));

            // --- LÓGICA DE PRIORIZACIÓN DE ROL INTEGRADA ---
            String rolFinal = "ROLE_CLIENTE"; // Valor base por defecto

            // Prioridad 1: Si se envió por Query Parameter en la URL (?nombreRol=...)
            if (nombreRol != null && !nombreRol.trim().isEmpty()) {
                rolFinal = nombreRol.trim();
            }
            // Prioridad 2: Si viene en el JSON
            else if (request.get("rol") != null) {
                Object rolObj = request.get("rol");

                if (rolObj instanceof Map) {
                    // Si viene anidado como en tu primer intento: "rol": { "nombre": "ROLE_ADMIN" }
                    Map<?, ?> rolMap = (Map<?, ?>) rolObj;
                    if (rolMap.get("nombre") != null) {
                        rolFinal = rolMap.get("nombre").toString().trim();
                    }
                } else {
                    // Si viene plano en el JSON: "rol": "ROLE_ADMIN"
                    rolFinal = rolObj.toString().trim();
                }
            }

            // Llamamos a tu servicio pasándole el objeto armado y el rol resuelto de forma dinámica
            Usuario usuarioGuardado = usuarioService.registrarUsuario(nuevoUsuario, rolFinal);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "¡Usuario registrado exitosamente!",
                    "id", usuarioGuardado.getId(),
                    "email", usuarioGuardado.getEmail(),
                    "rol", usuarioGuardado.getRol() != null ? usuarioGuardado.getRol().getNombre() : "SIN_ROL"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 1. READ - Obtener todos los usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<?> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    // 2. READ - Buscar un usuario específico por su ID
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        // .findById(id) busca por su llave primaria
        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(usuario))
                .orElse(ResponseEntity.notFound().build());
    }
    // Nueva ruta JPQL: http://localhost:8081/api/auth/usuarios/rol/ROLE_ADMIN
    @GetMapping("/usuarios/rol/{nombreRol}")
    public ResponseEntity<?> obtenerUsuariosPorRol(@PathVariable String nombreRol) {
        List<Usuario> usuarios = usuarioService.buscarPorRol(nombreRol);
        if (usuarios.isEmpty()) {
            return ResponseEntity.ok(Map.of("mensaje", "No se encontraron usuarios con el rol: " + nombreRol));
        }
        return ResponseEntity.ok(usuarios);
    }

    // 3. UPDATE - Actualizar los datos de un usuario existente
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario datosNuevos) {
        return usuarioService.buscarPorId(id).map(usuarioExistente -> {
            // Le pasamos los datos nuevos al usuario que ya existe
            usuarioExistente.setNombre(datosNuevos.getNombre());
            usuarioExistente.setEmail(datosNuevos.getEmail());

            // ¡Magia! Como usuarioExistente YA TIENE UN ID, .save() hará un UPDATE en vez de un INSERT
            Usuario usuarioActualizado = usuarioService.guardar(usuarioExistente);
            return ResponseEntity.ok(usuarioActualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE - Eliminar un usuario por su ID
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        return usuarioService.buscarPorId(id).map(usuario -> {
            // .deleteById(id) borra el registro físicamente de PostgreSQL
            usuarioService.eliminarPorId(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. Buscar al usuario por correo electrónico
        return usuarioService.buscarPorEmail(loginRequest.getEmail()).map(usuario -> {
            // 2. Validar si la contraseña coincide con la encriptada en la BD
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

            if (encoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                String token = jwtService.generarToken(usuario); // 3. Si es correcta, fabricamos el Token JWT
                // Formamos la respuesta JSON para enviar a Postman
                java.util.Map<String, String> respuesta = new java.util.HashMap<>();
                respuesta.put("mensaje", "¡Inicio de sesión exitoso!");
                respuesta.put("token", token);
                return ResponseEntity.ok(respuesta);
            } else {
                return ResponseEntity.status(401).body("Contraseña incorrecta.");
            }
        }).orElse(ResponseEntity.status(404).body("El usuario con ese correo no está registrado."));
    }
}
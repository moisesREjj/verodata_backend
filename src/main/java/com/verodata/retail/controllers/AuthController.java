package com.verodata.retail.controllers;

import com.verodata.retail.entities.Usuario;
import com.verodata.retail.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Ruta final: http://localhost:8081/api/auth/registrar
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> request) {
        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(request.get("nombre"));
            nuevoUsuario.setEmail(request.get("email"));
            nuevoUsuario.setPassword(request.get("password"));

            String rolAsignado = request.get("rol");
            if (rolAsignado == null || rolAsignado.isEmpty()) {
                rolAsignado = "ROLE_CLIENTE"; // Rol por defecto si no mandan ninguno
            }

            Usuario usuarioGuardado = usuarioService.registrarUsuario(nuevoUsuario, rolAsignado);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "¡Usuario registrado exitosamente!",
                    "id", usuarioGuardado.getId(),
                    "email", usuarioGuardado.getEmail(),
                    "rol", usuarioGuardado.getRol().getNombre()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 1. READ - Obtener todos los usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<?> obtenerTodos() {
        // Borra la línea vieja que tenía espacios y pon esta línea limpia:
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
}

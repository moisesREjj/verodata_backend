package com.verodata.retail.controllers;

import com.verodata.retail.entities.LoginRequest;
import com.verodata.retail.entities.Usuario;
import com.verodata.retail.entities.ClienteDetalle;
import com.verodata.retail.entities.AnalistaDetalle;
import com.verodata.retail.entities.AdminDetalle;
import com.verodata.retail.services.JwtService;
import com.verodata.retail.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(
            @RequestBody Map<String, Object> request,
            @RequestParam(required = false) String nombreRol
    ) {
        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre((String) request.get("nombre"));
            nuevoUsuario.setEmail((String) request.get("email"));
            nuevoUsuario.setPassword((String) request.get("password"));

            // --- LÓGICA DE PRIORIZACIÓN DE ROL INTEGRADA ---
            String rolFinal = "ROLE_CLIENTE";

            if (nombreRol != null && !nombreRol.trim().isEmpty()) {
                rolFinal = nombreRol.trim();
            }
            else if (request.get("rol") != null) {
                Object rolObj = request.get("rol");

                if (rolObj instanceof Map) {
                    Map<?, ?> rolMap = (Map<?, ?>) rolObj;
                    if (rolMap.get("nombre") != null) {
                        rolFinal = rolMap.get("nombre").toString().trim();
                    }
                } else {
                    rolFinal = rolObj.toString().trim();
                }
            }

            // Sanitización estricta para control de flujo
            rolFinal = rolFinal.toUpperCase();

            // --- RESOLUCIÓN Y CAPTURA DE DATOS ANIDADOS SEGÚN EL ROL CORPORATIVO ---
            switch (rolFinal) {
                case "ROLE_CLIENTE":
                    if (request.containsKey("datosCliente")) {
                        Map<?, ?> datos = (Map<?, ?>) request.get("datosCliente");
                        ClienteDetalle cd = new ClienteDetalle();
                        cd.setDireccionEnvio(datos.get("direccionEnvio") != null ? datos.get("direccionEnvio").toString() : null);
                        cd.setCodigoPostal(datos.get("codigoPostal") != null ? datos.get("codigoPostal").toString() : null);
                        cd.setTelefono(datos.get("telefono") != null ? datos.get("telefono").toString() : null);
                        nuevoUsuario.setClienteDetalle(cd);
                    }
                    break;

                case "ROLE_ANALISTA":
                    if (request.containsKey("datosAnalista")) {
                        Map<?, ?> datos = (Map<?, ?>) request.get("datosAnalista");
                        AnalistaDetalle ad = new AnalistaDetalle();
                        ad.setRegionAsignada(datos.get("regionAsignada") != null ? datos.get("regionAsignada").toString() : null);
                        ad.setNivelAccesoDashboard(datos.get("nivelAccesoDashboard") != null ? datos.get("nivelAccesoDashboard").toString() : null);
                        ad.setEspecialidad(datos.get("especialidad") != null ? datos.get("especialidad").toString() : null);
                        nuevoUsuario.setAnalistaDetalle(ad);
                    }
                    break;

                case "ROLE_ADMIN":
                    if (request.containsKey("datosAdmin")) {
                        Map<?, ?> datos = (Map<?, ?>) request.get("datosAdmin");
                        AdminDetalle adm = new AdminDetalle();
                        adm.setSedeSupervisada(datos.get("sedeSupervisada") != null ? datos.get("sedeSupervisada").toString() : null);
                        adm.setCodigoEmpleado(datos.get("codigoEmpleado") != null ? datos.get("codigoEmpleado").toString() : null);
                        nuevoUsuario.setAdminDetalle(adm);
                    }
                    break;
            }

            // Consérvase la ejecución original del servicio que cifra contraseñas y asigna el rol en base de datos
            Usuario usuarioGuardado = usuarioService.registrarUsuario(nuevoUsuario, rolFinal);

            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(usuario))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuarios/rol/{nombreRol}")
    public ResponseEntity<?> obtenerUsuariosPorRol(@PathVariable String nombreRol) {
        List<Usuario> usuarios = usuarioService.buscarPorRol(nombreRol);
        if (usuarios.isEmpty()) {
            return ResponseEntity.ok(Map.of("mensaje", "No se encontraron usuarios con el rol: " + nombreRol));
        }
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario datosNuevos) {
        return usuarioService.buscarPorId(id).map(usuarioExistente -> {
            usuarioExistente.setNombre(datosNuevos.getNombre());
            usuarioExistente.setEmail(datosNuevos.getEmail());

            // Si en la actualización envían detalles de objetos hijos, se actualizan dinámicamente
            if (datosNuevos.getClienteDetalle() != null) usuarioExistente.setClienteDetalle(datosNuevos.getClienteDetalle());
            if (datosNuevos.getAnalistaDetalle() != null) usuarioExistente.setAnalistaDetalle(datosNuevos.getAnalistaDetalle());
            if (datosNuevos.getAdminDetalle() != null) usuarioExistente.setAdminDetalle(datosNuevos.getAdminDetalle());

            Usuario usuarioActualizado = usuarioService.guardar(usuarioExistente);
            return ResponseEntity.ok(usuarioActualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        return usuarioService.buscarPorId(id).map(usuario -> {
            usuarioService.eliminarPorId(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return usuarioService.buscarPorEmail(loginRequest.getEmail()).map(usuario -> {
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

            if (encoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                String token = jwtService.generarToken(usuario);
                java.util.Map<String, Object> respuesta = new java.util.HashMap<>();
                respuesta.put("mensaje", "¡Inicio de sesión exitoso!");
                respuesta.put("token", token);
                respuesta.put("usuario", usuario); // Retorna el objeto completo hidratado
                return ResponseEntity.ok(respuesta);
            } else {
                return ResponseEntity.status(401).body("Contraseña incorrecta.");
            }
        }).orElse(ResponseEntity.status(404).body("El usuario con ese correo no está registrado."));
    }
}
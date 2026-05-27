package com.verodata.retail.repositories;

import com.verodata.retail.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Esto nos servirá para el Login y para evitar correos duplicados
    Optional<Usuario> findByEmail(String email);
    // --- NUEVA CONSULTA JPQL PERSONALIZADA ---
    // usamos "Usuario u" y "u.rol.nombre" (Objetos y atributos de Java, NO tablas SQL)
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = :nombreRol")
    List<Usuario> buscarUsuariosPorNombreDeRol(@Param("nombreRol") String nombreRol);
}
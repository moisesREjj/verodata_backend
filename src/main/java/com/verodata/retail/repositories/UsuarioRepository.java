package com.verodata.retail.repositories;

import com.verodata.retail.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Esto nos servirá para el Login y para evitar correos duplicados
    Optional<Usuario> findByEmail(String email);
}
package com.verodata.retail.repositories;

import com.verodata.retail.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    // Esto nos servirá para buscar un rol por su nombre (Ej: "ROLE_CLIENTE")
    Optional<Rol> findByNombre(String nombre);
}
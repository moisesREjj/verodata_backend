package com.verodata.retail.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "analista_detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalistaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region_asignada", length = 100)
    private String regionAsignada; // Ej: Lacio, Lombardía, Campania

    @Column(name = "nivel_acceso_dashboard", length = 50)
    private String nivelAccesoDashboard; // Ej: AVANZADO, INTERMEDIO

    @Column(length = 150)
    private String especialidad;
}
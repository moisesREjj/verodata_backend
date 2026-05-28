package com.verodata.retail.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sede_supervisada", length = 150)
    private String sedeSupervisada;

    @Column(name = "codigo_empleado", length = 50)
    private String codigoEmpleado;
}
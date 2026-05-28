package com.verodata.retail.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cliente_detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "direccion_envio", length = 255)
    private String direccionEnvio;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(length = 50)
    private String telefono;
}
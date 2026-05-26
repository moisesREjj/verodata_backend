package com.verodata.retail.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.verodata.retail.entities.Usuario;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    // Esta es la firma secreta para asegurar que nadie altere el Token.
    private static final String CLAVE_SECRETA = "VeroData_Firma_Secreta_Super_Segura_2026";
    // El token vencerá en 24 horas
    private static final int TIEMPO_EXPIRACION = 86400000;

    public String generarToken(Usuario usuario) {
        Algorithm algoritmo = Algorithm.HMAC256(CLAVE_SECRETA);

        return JWT.create()
                .withIssuer("verodata-backend")
                .withSubject(usuario.getEmail())
                .withClaim("nombre", usuario.getNombre())
                .withClaim("rol", usuario.getRol() != null ? usuario.getRol().getNombre() : "SIN_ROL")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + TIEMPO_EXPIRACION))
                .sign(algoritmo);
    }
}
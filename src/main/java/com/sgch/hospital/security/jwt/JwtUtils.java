package com.sgch.hospital.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Valores inyectados desde application.yml
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @PostConstruct // Asegura que esta función se ejecute después de la inyección de valores
    public void debugSecret() {
        // ESTA LÍNEA ES SOLO TEMPORAL DE DEPURACIÓN CRÍTICA
        // Verifica que el secreto cargado sea el mismo que pusiste en
        // application.properties
        System.out.println("DEBUG JWT: Secreto cargado: " + jwtSecret);
    }

    // Método para obtener la clave de firma
    private Key key() {
        // Genera una clave segura a partir del secreto (debe ser de al menos 256 bits)
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Genera un token JWT para un usuario autenticado.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername())) // El email es el sujeto
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Obtiene el nombre de usuario (email) del token.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Valida la integridad del token (firma y expiración).
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (Exception e) {
            // Logear errores de validación (firma inválida, expirado, etc.)
            logger.error("Token JWT inválido: {}", e.getMessage());
        }
        return false;
    }
}

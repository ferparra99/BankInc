package com.nexos.bankinc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio JWT - Maneja la generación y validación de JSON Web Tokens
 * =========================================================================
 *
 * Un JWT es un estándar abierto (RFC 7519) que define un formato compacto y
 * auto-contenido para transmitir información segura entre partes como un objeto JSON.
 *
 * Estructura de un JWT:
 * ---------------------
 * Header.Payload.Signature
 * - Header: Algoritmo (HS256) y tipo de token
 * - Payload: Claims (datos) - username, roles, expiración, etc.
 * - Signature: Firma digital que valida la integridad del token
 *
 * El token es firmado con una clave secreta usando HMAC-SHA256, lo que
 * garantiza que no puede ser modificado sin conocer la clave.
 */
@Service
public class JwtService {

    /**
     * Clave secreta para firmar los tokens
     * IMPORTANTE: En producción, usar una clave de al menos 256 bits (32 bytes)
     * y almacenarla en variables de entorno, nunca en código fuente.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Tiempo de expiración del token en segundos
     * Por defecto: 86400 (24 horas)
     */
    @Value("${jwt.expiration:86400}")
    private Long expiration;

    /**
     * Obtiene la clave de firma para HMAC
     * Convierte el string secret en una clave compatible con HMAC-SHA256
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Extrae el username (subject) del token
     * El "subject" es el identificador principal del usuario
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Método genérico para extraer cualquier claim del token
     * @param token El JWT token
     * @param claimsResolver Función que define qué claim extraer
     * @return El valor del claim solicitado
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parsea y valida el token, extrayendo todos los claims
     * Este método también valida la firma del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifica si el token ha expirado
     * Compara la fecha de expiración con la fecha actual
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Genera un nuevo token JWT para el usuario
     * Incluye:
     * - Subject: username del usuario
     * - Issued At: fecha de creación
     * - Expiration: fecha de expiración
     * - Claims: información adicional (actualmente vacío)
     *
     * @param userDetails Información del usuario (de Spring Security)
     * @return Token JWT firmado y codificado
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // En el futuro se pueden agregar claims personalizados:
        // claims.put("roles", userDetails.getAuthorities());
        // claims.put("clientId", userDetails.getClientId());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Crea el token JWT con todos sus componentes
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)              // Datos adicionales (puede estar vacío)
                .setSubject(subject)           // Username del usuario
                .setIssuedAt(new Date(System.currentTimeMillis()))  // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))  // Fecha expiración
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Firma con HMAC-SHA256
                .compact();                    // Genera el string codificado
    }

    /**
     * Valida un token JWT
     * Verifica dos cosas:
     * 1. El username del token coincide con el username del usuario
     * 2. El token no ha expirado
     *
     * @param token El JWT a validar
     * @param userDetails Información del usuario para comparar
     * @return true si el token es válido, false si no lo es
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Validamos que el username coincida Y que no esté expirado
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
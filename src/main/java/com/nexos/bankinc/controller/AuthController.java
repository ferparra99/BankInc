package com.nexos.bankinc.controller;

import com.nexos.bankinc.dto.request.LoginRequest;
import com.nexos.bankinc.dto.response.AuthResponse;
import com.nexos.bankinc.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Endpoint de LOGIN
     * =================
     * Flujo:
     * 1. Recibe { username, password } del cliente
     * 2. Envía las credenciales al AuthenticationManager
     * 3. Si son válidas, genera un JWT token
     * 4. Retorna el token al cliente
     *
     * El cliente debe guardar este token y enviarlo en el header
     * Authorization: Bearer <token> en cada petición protegida
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        // Paso 1: Autenticar las credenciales con Spring Security
        // Si el usuario no existe o la contraseña es incorrecta,
        // se lanza una AuthenticationException (401 Unauthorized)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Paso 2: Obtener los detalles del usuario autenticado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Paso 3: Generar el JWT token con la info del usuario
        String token = jwtService.generateToken(userDetails);

        // Paso 4: Retornar el token al cliente
        return ResponseEntity.ok(new AuthResponse(token));
    }

    /**
     * Endpoint de LOGOUT
     * ==================
     * En un sistema stateless con JWT, el logout del lado del servidor
     * es conceptualmente diferente a las sesiones tradicionales:
     *
     * - El token JWT sigue siendo válido hasta su expiración
     * - No podemos "invalidar" el token en el servidor (stateless)
     * - El logout se maneja del lado del cliente eliminando el token
     *
     * Lo que hacemos aquí es:
     * 1. Limpiar el SecurityContext de la sesión actual (por si acaso)
     * 2. Retornar una confirmación al cliente
     *
     * NOTA: Para invalidación real de tokens, implementar una blacklist
     * en Redis o base de datos, o usar tokens de corta duración.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Limpiar el contexto de seguridad de la sesión actual
        // Esto solo afecta al servidor, no al token en sí
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Logout exitoso. Por favor elimine el token del lado del cliente.");
    }
}
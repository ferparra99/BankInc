package com.nexos.bankinc.config;

import com.nexos.bankinc.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ============================================================
            // SEGURIDAD BÁSICA
            // ============================================================

            // CSRF deshabilitado: No necesario para APIs stateless con JWT
            // (CSRF protege contra ataques en apps que usan cookies/sessions)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS habilitado: Permite configuraciones de cross-origin
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Session STATELESS: Cada request es independiente
            // No se guardan sesiones en el servidor, el JWT contiene toda la info
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ============================================================
            // CONTROL DE ACCESO POR ENDPOINTS
            // ============================================================
            .authorizeHttpRequests(auth -> auth
                // --------------------------------------------------------
                // ENDPOINTS PÚBLICOS (sin autenticación requerida)
                // --------------------------------------------------------
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/auth/**").permitAll()

                // --------------------------------------------------------
                // ENDPOINTS DE TARJETAS (protegidos por rol)
                // --------------------------------------------------------
                // GET /card/{productId}/number - Generar número de tarjeta
                // Roles: ADMIN, CAJERO (empleados que emiten tarjetas)
                .requestMatchers(HttpMethod.GET, "/card/{productId}/number").hasAnyRole("ADMIN", "CAJERO")

                // POST /card/enroll - Inscribir/asociar tarjeta
                // Roles: ADMIN, CAJERO, CLIENTE (todos pueden asociar tarjetas)
                .requestMatchers(HttpMethod.POST, "/card/enroll").hasAnyRole("ADMIN", "CAJERO", "CLIENTE")

                // DELETE /card/{cardId} - Eliminar tarjeta
                // Roles: ADMIN, SUPERVISOR (solo personal administrativo)
                .requestMatchers(HttpMethod.DELETE, "/card/{cardId}").hasAnyRole("ADMIN", "SUPERVISOR")

                // POST /card/balance - Recargar saldo
                // Roles: ADMIN, CAJERO, CLIENTE (todos pueden recargar)
                .requestMatchers(HttpMethod.POST, "/card/balance").hasAnyRole("ADMIN", "CAJERO", "CLIENTE")

                // GET /card/balance/{cardId} - Consultar saldo
                // Roles: ADMIN, CAJERO, CLIENTE (propietarios y empleados)
                .requestMatchers(HttpMethod.GET, "/card/balance/{cardId}").hasAnyRole("ADMIN", "CAJERO", "CLIENTE")

                // GET /card/cards - Listar todas las tarjetas
                // Roles: ADMIN, SUPERVISOR (solo personal administrativo)
                .requestMatchers(HttpMethod.GET, "/card/cards").hasAnyRole("ADMIN", "SUPERVISOR")

                // DELETE /card/activeCard/{cardId} - Bloquear tarjeta activa
                // Roles: ADMIN, SUPERVISOR (solo personal administrativo)
                .requestMatchers(HttpMethod.DELETE, "/card/activeCard/{cardId}").hasAnyRole("ADMIN", "SUPERVISOR")

                // --------------------------------------------------------
                // ENDPOINTS DE TRANSACCIONES (protegidos por rol)
                // --------------------------------------------------------
                // POST /transaction/purchase - Realizar compra
                // Roles: ADMIN, CAJERO, CLIENTE
                .requestMatchers(HttpMethod.POST, "/transaction/purchase").hasAnyRole("ADMIN", "CAJERO", "CLIENTE")

                // GET /transaction/{transactionId} - Consultar transacción
                // Roles: ADMIN, CAJERO, CLIENTE
                .requestMatchers(HttpMethod.GET, "/transaction/{transactionId}").hasAnyRole("ADMIN", "CAJERO", "CLIENTE")

                // POST /transaction/anulation - Anular transacción
                // Roles: ADMIN, SUPERVISOR (solo supervisores pueden anular)
                .requestMatchers(HttpMethod.POST, "/transaction/anulation").hasAnyRole("ADMIN", "SUPERVISOR")

                // GET /transaction/transaccions - Listar todas las transacciones
                // Roles: ADMIN, SUPERVISOR (solo personal administrativo)
                .requestMatchers(HttpMethod.GET, "/transaction/transaccions").hasAnyRole("ADMIN", "SUPERVISOR")

                // --------------------------------------------------------
                // DEFAULT: Cualquier otro endpoint requiere autenticación
                // --------------------------------------------------------
                .anyRequest().authenticated()
            )

            // Permitir frames (para H2 Console)
            .headers(headers -> headers.frameOptions().sameOrigin());

        // ============================================================
        // CONFIGURACIÓN DE AUTENTICACIÓN
        // ============================================================
        http.authenticationProvider(authenticationProvider());

        // Agregar el filtro JWT ANTES del filtro de autenticación de usuario/contraseña
        // Esto permite que el JWT sea validado en cada request
        http.addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (ajustar según tu frontend)
        // * para desarrollo, especificar dominio para producción
        configuration.setAllowedOrigins(List.of("*"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Headers permitidos en las peticiones
        configuration.setAllowedHeaders(List.of("*"));

        // Permitir envío de credenciales (cookies, headers de autorización)
        configuration.setAllowCredentials(false); // Cambiar a true si usas credenciales con orígenes específicos

        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);

        // Headers expuestos al cliente
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
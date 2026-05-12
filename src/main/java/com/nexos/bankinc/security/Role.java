package com.nexos.bankinc.security;

/**
 * Roles definidos en el sistema
 * ===============================
 *
 * Cada rol tiene un prefijo "ROLE_" por convención de Spring Security.
 * Spring Security automáticamente agrega este prefijo cuando usa
 * hasRole() en la configuración.
 *
 * Roles disponibles:
 * - ROLE_ADMIN: Administrador del sistema, acceso completo
 * - ROLE_CAJERO: Empleado de caja, puede manejar transacciones y tarjetas
 * - ROLE_CLIENTE: Cliente del banco, acceso limitado a sus propios recursos
 * - ROLE_SUPERVISOR: Supervisor, puede anular transacciones y ver reportes
 */
public enum Role {
    ROLE_ADMIN,
    ROLE_CAJERO,
    ROLE_CLIENTE,
    ROLE_SUPERVISOR
}
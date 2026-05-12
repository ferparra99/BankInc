package com.nexos.bankinc.security;

import com.nexos.bankinc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de UserDetailsService - Carga usuarios para autenticación
 * =========================================================================
 *
 * Spring Security usa esta interfaz para obtener los datos del usuario
 * durante el proceso de autenticación.
 *
 * ¿Cómo funciona?
 * 1. El AuthenticationManager recibe credenciales (username/password)
 * 2. Busca un UserDetailsService registrado (este)
 * 3. Llama a loadUserByUsername() con el username
 * 4. Obtenemos el usuario de la BD
 * 5. Spring Security compara el password hash con el ingresado
 * 6. Si coincide, la autenticación es exitosa
 *
 * Este patrón permite desacoplar Spring Security de tu modelo de datos.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario por su username
     *
     * @param username El nombre de usuario a buscar
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }
}
package com.nexos.bankinc.config;

import com.nexos.bankinc.entity.User;
import com.nexos.bankinc.repository.UserRepository;
import com.nexos.bankinc.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Crear usuarios de ejemplo
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@bankinc.com")
                    .roles(List.of(Role.ROLE_ADMIN))
                    .enabled(true)
                    .build();

            User cajero = User.builder()
                    .username("cajero")
                    .password(passwordEncoder.encode("cajero123"))
                    .email("cajero@bankinc.com")
                    .roles(List.of(Role.ROLE_CAJERO))
                    .enabled(true)
                    .build();

            User cliente = User.builder()
                    .username("cliente")
                    .password(passwordEncoder.encode("cliente123"))
                    .email("cliente@bankinc.com")
                    .roles(List.of(Role.ROLE_CLIENTE))
                    .enabled(true)
                    .build();

            User supervisor = User.builder()
                    .username("supervisor")
                    .password(passwordEncoder.encode("supervisor123"))
                    .email("supervisor@bankinc.com")
                    .roles(List.of(Role.ROLE_SUPERVISOR))
                    .enabled(true)
                    .build();

            userRepository.saveAll(List.of(admin, cajero, cliente, supervisor));
            System.out.println("Usuarios de ejemplo creados:");
            System.out.println("Admin: admin/admin123");
            System.out.println("Cajero: cajero/cajero123");
            System.out.println("Cliente: cliente/cliente123");
            System.out.println("Supervisor: supervisor/supervisor123");
        }
    }
}

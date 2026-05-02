package com.remar.EN.gestion.config;

import com.remar.EN.gestion.entity.Rol;
import com.remar.EN.gestion.entity.Usuario;
import com.remar.EN.gestion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .email("admin@en.gestion")
                    .password(passwordEncoder.encode("admin1234"))
                    .rol(Rol.ADMIN)
                    .build();
            repository.save(admin);
            log.info("Usuario admin creado: admin@en.gestion / admin1234");
        }
    }
}

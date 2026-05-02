package com.remar.EN.gestion.service;

import com.remar.EN.gestion.dto.LoginRequestDTO;
import com.remar.EN.gestion.dto.LoginResponseDTO;
import com.remar.EN.gestion.dto.RegisterRequestDTO;
import com.remar.EN.gestion.entity.Usuario;
import com.remar.EN.gestion.repository.UsuarioRepository;
import com.remar.EN.gestion.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        Usuario usuario = repository.findByEmail(dto.getEmail())
                .orElseThrow();
        String token = jwtService.generarToken(usuario);
        return new LoginResponseDTO(token, usuario.getEmail(), usuario.getRol().name());
    }

    public LoginResponseDTO registrar(RegisterRequestDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + dto.getEmail());
        }
        Usuario usuario = Usuario.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(dto.getRol())
                .build();
        repository.save(usuario);
        String token = jwtService.generarToken(usuario);
        return new LoginResponseDTO(token, usuario.getEmail(), usuario.getRol().name());
    }
}

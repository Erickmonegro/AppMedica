package com.proyectomedico.appmedicacenfasies.service;

import com.proyectomedico.appmedicacenfasies.model.Usuario;
import com.proyectomedico.appmedicacenfasies.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;

    public Usuario autenticar(String username, String password) {
        return usuarioRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password)) // En el futuro usaremos BCrypt para encriptar
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
    }
}
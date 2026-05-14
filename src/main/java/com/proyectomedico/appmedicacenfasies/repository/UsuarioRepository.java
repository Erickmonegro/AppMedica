package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    // Spring Boot detecta "findBy" y "Username" y crea el SQL: SELECT * FROM usuarios WHERE username = ?
    Optional<Usuario> findByUsername(String username);
}

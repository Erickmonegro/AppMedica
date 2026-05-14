package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED) // Estrategia para la herencia
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    private Rol rol; // Aquí guardaremos si es SECRETARIA o MEDICO
}
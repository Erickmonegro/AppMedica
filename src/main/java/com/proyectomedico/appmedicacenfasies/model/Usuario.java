package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED) // ESTA ES LA MAGIA SENIOR
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario { // Cambiado a singular

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String password;

    // Aquí más adelante agregaremos el Rol (ADMIN, MEDICO, etc.)
}
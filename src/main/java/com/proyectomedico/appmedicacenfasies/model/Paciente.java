package com.proyectomedico.appmedicacenfasies.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que representa a un Paciente en el sistema CENFASIES.
 * Utilizamos JPA para el mapeo y Lombok para eliminar el código repetitivo.
 */
@Entity
@Table(name = "pacientes")
@Data // Genera Getters, Setters, toString, equals y hashCode automáticamente
@NoArgsConstructor // Constructor vacío requerido por JPA
@AllArgsConstructor // Constructor con todos los campos
@Builder // Permite crear objetos de forma fluida: Paciente.builder().nombre("...").build()
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Genera un UUID automático
    @Column(name = "paciente_id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nombreApellidos;

    @Column(unique = true, nullable = false, length = 20)
    private String cedula;

    private LocalDate fechaNacimiento;

    // Campo calculado o informativo según el formulario
    private Integer edad;

    @Column(length = 100)
    private String seguro;

    @Column(length = 100)
    private String ocupacion;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    private String telefonos;

    // Auditoría básica
    @Column(name = "creado_en", updatable = false)
    private java.time.LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = java.time.LocalDateTime.now();
    }
}



/*
Nombre
Apellido
fecha de nacimiento
edad
cedula
telefonos
direccion
ocupacion
lista Hoja Evoluacion
 */
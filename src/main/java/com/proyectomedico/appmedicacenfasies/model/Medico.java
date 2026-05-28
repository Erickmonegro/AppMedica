package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medicos")
@PrimaryKeyJoinColumn(name = "usuario_id") // Se conecta con el ID del Usuario padre
@Data
@EqualsAndHashCode(callSuper = true) // Necesario en Lombok cuando usamos herencia
@NoArgsConstructor
// @AllArgsConstructor (Lo quitamos porque en la herencia es mejor manejar los constructores a mano o con SuperBuilder)
public class Medico extends Usuario { // Extiende de la clase padre

    // El médico hereda el ID, nombre y password de Usuario automáticamente.
    // Aquí solo ponemos cosas EXCLUSIVAS del médico, por ejemplo:

    @ElementCollection(targetClass = Especialidad.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "medico_especialidades", joinColumns = @JoinColumn(name = "medico_id"))
    @Enumerated(EnumType.STRING) // ¡Súper importante para que guarde el texto y no el número del enum!
    @Column(name = "especialidad")
    private Set<Especialidad> especialidades = new HashSet<>();

    private String exequatur; // Número de licencia médica (sugerencia)
}
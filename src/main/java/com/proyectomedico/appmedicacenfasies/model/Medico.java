package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    private Especialidad especialidad; //[cite: 3]

    private String exequatur; // Número de licencia médica (sugerencia)
}
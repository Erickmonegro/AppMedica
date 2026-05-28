package com.proyectomedico.appmedicacenfasies.model.sonografias;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "sonografias_pelvica_fem")
@PrimaryKeyJoinColumn(name = "sonografia_base_id")

public class SonografiaPelvicaFemenina extends SonografiaBase {

    @Column(columnDefinition = "TEXT")
    private String utero;
    private String medidasUtero; // Ej: L:69.50mm AP:37.80mm T:50.67mm

    @Column(columnDefinition = "TEXT")
    private String cervix;

    @Column(columnDefinition = "TEXT")
    private String cavidadUterinaEndometrio;

    @Column(columnDefinition = "TEXT")
    private String ovarioDerecho;

    @Column(columnDefinition = "TEXT")
    private String ovarioIzquierdo;

    @Column(columnDefinition = "TEXT")
    private String fondoSacoDouglas;
}
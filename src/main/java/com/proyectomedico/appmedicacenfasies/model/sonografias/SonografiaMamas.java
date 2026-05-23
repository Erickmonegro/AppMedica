package com.proyectomedico.appmedicacenfasies.model.sonografias;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sonografias_mamas")
@PrimaryKeyJoinColumn(name = "sonografia_base_id")
public class SonografiaMamas extends SonografiaBase {

    @Column(columnDefinition = "TEXT")
    private String superficieCutanea;

    @Column(columnDefinition = "TEXT")
    private String pezonAreola;

    @Column(columnDefinition = "TEXT")
    private String mamaDerecha;

    @Column(columnDefinition = "TEXT")
    private String mamaIzquierda;

    @Column(columnDefinition = "TEXT")
    private String regionesAxilares;

    private String biRads; // Nivel de Bi-rads
}
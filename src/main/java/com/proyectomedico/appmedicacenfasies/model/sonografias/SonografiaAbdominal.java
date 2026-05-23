package com.proyectomedico.appmedicacenfasies.model.sonografias;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sonografias_abdominal")
@PrimaryKeyJoinColumn(name = "sonografia_base_id")
public class SonografiaAbdominal extends SonografiaBase {

    @Column(columnDefinition = "TEXT")
    private String higado;
    private String lhd; // Medida en mm

    @Column(columnDefinition = "TEXT")
    private String conductoColedocoVenaPorta;

    @Column(columnDefinition = "TEXT")
    private String vesiculaBiliar;

    @Column(columnDefinition = "TEXT")
    private String pancreas;

    @Column(columnDefinition = "TEXT")
    private String bazo;

    @Column(columnDefinition = "TEXT")
    private String rinonDerecho;

    @Column(columnDefinition = "TEXT")
    private String rinonIzquierdo;

    @Column(columnDefinition = "TEXT")
    private String intestinos; // Opcional, pero apareció en uno de los reportes
}
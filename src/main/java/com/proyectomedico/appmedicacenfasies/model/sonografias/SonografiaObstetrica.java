package com.proyectomedico.appmedicacenfasies.model.sonografias;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sonografias_obstetrica")
@PrimaryKeyJoinColumn(name = "sonografia_base_id")
public class SonografiaObstetrica extends SonografiaBase {

    @Column(columnDefinition = "TEXT")
    private String fetoResumen;

    private String frecuenciaCardiacaFetal;
    private String extremidades; // Ej: "4 EXTREMIDADES PRESENTES"

    // Biometría Fetal
    private String bpd;
    private String hc;
    private String ac;
    private String fl;
    private String pesoEstimado;

    @Column(columnDefinition = "TEXT")
    private String placenta;

    @Column(columnDefinition = "TEXT")
    private String liquidoAmniotico;

    @Column(columnDefinition = "TEXT")
    private String cordonUmbilical;
}
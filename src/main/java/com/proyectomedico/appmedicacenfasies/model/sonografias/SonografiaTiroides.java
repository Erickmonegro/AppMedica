package com.proyectomedico.appmedicacenfasies.model.sonografias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sonografias_tiroides")
@PrimaryKeyJoinColumn(name = "sonografia_base_id")
public class SonografiaTiroides extends SonografiaBase {

    @Column(columnDefinition = "TEXT")
    private String glandulaTiroides;

    @Column(columnDefinition = "TEXT")
    private String lobuloDerecho;

    @Column(columnDefinition = "TEXT")
    private String lobuloIzquierdo;

    @Column(columnDefinition = "TEXT")
    private String istmo;

    @Column(columnDefinition = "TEXT")
    private String traquea;

    @Column(columnDefinition = "TEXT")
    private String planoMuscular;

    private String tiRads; // Nivel de clasificación (Ej: TIRADS 1)
}
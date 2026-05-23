package com.proyectomedico.appmedicacenfasies.model.sonografias;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sonografias_pelvica_masc")
@PrimaryKeyJoinColumn(name = "sonografia_base_id")
public class SonografiaPelvicaMasculina extends SonografiaBase {

    @Column(columnDefinition = "TEXT")
    private String vejiga;

    private String volumenPremiccion;
    private String volumenPostmicion;

    @Column(columnDefinition = "TEXT")
    private String prostata;
    private String volumenProstatico;
}
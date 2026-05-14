package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "resultados_evolucion")
public class ResultadosHojaEvolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String hb;         // Hemoglobina
    private String htco;       // Hematocrito
    private String plaq;       // Plaquetas
    private String glic;       // Glicemia (Corregido de CLIC)
    private String h1ac;       // Hemoglobina Glicosilada
    private String colest;     // Colesterol Total
    private String hdl;        // Colesterol Bueno
    private String ldl;        // Colesterol Malo
    private String trig;       // Triglicéridos

    @Column(length = 500)      // Más largo por si describe la sonografía
    private String sonografias;

    // Relación Inversa (Una hoja de evolución tiene un resultado)
    @OneToOne(mappedBy = "resultadosLaboratorio")
    private HojaEvolucion hojaEvolucion;
}
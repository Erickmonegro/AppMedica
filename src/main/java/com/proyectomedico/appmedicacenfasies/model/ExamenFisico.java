package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "examenes_fisicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamenFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Signos Vitales
// Dentro de tu modelo ExamenFisico.java
    private Double peso;
    private Double talla;
    private Double imc;
    private String tensionArterial;
    private Double frecuenciaCardiaca; // Cámbialo a Double si lo tenías como String
    private Double frecuenciaRespiratoria; // Cámbialo a Double si lo tenías como String
    private Double temperatura; // ¡Asegúrate de agregar este!
    private Double spO2;
    // ... (Y los demás campos de texto como cabeza, cuello, torax, etc.)
    // Evaluación por sistemas (Usamos TEXT por si el médico detalla mucho)
    @Column(columnDefinition = "TEXT")
    private String cabeza;
    @Column(columnDefinition = "TEXT")
    private String cuello;
    @Column(columnDefinition = "TEXT")
    private String torax;
    @Column(columnDefinition = "TEXT")
    private String corazon;
    @Column(columnDefinition = "TEXT")
    private String pulmones;
    @Column(columnDefinition = "TEXT")
    private String abdomen;
    @Column(columnDefinition = "TEXT")
    private String genitalesExternos;
    @Column(columnDefinition = "TEXT")
    private String miembroSuperior;
    @Column(columnDefinition = "TEXT")
    private String miembroInferior;
    @Column(columnDefinition = "TEXT")
    private String pielYFaneras;
    @Column(columnDefinition = "TEXT")
    private String mamas;
    @Column(columnDefinition = "TEXT")
    private String especuloscopia;
    @Column(columnDefinition = "TEXT")
    private String tactoVaginal;

    // CONEXIÓN AISLADA
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
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
    private String tensionArterial; // TA
    private String frecuenciaCardiaca; // FC
    private String frecuenciaRespiratoria; // FR
    private Double peso;
    private Double talla;
    private Double imc;

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

    // CONEXIÓN AISLADA
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
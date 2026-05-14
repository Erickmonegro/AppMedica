package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "antecedentes_paciente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AntecedentesPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String antecedentesFamiliares;

    @Column(columnDefinition = "TEXT")
    private String antecedentesPersonales;

    @Column(columnDefinition = "TEXT")
    private String transfusion;

    @Column(columnDefinition = "TEXT")
    private String cirugias;

    @Column(columnDefinition = "TEXT")
    private String alergias;

    // CONEXIÓN AISLADA
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
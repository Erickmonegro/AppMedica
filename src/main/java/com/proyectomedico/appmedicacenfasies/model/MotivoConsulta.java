package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "motivo_consulta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotivoConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String motivoConsulta; //[cite: 8]

    @Column(columnDefinition = "TEXT")
    private String historiaEnfermedadActual; //[cite: 8]

    // --- NUEVA CONEXIÓN ---
    // Un paciente puede tener muchos motivos de consulta (uno por cada visita)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
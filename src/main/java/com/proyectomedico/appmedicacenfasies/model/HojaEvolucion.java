package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "hojas_evolucion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HojaEvolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate fecha; // [cite: 5]

    @Column(columnDefinition = "TEXT")
    private String motivoSeguimiento; // [cite: 7]

    @Column(columnDefinition = "TEXT")
    private String historiaEnfermedadActual; // [cite: 9]

    @Column(columnDefinition = "TEXT")
    private String diagnostico; // [cite: 10]

    @Column(columnDefinition = "TEXT")
    private String tratamiento; // [cite: 11]

    @Column(columnDefinition = "TEXT")
    private String plan; // [cite: 12]

    // CONEXIÓN: Muchas hojas pertenecen a UN paciente
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
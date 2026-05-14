package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "turnos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Relación: Muchos turnos pueden pertenecer a un solo Paciente
    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // Relación: Muchos turnos pueden estar asignados a un solo Médico
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medicoAsignado;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDateTime fechaEntrada;

    // Estados: "EN_ESPERA", "EN_CONSULTA", "FINALIZADO"
    @Column(nullable = false)
    private String estado;

    @Column(name = "area_destino")
    private String areaDestino; // Ej: "Sonografía", "Pediatría", "Medicina General"

    // El sistema registrará esto automáticamente antes de guardar
    @PrePersist
    protected void onCreate() {
        if (this.fechaEntrada == null) {
            this.fechaEntrada = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = "EN_ESPERA";
        }
    }
}
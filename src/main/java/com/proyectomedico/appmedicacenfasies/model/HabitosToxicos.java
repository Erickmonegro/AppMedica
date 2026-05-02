package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "habitos_toxicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class HabitosToxicos {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private boolean alcohol;
    private boolean cafe;
    private boolean tabaco;
    private boolean cigarroElectronico;
    private boolean hooka;
    private boolean drogas;


    // CONEXIÓN AISLADA
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;


}

package com.proyectomedico.appmedicacenfasies.model.sonografias;

import com.proyectomedico.appmedicacenfasies.model.Paciente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "sonografias_base")
@Inheritance(strategy = InheritanceType.JOINED)
public class SonografiaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "medico_realizador", nullable = false)
    private String medicoRealizador;

    @Column(name = "tipo_sonografia", nullable = false)
    private String tipoSonografia; // Ej: "ABDOMINAL", "OBSTETRICA", "MAMAS"

    // Esto va al final del reporte
    @Column(name = "diagnostico_conclusion", columnDefinition = "TEXT")
    private String diagnosticoConclusion;

    @Column(name = "ruta_pdf")
    private String rutaPdf;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() { this.fechaCreacion = LocalDateTime.now(); }
}

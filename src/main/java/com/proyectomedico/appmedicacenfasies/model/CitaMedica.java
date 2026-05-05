package com.proyectomedico.appmedicacenfasies.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "citas_medicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Nos permite crear objetos CitaMedica de forma muy limpia en el Service
public class CitaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cita_id", updatable = false, nullable = false)
    private UUID id;

    // RELACIÓN: Muchas citas pertenecen a UN paciente.
    // Usamos LAZY para no saturar la RAM descargando la historia clínica cuando solo queremos ver la agenda.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // Separamos fecha y hora para que los filtros de calendario en Angular sean súper rápidos
    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    // Semilla para el Multi-tenant: Por ahora es un texto, en el futuro será una relación a la tabla Medicos
    @Column(name = "medico_asignado")
    private String medicoAsignado;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String motivo;

    // Le decimos a Spring que guarde el Enum como Texto ("PROGRAMADA") y no como un número (0, 1, 2)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    // Auditoría automática: Spring Boot llenará esto solo. Vital para temas legales.
    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}
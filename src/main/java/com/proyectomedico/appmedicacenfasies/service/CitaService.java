package com.proyectomedico.appmedicacenfasies.service;

import com.proyectomedico.appmedicacenfasies.dto.CitaRegistroDTO;
import com.proyectomedico.appmedicacenfasies.model.CitaMedica;
import com.proyectomedico.appmedicacenfasies.model.EstadoCita;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import com.proyectomedico.appmedicacenfasies.repository.CitaMedicaRepository;
import com.proyectomedico.appmedicacenfasies.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaMedicaRepository citaMedicaRepository;
    private final PacienteRepository pacienteRepository;

    // ==========================================
    // 1. LECTURA (Para el Dashboard)
    // ==========================================

    // Práctica Senior: readOnly = true le dice a Hibernate que no prepare el motor de guardado.
    // Esto hace que la consulta sea muchísimo más rápida y consuma menos RAM.
    // ==========================================
    // 1. LECTURA (Para el Dashboard)
    // ==========================================

    @Transactional(readOnly = true)
    public List<com.proyectomedico.appmedicacenfasies.dto.CitaDashboardDTO> obtenerCitasParaDashboard() {
        log.info("Extrayendo citas programadas para el widget del Dashboard...");

        LocalDate hoy = LocalDate.now();
        List<CitaMedica> citas = citaMedicaRepository.findByFechaGreaterThanEqualOrderByFechaAscHoraAsc(hoy);

        // Aquí es donde ocurría el error. Debemos mapear (transformar) la Entidad CitaMedica
        // estrictamente a un 'CitaDashboardDTO', NO a un 'CitaRegistroDTO'.
        return citas.stream()
                .map(cita -> new com.proyectomedico.appmedicacenfasies.dto.CitaDashboardDTO(
                        cita.getPaciente().getNombreApellidos(),
                        cita.getFecha(),
                        cita.getHora(),
                        cita.getMedicoAsignado(),
                        cita.getMotivo()
                ))
                .toList();
    }

    // ==========================================
    // 2. ESCRITURA (Agendar nueva cita)
    // ==========================================

    @Transactional
    public void programarNuevaCita(UUID pacienteId, CitaRegistroDTO dto) {
        log.info("Iniciando validaciones para programar nueva cita para el paciente ID: {}", pacienteId);

        // REGLA 1: Fail-Fast - Validar que la fecha no sea en el pasado
        LocalDateTime momentoCita = LocalDateTime.of(dto.fecha(), dto.hora());
        if (momentoCita.isBefore(LocalDateTime.now())) {
            log.warn("Intento de agendar cita en el pasado: {}", momentoCita);
            throw new IllegalArgumentException("No se puede programar una cita en una fecha u hora que ya pasó.");
        }

        // REGLA 2: Fail-Fast - Regla Anti-Colisión
        boolean espacioOcupado = citaMedicaRepository.existsByFechaAndHoraAndMedicoAsignadoAndEstadoNot(
                dto.fecha(),
                dto.hora(),
                dto.medicoAsignado(),
                EstadoCita.CANCELADA // Excluimos las canceladas, porque esos espacios están libres
        );

        if (espacioOcupado) {
            log.warn("Colisión detectada: El Dr/Dra. {} ya tiene cita el {} a las {}", dto.medicoAsignado(), dto.fecha(), dto.hora());
            throw new IllegalStateException("El médico ya tiene una cita asignada en ese horario. Por favor, seleccione otro espacio.");
        }

        // REGLA 3: Integridad Referencial - Buscar al paciente
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Error crítico: El paciente seleccionado no existe en la base de datos."));

        // Si pasamos las defensas, ensamblamos y guardamos usando el Patrón Builder
        CitaMedica nuevaCita = CitaMedica.builder()
                .paciente(paciente)
                .fecha(dto.fecha())
                .hora(dto.hora())
                .medicoAsignado(dto.medicoAsignado())
                .motivo(dto.motivo())
                .estado(EstadoCita.PROGRAMADA) // Toda cita nueva nace como PROGRAMADA
                .build();

        citaMedicaRepository.save(nuevaCita);
        log.info("Cita médica programada exitosamente con el ID: {}", nuevaCita.getId());
    }
}
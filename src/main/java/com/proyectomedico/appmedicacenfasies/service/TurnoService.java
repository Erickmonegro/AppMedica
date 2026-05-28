package com.proyectomedico.appmedicacenfasies.service;

import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import com.proyectomedico.appmedicacenfasies.model.Turno;
import com.proyectomedico.appmedicacenfasies.repository.MedicoRepository;
import com.proyectomedico.appmedicacenfasies.repository.PacienteRepository;
import com.proyectomedico.appmedicacenfasies.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository; // Asumo que tienes este repositorio

    /**
     * Módulo Secretaria: Registra la llegada de un paciente y lo pone en la sala de espera de un médico.
     */
    @Transactional
    public Turno crearTurnoParaPaciente(UUID pacienteId, UUID medicoId, String areaDestino, String tipoEstudio) {
        log.info("Creando nuevo turno para paciente ID: {} con Médico ID: {}", pacienteId, medicoId);

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        Turno nuevoTurno = new Turno();
        nuevoTurno.setPaciente(paciente);
        nuevoTurno.setMedicoAsignado(medico);

        // Asignaciones del enrutamiento
        nuevoTurno.setAreaDestino(areaDestino);
        nuevoTurno.setTipoEstudio(tipoEstudio); // <--- ¡LA MAGIA OCURRE AQUÍ!

        // Nota: La fechaEntrada y el estado "EN_ESPERA" se ponen solos gracias al @PrePersist en la Entidad.

        return turnoRepository.save(nuevoTurno);
    }
}
package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, UUID> {

    // 1. Para la Secretaria: Ver TODOS los pacientes que están esperando en la clínica hoy
    List<Turno> findByEstadoOrderByFechaEntradaAsc(String estado);

    // 2. Para el Médico: Ver SUS pacientes asignados que están esperando
    List<Turno> findByMedicoAsignadoIdAndEstadoOrderByFechaEntradaAsc(UUID medicoId, String estado);

    // 3. Para ver el historial de un paciente específico (Trazabilidad)
    List<Turno> findByPacienteIdOrderByFechaEntradaDesc(UUID pacienteId);
    // NUEVA: Para la Secretaria (Filtra solo por estado para ver a TODOS los que esperan)

}
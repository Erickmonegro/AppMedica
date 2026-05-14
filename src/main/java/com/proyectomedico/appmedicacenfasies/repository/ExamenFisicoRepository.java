package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.ExamenFisico;

import com.proyectomedico.appmedicacenfasies.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExamenFisicoRepository extends JpaRepository<ExamenFisico, UUID> {

    Optional<ExamenFisico> findByPacienteId(UUID pacienteId);
    Optional<ExamenFisico> findByPaciente(Paciente paciente);


}

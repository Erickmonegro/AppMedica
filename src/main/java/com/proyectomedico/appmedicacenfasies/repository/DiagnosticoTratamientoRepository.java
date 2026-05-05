package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.DiagnosticoTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiagnosticoTratamientoRepository extends JpaRepository<DiagnosticoTratamiento, UUID> {
    Optional<DiagnosticoTratamiento> findByPacienteId(UUID pacienteId);
}
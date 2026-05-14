package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.DiagnosticoTratamiento;
import com.proyectomedico.appmedicacenfasies.model.MotivoConsulta;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MotivoConsultaRepository extends JpaRepository<MotivoConsulta, UUID> {
    Optional<MotivoConsulta> findByPacienteId(UUID pacienteId);
    Optional<MotivoConsulta> findByPaciente(Paciente paciente);

}

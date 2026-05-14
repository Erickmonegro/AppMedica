package com.proyectomedico.appmedicacenfasies.repository;


import com.proyectomedico.appmedicacenfasies.model.HabitosToxicos;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface HabitosToxicosRepository extends JpaRepository<HabitosToxicos, UUID> {

    Optional<HabitosToxicos> findByPacienteId(UUID pacienteId);
    Optional<HabitosToxicos> findByPaciente(Paciente paciente);
}



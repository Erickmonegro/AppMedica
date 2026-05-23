package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.NotaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotaMedicaRepository extends JpaRepository<NotaMedica, UUID> {
    List<NotaMedica> findByPacienteIdOrderByFechaCreacionDesc(UUID pacienteId);
}
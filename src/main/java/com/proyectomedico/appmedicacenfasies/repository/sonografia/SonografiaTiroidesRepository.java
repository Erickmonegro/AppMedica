package com.proyectomedico.appmedicacenfasies.repository.sonografia;

import com.proyectomedico.appmedicacenfasies.model.sonografias.SonografiaTiroides;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SonografiaTiroidesRepository extends JpaRepository<SonografiaTiroides, UUID> {

    // Método para recuperar el historial específico si lo necesitamos después
    List<SonografiaTiroides> findByPacienteIdOrderByFechaCreacionDesc(UUID pacienteId);
}
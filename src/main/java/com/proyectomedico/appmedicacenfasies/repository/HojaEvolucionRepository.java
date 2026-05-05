package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.HojaEvolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HojaEvolucionRepository extends JpaRepository<HojaEvolucion, UUID> {

    // Magia de Spring: Busca por el ID del paciente y ordena por fecha descendente (lo más nuevo arriba)
    List<HojaEvolucion> findByPacienteIdOrderByFechaDesc(UUID pacienteId);
}
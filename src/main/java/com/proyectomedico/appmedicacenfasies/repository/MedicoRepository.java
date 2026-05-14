package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.Especialidad;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MedicoRepository extends JpaRepository<Medico, UUID> {

    List<Medico> findByEspecialidad(Especialidad especialidad);
}

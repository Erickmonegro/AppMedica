package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.AntecedentesPaciente;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AntecedentesPacienteRepository extends JpaRepository<AntecedentesPaciente, UUID> {

}

package com.proyectomedico.appmedicacenfasies.repository.sonografia;

import com.proyectomedico.appmedicacenfasies.model.sonografias.SonografiaBase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SonografiaBaseRepository extends JpaRepository<SonografiaBase, UUID> {
    // Este método nos servirá para llenar la nueva pestaña del Visor de Expediente
    List<SonografiaBase> findByPacienteIdOrderByFechaCreacionDesc(UUID pacienteId);
}
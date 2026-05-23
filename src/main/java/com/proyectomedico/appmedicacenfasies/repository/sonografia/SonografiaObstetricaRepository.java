package com.proyectomedico.appmedicacenfasies.repository.sonografia;

import com.proyectomedico.appmedicacenfasies.model.sonografias.SonografiaObstetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SonografiaObstetricaRepository extends JpaRepository<SonografiaObstetrica, UUID> {
}
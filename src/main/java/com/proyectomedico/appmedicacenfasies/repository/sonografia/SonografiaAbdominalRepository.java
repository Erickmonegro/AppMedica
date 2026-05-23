package com.proyectomedico.appmedicacenfasies.repository.sonografia;

import com.proyectomedico.appmedicacenfasies.model.sonografias.SonografiaAbdominal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SonografiaAbdominalRepository extends JpaRepository<SonografiaAbdominal, UUID> {
}
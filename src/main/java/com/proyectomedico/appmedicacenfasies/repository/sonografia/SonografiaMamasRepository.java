package com.proyectomedico.appmedicacenfasies.repository.sonografia;

import com.proyectomedico.appmedicacenfasies.model.sonografias.SonografiaMamas;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SonografiaMamasRepository extends JpaRepository<SonografiaMamas, UUID> {
}

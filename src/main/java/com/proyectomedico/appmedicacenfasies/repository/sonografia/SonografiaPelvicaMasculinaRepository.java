package com.proyectomedico.appmedicacenfasies.repository.sonografia;

import com.proyectomedico.appmedicacenfasies.model.sonografias.SonografiaPelvicaMasculina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SonografiaPelvicaMasculinaRepository extends JpaRepository<SonografiaPelvicaMasculina, UUID> {
}

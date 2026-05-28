package com.proyectomedico.appmedicacenfasies.repository.sonografia;

import com.proyectomedico.appmedicacenfasies.model.sonografias.SonografiaPelvicaFemenina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SonografiaPelvicaFemeninaRepository extends JpaRepository<SonografiaPelvicaFemenina, UUID> {
}

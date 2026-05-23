package com.proyectomedico.appmedicacenfasies.repository.sonografia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SonografiaPelvicaMasculina extends JpaRepository<SonografiaPelvicaFemenina, UUID> {
}

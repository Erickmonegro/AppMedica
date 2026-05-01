package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.ExamenFisico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamenFisicoRepository extends JpaRepository<ExamenFisico, UUID> {

}

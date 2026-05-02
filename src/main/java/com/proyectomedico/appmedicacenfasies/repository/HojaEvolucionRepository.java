package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.HojaEvolucion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HojaEvolucionRepository extends JpaRepository <HojaEvolucion, UUID> {
}

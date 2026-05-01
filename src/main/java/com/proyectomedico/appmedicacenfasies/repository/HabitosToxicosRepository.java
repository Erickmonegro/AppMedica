package com.proyectomedico.appmedicacenfasies.repository;


import com.proyectomedico.appmedicacenfasies.model.HabitosToxicos;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface HabitosToxicosRepository extends JpaRepository<HabitosToxicos, UUID> {


}

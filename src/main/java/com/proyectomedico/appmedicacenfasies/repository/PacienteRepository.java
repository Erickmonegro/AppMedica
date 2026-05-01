package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    // Spring Boot construye la consulta SQL automáticamente solo con leer el nombre del método:
    // SELECT * FROM pacientes WHERE cedula = ?
    Optional<Paciente> findByCedula(String cedula);

    // Ejemplo: Útil para buscar si un paciente ya está registrado
    boolean existsByCedula(String cedula);

  List<Paciente> findByNombreApellidosContainingIgnoreCaseOrCedulaContaining(String nombre, String cedula);
}
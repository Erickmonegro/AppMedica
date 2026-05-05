package com.proyectomedico.appmedicacenfasies.repository;

import com.proyectomedico.appmedicacenfasies.model.CitaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CitaMedicaRepository extends JpaRepository<CitaMedica, UUID> {

    /**
     * Magia de Spring Data JPA: Solo con el nombre del método, Spring crea el SQL por debajo.
     * Esto busca: "Citas cuya Fecha sea Mayor o Igual a [X], ordenadas primero por Fecha y luego por Hora".
     * Es exactamente lo que necesita nuestro Widget del Dashboard.
     */
    List<CitaMedica> findByFechaGreaterThanEqualOrderByFechaAscHoraAsc(LocalDate fecha);
    // VERIFICACIÓN DE COLISIÓN: ¿Existe alguna cita en esta fecha, a esta hora, con este médico, que NO esté cancelada?
    boolean existsByFechaAndHoraAndMedicoAsignadoAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            String medicoAsignado,
            com.proyectomedico.appmedicacenfasies.model.EstadoCita estado
    );
}
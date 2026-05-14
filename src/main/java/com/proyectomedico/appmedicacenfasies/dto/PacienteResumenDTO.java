package com.proyectomedico.appmedicacenfasies.dto;

import java.util.UUID;

public record PacienteResumenDTO(UUID id, String nombreApellidos, String cedula, String ultimaVisita) {

    // =========================================================================
    // MAGIA SENIOR: Constructor Secundario (Fallback)
    // Si el @Query de la base de datos solo manda 3 parámetros, Java usará este
    // método y rellenará la "última visita" automáticamente para que no explote.
    // =========================================================================
    public PacienteResumenDTO(UUID id, String nombreApellidos, String cedula) {
        this(id, nombreApellidos, cedula, "No registrada");
    }

}
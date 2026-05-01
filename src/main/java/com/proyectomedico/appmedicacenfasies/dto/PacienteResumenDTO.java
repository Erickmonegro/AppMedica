package com.proyectomedico.appmedicacenfasies.dto;

import java.util.UUID;

public record PacienteResumenDTO(
        UUID id,
        String nombreApellidos,
        String cedula
) {
}

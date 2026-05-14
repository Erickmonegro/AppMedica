package com.proyectomedico.appmedicacenfasies.dto;

import com.proyectomedico.appmedicacenfasies.model.Especialidad;
import java.util.UUID;

public record PacienteRapidoDTO(
        String cedula,
        String nombreApellidos,
        String telefono,
        String seguro,
        Especialidad especialidadRequerida, // Usamos tu Enum
        UUID medicoAsignadoId // El ID del médico seleccionado
) {}
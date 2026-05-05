package com.proyectomedico.appmedicacenfasies.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO optimizado exclusivamente para el Widget del Dashboard.
 * Al ser un 'record', Java crea automáticamente los métodos nombrePaciente(), fecha(), etc.
 */
public record CitaDashboardDTO(
        String nombrePaciente,
        LocalDate fecha,
        LocalTime hora,
        String medicoAsignado,
        String motivo
) {
}
package com.proyectomedico.appmedicacenfasies.dto;

/**
 * DTO para transportar el motivo principal por el que el paciente visita la clínica.
 */
public record HistoriaEnfermedadDTO(
        String motivoConsulta,
        String historiaEnfermedadActual
) {}
package com.proyectomedico.appmedicacenfasies.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRegistroDTO(
        LocalDate fecha,
        LocalTime hora,
        String medicoAsignado,
        String motivo
) {
}
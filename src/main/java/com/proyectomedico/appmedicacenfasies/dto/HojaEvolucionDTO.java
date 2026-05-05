package com.proyectomedico.appmedicacenfasies.dto;

import java.time.LocalDate;
import java.util.UUID;

public record HojaEvolucionDTO(
        UUID id,
        LocalDate fecha,
        String motivoSeguimiento,
        String historiaEnfermedadActual,
        String diagnostico,
        String tratamiento,
        String plan,
        String rutaPdf
) {
}
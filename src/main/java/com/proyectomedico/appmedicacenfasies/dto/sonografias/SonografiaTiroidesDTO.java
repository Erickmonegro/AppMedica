package com.proyectomedico.appmedicacenfasies.dto.sonografias;

public record SonografiaTiroidesDTO(
        String medicoRealizador,
        String diagnosticoConclusion,

        // Campos extraídos del reporte físico
        String glandulaTiroides,
        String lobuloDerecho,
        String lobuloIzquierdo,
        String istmo,
        String traquea,
        String planoMuscular,
        String tiRads // Para almacenar la clasificación (Ej: TIRADS 1)
) {
}
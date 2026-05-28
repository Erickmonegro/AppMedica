package com.proyectomedico.appmedicacenfasies.dto.sonografias;

public record SonografiaMamasDTO(
        String medicoRealizador,
        String diagnosticoConclusion,

        String superficieCutanea,
        String pezonAreola,
        String mamaDerecha,
        String mamaIzquierda,
        String regionesAuxiliares,
        String biRands
) {
}

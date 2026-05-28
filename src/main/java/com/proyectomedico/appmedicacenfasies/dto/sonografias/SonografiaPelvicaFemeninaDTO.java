package com.proyectomedico.appmedicacenfasies.dto.sonografias;

public record SonografiaPelvicaFemeninaDTO(
        String medicoRealizador,
        String diagnosticoConclusion,
        String utero,
        String medidaUteros,
        String cervix,
        String cavidadUterinaEndometrio,
        String ovarioDerecho,
        String ovarioIzquierdo,
        String fondoSacoDouglas
) {
}

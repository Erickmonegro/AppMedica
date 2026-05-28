package com.proyectomedico.appmedicacenfasies.dto.sonografias;

public record SonografiaPelvicaMasculinaDTO(
        String medicoRealizador,
        String diagnosticoConclusion,
        String vejiga,
        String volumenPremiccion,
        String volumenPostmicion,
        String prostata,
        String volumenProstatico
) {
}

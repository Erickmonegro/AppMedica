package com.proyectomedico.appmedicacenfasies.dto.sonografias;

public record SonografiaAbdominalDTO (
        // Datos Base
        String medicoRealizador,
        String diagnosticoConclusion,

        // Datos Específicos
        String higado,
        String lhd,
        String conductoColedocoVenaPorta,
        String vesiculaBiliar,
        String pancreas,
        String bazo,
        String rinonDerecho,
        String rinonIzquierdo,
        String intestinos
){
}

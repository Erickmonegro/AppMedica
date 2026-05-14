package com.proyectomedico.appmedicacenfasies.dto;

public record AntecedentesDTO(
        String antecedentesFamiliares,
        String antecedentesPersonales,
        String transfusiones,
        String cirugias,
        String alergias
) {}
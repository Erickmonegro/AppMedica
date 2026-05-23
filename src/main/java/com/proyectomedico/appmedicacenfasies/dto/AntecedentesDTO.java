package com.proyectomedico.appmedicacenfasies.dto;

import java.time.LocalDate;

public record AntecedentesDTO(
        String antecedentesFamiliares,
        String antecedentesPersonales,
        String transfusiones,
        String cirugias,
        String alergias,
        String menarquia,
        java.time.LocalDate fum,
        String gpca
) {}
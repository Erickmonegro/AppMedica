package com.proyectomedico.appmedicacenfasies.dto;

import java.time.LocalDate;

// 1. DTOs Secundarios (Módulos reutilizables)
// 2. EL DTO MAESTRO (El que recibe el Controlador desde JavaFX)
public record PacienteRegistroDTO(
        DatosPersonalesDTO datosPersonales,
        AntecedentesDTO antecedentes,
        HabitosToxicosDTO habitos,
        ExamenFisicoDTO examenFisico
) {}

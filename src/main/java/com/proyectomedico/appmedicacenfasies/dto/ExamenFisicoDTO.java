package com.proyectomedico.appmedicacenfasies.dto;

public record ExamenFisicoDTO(
        Double peso,
        Double talla,
        String tensionArterial,
        Double frecuenciaCardiaca,
        Double frecuenciaRespiratoria,
        Double temperatura,
        String cabeza,
        String cuello,
        String torax,
        String corazon,
        String pulmones,
        String abdomen,
        String genitalesExternos,
        String miembroSuperior,
        String miembroInferior,
        String pielYFaneras,
        String hallazgosExamenFisico,
        String estudioComplementarios,
        String diagnostico,
        String tratamiento
) {}
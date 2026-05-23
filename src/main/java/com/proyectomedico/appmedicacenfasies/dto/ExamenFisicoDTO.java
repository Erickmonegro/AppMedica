package com.proyectomedico.appmedicacenfasies.dto;

public record ExamenFisicoDTO(
        Double peso,
        Double talla,
        String tensionArterial,
        Double frecuenciaCardiaca,
        Double frecuenciaRespiratoria,
        Double temperatura,
        Double spO2,
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
        String mamas,            // <--- NUEVO
        String especuloscopia,   // <--- NUEVO
        String tactoVaginal,     // <--- NUEVO
        String estudioComplementarios,
        String diagnostico,
        String tratamiento
) {}
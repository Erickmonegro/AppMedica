package com.proyectomedico.appmedicacenfasies.dto;

import java.time.LocalDate;

public record PacienteRegistroDTO (

        // --- DATOS PERSONALES ---
        String nombreApellidos,
        String cedula,
        LocalDate fechaNacimiento,
        String direccion,
        String telefonos,
        String seguro,
        String ocupacion,

        // --- ANTECEDENTES (Heredo-familiares y Personales) ---
        String antecedentesFamiliares,
        String antecedentesPersonales,
        String personalesNoPatologicos,
        String transfucion,
        String cirugias,
        String alergias,

        // --- HÁBITOS TÓXICOS ---
        boolean fuma,      // Frecuencia/Cantidad
        boolean alcohol,
        boolean hooka,
        boolean cigarrilloElectronico,
        boolean cafe,
        boolean drogas,

        // --- EXAMEN FÍSICO INICIAL ---
        Double peso,
        Double talla,
        String tensionArterial,
        Double frecuenciaCardiaca,
        Double frecuenciaRespiratoria,
        Double temperatura,
        String hallazgosExamenFisico // Comentarios generales del médico
) {}


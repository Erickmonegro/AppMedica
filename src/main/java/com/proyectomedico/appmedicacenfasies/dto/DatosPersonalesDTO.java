package com.proyectomedico.appmedicacenfasies.dto;

import java.time.LocalDate;

public record DatosPersonalesDTO(
        String nombreApellidos,
        String cedula,
        LocalDate fechaNacimiento,
        String sexo,
        String tipoSangre,
        String estadoCivil,
        String direccion,
        String telefonos,
        String contactoEmergencia,
        String seguro,
        String ocupacion
) {}
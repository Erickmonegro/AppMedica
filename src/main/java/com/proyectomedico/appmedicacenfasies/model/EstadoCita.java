package com.proyectomedico.appmedicacenfasies.model;

/**
 * Define el ciclo de vida estricto de una cita médica.
 */
public enum EstadoCita {
    PROGRAMADA, // El paciente tiene la cita agendada para el futuro
    COMPLETADA, // El médico ya atendió al paciente
    CANCELADA,  // El paciente canceló o no asistió
    EN_SALA     // El paciente ya llegó y está en sala de espera (Pensando a futuro)
}
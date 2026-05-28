package com.proyectomedico.appmedicacenfasies.dto.sonografias;

public record SonografiaObstetricaDTO(
        String medicoRealizador,
        String diagnosticoConclusion,
        String fetoResumen,
        String frecuenciaCardiacaFetal,
        String extremidades,
        String bpd,
        String hc,
        String ac,
        String fl,
        String pesoEstimado,
        String placenta,
        String liquidoAmniotico,
        String cordonUmbilical
) {
}

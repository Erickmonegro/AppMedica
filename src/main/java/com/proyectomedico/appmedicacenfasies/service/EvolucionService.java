package com.proyectomedico.appmedicacenfasies.service;

import com.proyectomedico.appmedicacenfasies.model.HojaEvolucion;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import com.proyectomedico.appmedicacenfasies.repository.HojaEvolucionRepository;
import com.proyectomedico.appmedicacenfasies.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvolucionService {

    private final PacienteRepository pacienteRepository;
    private final HojaEvolucionRepository hojaEvolucionRepository;

    /**
     * Agrega una nueva hoja de evolución a un paciente existente buscando por su cédula.
     */
    @Transactional
    public HojaEvolucion agregarEvolucionPorCedula(String cedula, HojaEvolucion nuevaEvolucion) {
        log.info("Buscando paciente con cédula {} para agregar nueva evolución...", cedula);

        // 1. Buscar al paciente (Si no existe, lanzamos un error claro)
        Paciente paciente = pacienteRepository.findByCedula(cedula)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró ningún paciente con la cédula " + cedula));

        // 2. Configurar datos automáticos de la evolución
        if (nuevaEvolucion.getFecha() == null) {
            nuevaEvolucion.setFecha(LocalDate.now()); // Si el médico no pone fecha, usamos la de hoy
        }

        // 3. Vincular la evolución al paciente (La Llave Foránea)
        nuevaEvolucion.setPaciente(paciente);

        // 4. Guardar en la base de datos
        HojaEvolucion evolucionGuardada = hojaEvolucionRepository.save(nuevaEvolucion);
        log.info("✅ Evolución agregada exitosamente al paciente {}. ID Evolución: {}",
                paciente.getNombreApellidos(), evolucionGuardada.getId());

        return evolucionGuardada;
    }
}
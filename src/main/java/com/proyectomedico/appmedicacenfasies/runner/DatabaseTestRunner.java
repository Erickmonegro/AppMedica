package com.proyectomedico.appmedicacenfasies.runner;

import com.proyectomedico.appmedicacenfasies.model.*;
import com.proyectomedico.appmedicacenfasies.service.EvolucionService;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component // Le dice a Spring que gestione esta clase y la ejecute al arrancar
@RequiredArgsConstructor
public class DatabaseTestRunner implements CommandLineRunner {

    private final PacienteService pacienteService;
    private final EvolucionService evolucionService;

    @Override
    public void run(String... args) throws Exception {
        log.info("=======================================================");
        log.info("🚀 INICIANDO PRUEBA DE MOTOR DE BASE DE DATOS (FASE 1) 🚀");
        log.info("=======================================================");

        try {
            // 1. Crear Paciente de prueba (Usando formato de cédula dominicana)
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setNombreApellidos("Erick Rodriguez");
            nuevoPaciente.setCedula("402-1234567-5");
            nuevoPaciente.setFechaNacimiento(LocalDate.of(1990, 5, 15));
            nuevoPaciente.setSeguro("Senasa");

            // 2. Crear Examen Físico (Pondremos peso y talla para probar el cálculo de IMC)
            ExamenFisico examen = new ExamenFisico();
            examen.setPeso(85.5); // kg
            examen.setTalla(1.75); // metros
            examen.setTensionArterial("120/80");
            examen.setFrecuenciaCardiaca("72 lpm");

            // 3. Crear Hábitos Tóxicos
            HabitosToxicos habitos = new HabitosToxicos();
            habitos.setCafe(true);
            habitos.setAlcohol(false);
            habitos.setTabaco(false);
            habitos.setHooka(true);
            habitos.setDrogas(false);
            habitos.setCigarroElectronico(true);

            // 4. Crear Antecedentes
            AntecedentesPaciente antecedentes = new AntecedentesPaciente();
            antecedentes.setAlergias("Penicilina");
            antecedentes.setCirugias("Apendicectomía en 2015");

            // 5. ¡Ejecutar el Servicio!
            log.info("Enviando datos al PacienteService...");
            Paciente pacienteGuardado = pacienteService.registrarNuevoPaciente(nuevoPaciente, examen, habitos, antecedentes);

            log.info("✅ PRUEBA EXITOSA: Paciente guardado con UUID: {}", pacienteGuardado.getId());

        } catch (Exception e) {
            log.error("❌ ERROR DURANTE LA PRUEBA: {}", e.getMessage());
        }

        // Arriba, en la declaración de variables (Inyección):
// private final EvolucionService evolucionService;

// ... (después de guardar al paciente en el try-catch) ...

        log.info("--- SIMULANDO SEGUNDA VISITA (EVOLUCIÓN) ---");

        HojaEvolucion primeraEvolucion = new HojaEvolucion();
        primeraEvolucion.setMotivoSeguimiento("Dolor de cabeza recurrente");
        primeraEvolucion.setHistoriaEnfermedadActual("Paciente refiere cefalea de 3 días de evolución...");
        primeraEvolucion.setDiagnostico("Migraña tensional");
        primeraEvolucion.setTratamiento("Ibuprofeno 400mg cada 8 horas");
        primeraEvolucion.setPlan("Reposo, control en 15 días. Beber mucha agua.");

        // Usamos la cédula que le pusimos a Juan Pérez en el paso anterior
        evolucionService.agregarEvolucionPorCedula("402-1234567-5", primeraEvolucion);

        log.info("✅ PRUEBA DE EVOLUCIÓN EXITOSA.");

        log.info("=======================================================");
    }
}
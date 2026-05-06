package com.proyectomedico.appmedicacenfasies.service;

import com.proyectomedico.appmedicacenfasies.dto.*;
import com.proyectomedico.appmedicacenfasies.model.*;
import com.proyectomedico.appmedicacenfasies.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ExamenFisicoRepository examenFisicoRepository;
    private final HabitosToxicosRepository habitosToxicosRepository;
    private final AntecedentesPacienteRepository antecedentesRepository;
    private final HojaEvolucionRepository hojaEvolucionRepository;
    private final MotivoConsultaRepository motivoConsultaRepository;
    // INYECTAMOS TU NUEVA TABLA
    private final DiagnosticoTratamientoRepository diagnosticoTratamientoRepository;
    private final PdfService pdfService;

    // ==========================================
    // 1. ESCRITURA (Guardar en Base de Datos)
    // ==========================================

    @Transactional
    public String registrarNuevaHistoriaClinica(PacienteRegistroDTO dto) {
        log.info("Iniciando transacción segura para el paciente con cédula: {}", dto.datosPersonales().cedula());

        if (pacienteRepository.existsByCedula(dto.datosPersonales().cedula())) {
            throw new RuntimeException("Ya existe un paciente con la cédula: " + dto.datosPersonales().cedula());
        }

        // 1. PACIENTE PRINCIPAL
        Paciente paciente = new Paciente();
        paciente.setNombreApellidos(dto.datosPersonales().nombreApellidos());
        paciente.setCedula(dto.datosPersonales().cedula());
        paciente.setFechaNacimiento(dto.datosPersonales().fechaNacimiento());
        paciente.setDireccion(dto.datosPersonales().direccion());
        paciente.setTelefonos(dto.datosPersonales().telefonos());
        paciente.setSeguro(dto.datosPersonales().seguro());
        paciente.setOcupacion(dto.datosPersonales().ocupacion());
        paciente = pacienteRepository.save(paciente);

        // 2. ANTECEDENTES
        AntecedentesPaciente antecedentes = new AntecedentesPaciente();
        antecedentes.setPaciente(paciente);
        antecedentes.setAntecedentesFamiliares(dto.antecedentes().antecedentesFamiliares());
        antecedentes.setAntecedentesPersonales(dto.antecedentes().antecedentesPersonales());
        antecedentes.setAlergias(dto.antecedentes().alergias());
        antecedentes.setCirugias(dto.antecedentes().cirugias());
        antecedentes.setTransfusion(dto.antecedentes().transfusiones());
        antecedentes.setAntecedentesPersonalesNoPatologicos(dto.antecedentes().personalesNoPatologicos());
        antecedentesRepository.save(antecedentes);

        // 3. HÁBITOS TÓXICOS (Mapeando a tus variables exactas)
        HabitosToxicos habitos = new HabitosToxicos();
        habitos.setPaciente(paciente);
        habitos.setTabaco(dto.habitos().tabaco()); // UI manda 'fuma', BD recibe 'tabaco'
        habitos.setAlcohol(dto.habitos().alcohol());
        habitos.setCafe(dto.habitos().cafe());
        habitos.setDrogas(dto.habitos().drogas());
        habitos.setHooka(dto.habitos().hooka());
        habitos.setCigarroElectronico(dto.habitos().cigarrilloElectronico());
        habitosToxicosRepository.save(habitos);

        MotivoConsulta motivo = new MotivoConsulta();
        motivo.setPaciente(paciente); // Conectamos con el paciente
        motivo.setMotivoConsulta(dto.historiaEnfermedad().motivoConsulta());
        motivo.setHistoriaEnfermedadActual(dto.historiaEnfermedad().historiaEnfermedadActual());
        motivoConsultaRepository.save(motivo);

        // 4. EXAMEN FÍSICO (Solo lo físico)
        ExamenFisico examen = new ExamenFisico();
        examen.setPaciente(paciente);
        examen.setPeso(dto.examenFisico().peso());
        examen.setTalla(dto.examenFisico().talla());
        examen.setTensionArterial(dto.examenFisico().tensionArterial());
        examen.setFrecuenciaCardiaca(dto.examenFisico().frecuenciaCardiaca());
        examen.setFrecuenciaRespiratoria(dto.examenFisico().frecuenciaRespiratoria());
        examen.setTemperatura(dto.examenFisico().temperatura());

        // Calcular IMC
        if (examen.getPeso() != null && examen.getTalla() != null && examen.getTalla() > 0) {
            double imc = examen.getPeso() / Math.pow(examen.getTalla(), 2);
            examen.setImc(Math.round(imc * 10.0) / 10.0);
        }
        examenFisicoRepository.save(examen);

        // 5. DIAGNÓSTICO Y TRATAMIENTO (Tu nueva entidad)
        DiagnosticoTratamiento diagnostico = new DiagnosticoTratamiento();
        diagnostico.setPaciente(paciente);
        diagnostico.setDiagnostico(dto.examenFisico().diagnostico());
        diagnostico.setTratamiento(dto.examenFisico().tratamiento());
        diagnostico.setEstudioComplementario(dto.examenFisico().estudioComplementarios());
        // ... (Tu código actual guardando paciente, antecedentes, hábitos, examen físico y diagnóstico) ...
        diagnosticoTratamientoRepository.save(diagnostico);

        // ==========================================
        // 6. GENERACIÓN Y GUARDADO FÍSICO DEL PDF
        // ==========================================
        log.info("Generando documento PDF para el nuevo paciente...");
        try {
            // A. Fabricar el PDF en la RAM
            byte[] pdfBytes = pdfService.generarHojaClinicaPdf(dto);

            // B. Guardarlo físicamente en su carpeta unificada
            String rutaFinal = pdfService.guardarPdfEnCarpetaPaciente(
                    pdfBytes,
                    paciente.getCedula(),
                    paciente.getNombreApellidos(),
                    "Historia_Clinica_Base"
            );

            // C. Actualizar el paciente con la ruta donde quedó guardado
            if (rutaFinal != null) {
                paciente.setRutaPdfHistoria(rutaFinal);
                pacienteRepository.save(paciente); // Guardamos la actualización
            }
        } catch (Exception e) {
            // Nota Senior: Si el PDF falla (ej. disco lleno), atrapamos el error para que
            // no haga "Rollback" a la base de datos. Es mejor tener los datos en BD y regenerar el PDF luego.
            log.error("Los datos se guardaron, pero ocurrió un error al generar el PDF físico.", e);
        }

        log.info("Transacción completada. Historia clínica guardada con éxito.");

        // Devolvemos la ruta del PDF que se guardó en la entidad
        return paciente.getRutaPdfHistoria();
    }


    // ==========================================
    // 2. LECTURA (Consultas para el Dashboard y Visor)
    // ==========================================

    public List<PacienteResumenDTO> buscarPacientes(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return pacienteRepository.obtenerTodosLosPacientesResumen();
        } else {
            List<Paciente> pacientes = pacienteRepository.findByNombreApellidosContainingIgnoreCaseOrCedulaContaining(filtro, filtro);
            return pacientes.stream()
                    .map(p -> new PacienteResumenDTO(p.getId(), p.getNombreApellidos(), p.getCedula()))
                    .toList();
        }
    }

    /**
     * Módulo Citas: Busca un paciente exacto por su cédula y devuelve la ENTIDAD.
     * Necesario para establecer relaciones (Foreign Keys) en la base de datos.
     */
    @Transactional(readOnly = true)
    public Paciente buscarPorCedulaExacta(String cedula) {
        log.info("Buscando entidad Paciente exacta con cédula: {}", cedula);

        return pacienteRepository.findByCedula(cedula)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún paciente con la cédula exacta: " + cedula));
    }

    public List<PacienteResumenDTO> obtenerPacientesParaDashboard() {
        return pacienteRepository.obtenerTodosLosPacientesResumen();
    }

    @Transactional(readOnly = true)
    public PacienteRegistroDTO obtenerExpedienteCompleto(UUID pacienteId) {
        // 1. Buscar paciente principal
        Paciente p = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // 2. Mapear a DatosPersonalesDTO
        DatosPersonalesDTO personalesDTO = new DatosPersonalesDTO(
                p.getNombreApellidos(), p.getCedula(), p.getFechaNacimiento(), "", "", "",
                p.getDireccion(), p.getTelefonos(), "", p.getSeguro(), p.getOcupacion(),
                p.getRutaPdfHistoria()
        );

        // 3. Buscar dependencias
        AntecedentesPaciente ant = antecedentesRepository.findByPacienteId(pacienteId).orElse(new AntecedentesPaciente());
        AntecedentesDTO antDTO = new AntecedentesDTO(ant.getAntecedentesFamiliares(), ant.getAntecedentesPersonales(), "", "", ant.getCirugias(), ant.getAlergias());

        HabitosToxicos hab = habitosToxicosRepository.findByPacienteId(pacienteId).orElse(new HabitosToxicos());
        HabitosToxicosDTO habDTO = new HabitosToxicosDTO(hab.isTabaco(), hab.isAlcohol(), hab.isHooka(), hab.isCigarroElectronico(), hab.isCafe(), hab.isDrogas());

        ExamenFisico ex = examenFisicoRepository.findByPacienteId(pacienteId).orElse(new ExamenFisico());
        // Agregamos también la búsqueda de diagnóstico
        DiagnosticoTratamiento diag = diagnosticoTratamientoRepository.findByPacienteId(pacienteId).orElse(new DiagnosticoTratamiento());

        MotivoConsulta motivo = motivoConsultaRepository.findByPacienteId(pacienteId).orElse(new MotivoConsulta());


        HistoriaEnfermedadDTO historiaDTO = new HistoriaEnfermedadDTO(
                motivo.getMotivoConsulta(),
                motivo.getHistoriaEnfermedadActual()
        );
        // Empaquetamos todo en el DTO como la UI lo espera
        ExamenFisicoDTO exDTO = new ExamenFisicoDTO(
                ex.getPeso(), ex.getTalla(), ex.getTensionArterial(), ex.getFrecuenciaCardiaca(), ex.getFrecuenciaRespiratoria(), ex.getTemperatura(),
                ex.getCabeza(), ex.getCuello(), ex.getTorax(), ex.getCorazon(), ex.getPulmones(), ex.getAbdomen(), ex.getGenitalesExternos(),
                ex.getMiembroSuperior(), ex.getMiembroInferior(), ex.getPielYFaneras(),
                "", // Hallazgos generales (si lo tienes)
                diag.getEstudioComplementario(), diag.getDiagnostico(), diag.getTratamiento() // Lo sacamos de la tabla diag!
        );

        // 4. Ensamblar y devolver
        return new PacienteRegistroDTO(personalesDTO, historiaDTO, antDTO, habDTO, exDTO);
    }
    // ==========================================
    // 3. MÓDULO DE EVOLUCIÓN MÉDICA
    // ==========================================

    /**
     * Guarda una nueva nota de evolución vinculada a un paciente específico.
     */
    @Transactional
    public void agregarEvolucion(UUID pacienteId, HojaEvolucionDTO dto) {
        log.info("Agregando nueva hoja de evolución para el paciente ID: {}", pacienteId);

        // 1. Verificamos paciente
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // 2. Guardamos en Base de Datos PostgreSQL
        HojaEvolucion evolucion = new HojaEvolucion();
        evolucion.setPaciente(paciente);
        evolucion.setFecha(LocalDate.now());
        evolucion.setMotivoSeguimiento(dto.motivoSeguimiento());
        evolucion.setHistoriaEnfermedadActual(dto.historiaEnfermedadActual());
        evolucion.setDiagnostico(dto.diagnostico());
        evolucion.setTratamiento(dto.tratamiento());
        evolucion.setPlan(dto.plan());

        // Guardamos primero para obtener un ID si fuera necesario y confirmar transacción
        evolucion = hojaEvolucionRepository.save(evolucion);

        // ==========================================
        // 3. MAGIA DE ARCHIVOS: Generación y Guardado del PDF
        // ==========================================
        try {
            // A. Fabricamos el PDF en memoria
            byte[] pdfBytes = pdfService.generarEvolucionPdf(paciente, evolucion);

            // B. Formateamos la fecha para el nombre del archivo (Ej: Evolucion_2026-05-04_123456)
            String prefijo = "Evolucion_" + evolucion.getFecha().toString();

            // C. Lo guardamos en su carpeta unificada
            String rutaFinal = pdfService.guardarPdfEnCarpetaPaciente(
                    pdfBytes,
                    paciente.getCedula(),
                    paciente.getNombreApellidos(),
                    prefijo
            );

            // D. Actualizamos la evolución en BD con la ruta exacta del PDF
            if (rutaFinal != null) {
                evolucion.setRutaPdf(rutaFinal);
                hojaEvolucionRepository.save(evolucion); // Actualización final
            }
        } catch (Exception e) {
            log.error("La evolución se guardó en BD, pero falló la generación física del PDF.", e);
        }

        log.info("Evolución médica completada y archivada.");
    }

    /**
     * Obtiene el historial completo de evoluciones de un paciente para mostrar en la UI.
     */
    @Transactional(readOnly = true)
    public List<HojaEvolucionDTO> obtenerEvolucionesPorPaciente(UUID pacienteId) {
        log.info("Recuperando historial de evoluciones del paciente ID: {}", pacienteId);

        // Asumiendo que creaste el método en el repositorio para ordenar por fecha
        // Solo pasas la variable 'pacienteId', sin el prefijo 'UUID'
                List<HojaEvolucion> evoluciones = hojaEvolucionRepository.findByPacienteIdOrderByFechaDesc(pacienteId);

        // Convertimos las entidades a DTOs para la interfaz gráfica
        return evoluciones.stream()
                .map(evo -> new HojaEvolucionDTO(
                        evo.getId(),
                        evo.getFecha(),
                        evo.getMotivoSeguimiento(),
                        evo.getHistoriaEnfermedadActual(),
                        evo.getDiagnostico(),
                        evo.getTratamiento(),
                        evo.getPlan(),
                        evo.getRutaPdf()
                ))
                .toList();
    }
}
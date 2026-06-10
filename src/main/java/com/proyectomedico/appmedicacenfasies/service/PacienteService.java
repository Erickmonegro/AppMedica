package com.proyectomedico.appmedicacenfasies.service;

import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.dto.*;
import com.proyectomedico.appmedicacenfasies.model.*;
import com.proyectomedico.appmedicacenfasies.model.sonografias.SonografiaAbdominal;
import com.proyectomedico.appmedicacenfasies.repository.*;
import com.proyectomedico.appmedicacenfasies.repository.sonografia.SonografiaAbdominalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final NotaMedicaRepository notaMedicaRepository;
    private final DiagnosticoTratamientoRepository diagnosticoTratamientoRepository;
    private final PdfService pdfService;

    private final SesionGlobal sesionGlobal;

    // INYECCIONES AÑADIDAS PARA RESOLVER VISIBILIDAD Y FECHAS
    private final TurnoRepository turnoRepository;
    private final SonografiaAbdominalRepository sonografiaAbdominalRepository;
    private final MedicoRepository medicoRepository;

    // ==========================================
    // 1. ESCRITURA (Guardar en Base de Datos)
    // ==========================================

    @Transactional
    public String registrarNuevaHistoriaClinica(PacienteRegistroDTO dto) {
        log.info("Iniciando procesamiento de Historia Clínica para paciente con cédula: {}", dto.datosPersonales().cedula());

        Paciente paciente = pacienteRepository.findByCedula(dto.datosPersonales().cedula())
                .orElseGet(() -> new Paciente());

        paciente.setNombreApellidos(dto.datosPersonales().nombreApellidos());
        paciente.setCedula(dto.datosPersonales().cedula());
        paciente.setFechaNacimiento(dto.datosPersonales().fechaNacimiento());
        paciente.setDireccion(dto.datosPersonales().direccion());
        paciente.setTelefonos(dto.datosPersonales().telefonos());
        paciente.setSeguro(dto.datosPersonales().seguro());
        paciente.setOcupacion(dto.datosPersonales().ocupacion());
        paciente.setSexo(dto.datosPersonales().sexo());

        paciente = pacienteRepository.save(paciente);

        AntecedentesPaciente antecedentes = antecedentesRepository.findByPaciente(paciente).orElse(new AntecedentesPaciente());
        antecedentes.setPaciente(paciente);
        antecedentes.setAntecedentesFamiliares(dto.antecedentes().antecedentesFamiliares());
        antecedentes.setAntecedentesPersonales(dto.antecedentes().antecedentesPersonales());
        antecedentes.setAlergias(dto.antecedentes().alergias());
        antecedentes.setCirugias(dto.antecedentes().cirugias());
        antecedentes.setTransfusion(dto.antecedentes().transfusiones());
        antecedentes.setMenarquia(dto.antecedentes().menarquia());
        antecedentes.setFum(dto.antecedentes().fum());
        antecedentes.setGpca(dto.antecedentes().gpca());
        antecedentesRepository.save(antecedentes);

        HabitosToxicos habitos = habitosToxicosRepository.findByPaciente(paciente).orElse(new HabitosToxicos());
        habitos.setPaciente(paciente);
        habitos.setTabaco(dto.habitos().tabaco());
        habitos.setAlcohol(dto.habitos().alcohol());
        habitos.setCafe(dto.habitos().cafe());
        habitos.setDrogas(dto.habitos().drogas());
        habitos.setHooka(dto.habitos().hooka());
        habitos.setCigarroElectronico(dto.habitos().cigarrilloElectronico());
        habitosToxicosRepository.save(habitos);

        MotivoConsulta motivo = motivoConsultaRepository.findByPaciente(paciente).orElse(new MotivoConsulta());
        motivo.setPaciente(paciente);
        motivo.setMotivoConsulta(dto.historiaEnfermedad().motivoConsulta());
        motivo.setHistoriaEnfermedadActual(dto.historiaEnfermedad().historiaEnfermedadActual());
        motivoConsultaRepository.save(motivo);

        ExamenFisico examen = examenFisicoRepository.findByPaciente(paciente).orElse(new ExamenFisico());
        examen.setPaciente(paciente);
        examen.setPeso(dto.examenFisico().peso());
        examen.setTalla(dto.examenFisico().talla());
        examen.setTensionArterial(dto.examenFisico().tensionArterial());
        examen.setFrecuenciaCardiaca(dto.examenFisico().frecuenciaCardiaca());
        examen.setFrecuenciaRespiratoria(dto.examenFisico().frecuenciaRespiratoria());
        examen.setTemperatura(dto.examenFisico().temperatura());
        examen.setSpO2(dto.examenFisico().spO2());
        // --- INYECTANDO EXAMEN FÍSICO REGIONAL (CORREGIDO) ---
        examen.setCabeza(dto.examenFisico().cabeza());
        examen.setCuello(dto.examenFisico().cuello());
        examen.setTorax(dto.examenFisico().torax());
        examen.setCorazon(dto.examenFisico().corazon());
        examen.setPulmones(dto.examenFisico().pulmones());
        examen.setAbdomen(dto.examenFisico().abdomen());

        // Nombres corregidos según tu DTO y Entidad:
        examen.setGenitalesExternos(dto.examenFisico().genitalesExternos());
        examen.setMiembroSuperior(dto.examenFisico().miembroSuperior());
        examen.setMiembroInferior(dto.examenFisico().miembroInferior());
        examen.setPielYFaneras(dto.examenFisico().pielYFaneras());
        examen.setHallazgosExamenFisico(dto.examenFisico().hallazgosExamenFisico());

        examen.setMamas(dto.examenFisico().mamas());
        examen.setEspeculoscopia(dto.examenFisico().especuloscopia());
        examen.setTactoVaginal(dto.examenFisico().tactoVaginal());

        if (examen.getPeso() != null && examen.getTalla() != null && examen.getTalla() > 0) {
            double imc = examen.getPeso() / Math.pow(examen.getTalla(), 2);
            examen.setImc(Math.round(imc * 10.0) / 10.0);
        }
        examenFisicoRepository.save(examen);

        DiagnosticoTratamiento diagnostico = diagnosticoTratamientoRepository.findByPaciente(paciente).orElse(new DiagnosticoTratamiento());
        diagnostico.setPaciente(paciente);
        diagnostico.setDiagnostico(dto.examenFisico().diagnostico());
        diagnostico.setTratamiento(dto.examenFisico().tratamiento());
        diagnostico.setEstudioComplementario(dto.examenFisico().estudioComplementarios());
        diagnosticoTratamientoRepository.save(diagnostico);

// ====================================================================
        // 🌟 MAGIA ARQUITECTÓNICA: EL AUTO-TURNO SILENCIOSO 🌟
        // Si el médico creó al paciente directo, el sistema le enlaza un turno invisible
        // ====================================================================
        if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico doctor) {

            // Verificamos si la secretaria ya le había creado un turno hoy para no duplicarlo
            boolean necesitaTurno = true;
            var ultimoTurno = turnoRepository.findFirstByPacienteIdOrderByFechaEntradaDesc(paciente.getId());

            if (ultimoTurno.isPresent() && ultimoTurno.get().getMedicoAsignado().getId().equals(doctor.getId())) {
                if (ultimoTurno.get().getFechaEntrada().toLocalDate().isEqual(LocalDate.now())) {
                    necesitaTurno = false; // Ya pasó por recepción hoy
                }
            }

            // Si vino directo de la calle, le forjamos su puente en la base de datos
            if (necesitaTurno) {
                log.info("Generando Auto-Turno para enlazar al paciente autónomo con el Dr. {}", doctor.getNombreCompleto());
                Turno autoTurno = new Turno();
                autoTurno.setPaciente(paciente);
                autoTurno.setMedicoAsignado(doctor);
                autoTurno.setAreaDestino("CONSULTA DIRECTA");
                autoTurno.setTipoEstudio("CONSULTA_ESTANDAR");
                autoTurno.setEstado("ATENDIDO");
                autoTurno.setFechaEntrada(java.time.LocalDateTime.now());
                turnoRepository.save(autoTurno);
            }
        }
        // ====================================================================


        try {
            byte[] pdfBytes = pdfService.generarHojaClinicaPdf(dto);
            String rutaFinal = pdfService.guardarPdfEnCarpetaPaciente(pdfBytes, paciente.getCedula(), paciente.getNombreApellidos(), "Historia_Clinica_Base");
            if (rutaFinal != null) {
                paciente.setRutaPdfHistoria(rutaFinal);
                pacienteRepository.save(paciente);
            }
        } catch (Exception e) {
            log.error("Error al generar el PDF físico.", e);
        }

        return paciente.getRutaPdfHistoria();
    }

    // ==========================================
    // 2. LECTURA Y CONSOLIDACIÓN (Dashboard y Visor)
    // ==========================================

    public List<PacienteResumenDTO> buscarPacientes(String filtro) {
        List<Paciente> pacientes;

        if (filtro == null || filtro.trim().isEmpty()) {
            pacientes = pacienteRepository.findAll();
        } else {
            pacientes = pacienteRepository.findByNombreApellidosContainingIgnoreCaseOrCedulaContaining(filtro, filtro);
        }

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return pacientes.stream().map(p -> new PacienteResumenDTO(
                p.getId(),
                p.getNombreApellidos(),
                p.getCedula(),
                obtenerUltimaVisitaResolucionCascada(p.getId(), formato)
        )).toList();
    }
    @Transactional(readOnly = true)
    public List<PacienteResumenDTO> buscarPacientesDelMedico(String filtro, UUID medicoId) {
        String filtroLimpio = (filtro == null) ? "" : filtro.trim();

        // 1. Obtener lista base desde los turnos
        List<Paciente> pacientesTurno = pacienteRepository.findPacientesPorMedicoYFiltro(medicoId, filtroLimpio);

        // 2. Extracción del nombre del médico para cruzar con registros de sonografía autónomos
        String nombreMedico = medicoRepository.findById(medicoId).map(Medico::getNombreCompleto).orElse("");

        List<Paciente> pacientesSonografia = sonografiaAbdominalRepository.findAll().stream()
                .filter(s -> s.getMedicoRealizador() != null && s.getMedicoRealizador().contains(nombreMedico))
                .map(SonografiaAbdominal::getPaciente)
                .toList();

        // 3. Consolidación y filtrado en memoria
        Set<Paciente> pacientesConsolidados = new HashSet<>(pacientesTurno);
        pacientesConsolidados.addAll(pacientesSonografia);

        if (!filtroLimpio.isEmpty()) {
            pacientesConsolidados = pacientesConsolidados.stream()
                    .filter(p -> (p.getNombreApellidos() != null && p.getNombreApellidos().toLowerCase().contains(filtroLimpio.toLowerCase())) ||
                            (p.getCedula() != null && p.getCedula().contains(filtroLimpio)))
                    .collect(Collectors.toSet());
        }

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return pacientesConsolidados.stream().map(p -> new PacienteResumenDTO(
                p.getId(),
                p.getNombreApellidos(),
                p.getCedula(),
                obtenerUltimaVisitaResolucionCascada(p.getId(), formato)
        )).toList();
    }

    // MOTOR DE RESOLUCIÓN DE FECHAS EN CASCADA
    private String obtenerUltimaVisitaResolucionCascada(UUID pacienteId, DateTimeFormatter formato) {
        // Nivel 1: Recepción / Turnos
        var turno = turnoRepository.findFirstByPacienteIdOrderByFechaEntradaDesc(pacienteId);
        if (turno.isPresent() && turno.get().getFechaEntrada() != null) {
            return turno.get().getFechaEntrada().format(formato);
        }

        // Nivel 2: Evolución Médica
        var evoluciones = hojaEvolucionRepository.findByPacienteIdOrderByFechaDesc(pacienteId);
        if (!evoluciones.isEmpty() && evoluciones.get(0).getFecha() != null) {
            return evoluciones.get(0).getFecha().format(formato);
        }

        // Nivel 3: Auto-Triaje Sonográfico
        var sonografias = sonografiaAbdominalRepository.findByPacienteIdOrderByFechaCreacionDesc(pacienteId);
        if (!sonografias.isEmpty() && sonografias.get(0).getFechaCreacion() != null) {
            return sonografias.get(0).getFechaCreacion().format(formato);
        }

        return "Sin registro";
    }

    @Transactional(readOnly = true)
    public Paciente buscarPorCedulaExacta(String cedula) {
        return pacienteRepository.findByCedula(cedula)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún paciente con la cédula exacta: " + cedula));
    }

    @Transactional(readOnly = true)
    public PacienteRegistroDTO obtenerExpedienteCompleto(UUID pacienteId) {
        Paciente p = pacienteRepository.findById(pacienteId).orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        DatosPersonalesDTO personalesDTO = new DatosPersonalesDTO(
                p.getNombreApellidos(), p.getCedula(), p.getFechaNacimiento(), "", "", "",
                p.getDireccion(), p.getTelefonos(), "", p.getSeguro(), p.getOcupacion(), p.getRutaPdfHistoria()
        );

        AntecedentesPaciente ant = antecedentesRepository.findByPacienteId(pacienteId).orElse(new AntecedentesPaciente());
        AntecedentesDTO antDTO = new AntecedentesDTO(ant.getAntecedentesFamiliares(), ant.getAntecedentesPersonales(), "", ant.getCirugias(), ant.getAlergias(), ant.getMenarquia(), ant.getFum(), ant.getGpca());

        HabitosToxicos hab = habitosToxicosRepository.findByPacienteId(pacienteId).orElse(new HabitosToxicos());
        HabitosToxicosDTO habDTO = new HabitosToxicosDTO(hab.isTabaco(), hab.isAlcohol(), hab.isHooka(), hab.isCigarroElectronico(), hab.isCafe(), hab.isDrogas());

        ExamenFisico ex = examenFisicoRepository.findByPacienteId(pacienteId).orElse(new ExamenFisico());
        DiagnosticoTratamiento diag = diagnosticoTratamientoRepository.findByPacienteId(pacienteId).orElse(new DiagnosticoTratamiento());
        MotivoConsulta motivo = motivoConsultaRepository.findByPacienteId(pacienteId).orElse(new MotivoConsulta());

        HistoriaEnfermedadDTO historiaDTO = new HistoriaEnfermedadDTO(motivo.getMotivoConsulta(), motivo.getHistoriaEnfermedadActual());
        ExamenFisicoDTO exDTO = new ExamenFisicoDTO(
                ex.getPeso(), ex.getTalla(), ex.getTensionArterial(), ex.getFrecuenciaCardiaca(), ex.getFrecuenciaRespiratoria(), ex.getTemperatura(), ex.getSpO2(),
                ex.getCabeza(), ex.getCuello(), ex.getTorax(), ex.getCorazon(), ex.getPulmones(), ex.getAbdomen(), ex.getGenitalesExternos(),
                ex.getMiembroSuperior(), ex.getMiembroInferior(), ex.getPielYFaneras(),
                "", ex.getMamas(), ex.getEspeculoscopia(), ex.getTactoVaginal(),
                diag.getEstudioComplementario(), diag.getDiagnostico(), diag.getTratamiento()
        );

        return new PacienteRegistroDTO(personalesDTO, historiaDTO, antDTO, habDTO, exDTO);
    }

    // ==========================================
    // 3. MÓDULO DE EVOLUCIÓN MÉDICA
    // ==========================================

    @Transactional
    public void agregarEvolucion(UUID pacienteId, HojaEvolucionDTO evolucionDTO) {
        Paciente paciente = pacienteRepository.findById(pacienteId).orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        HojaEvolucion nuevaEvolucion = new HojaEvolucion();
        nuevaEvolucion.setPaciente(paciente);
        nuevaEvolucion.setFecha(LocalDate.now());
        nuevaEvolucion.setMotivoSeguimiento(evolucionDTO.motivoSeguimiento());
        nuevaEvolucion.setHistoriaEnfermedadActual(evolucionDTO.historiaEnfermedadActual());
        nuevaEvolucion.setDiagnostico(evolucionDTO.diagnostico());
        nuevaEvolucion.setTratamiento(evolucionDTO.tratamiento());
        nuevaEvolucion.setPlan(evolucionDTO.plan());

        if (evolucionDTO.resultados() != null) {
            ResultadosHojaEvolucion lab = new ResultadosHojaEvolucion();
            lab.setHb(evolucionDTO.resultados().hb());
            lab.setHtco(evolucionDTO.resultados().htco());
            lab.setPlaq(evolucionDTO.resultados().plaq());
            lab.setGlic(evolucionDTO.resultados().glic());
            lab.setH1ac(evolucionDTO.resultados().h1ac());
            lab.setColest(evolucionDTO.resultados().colest());
            lab.setHdl(evolucionDTO.resultados().hdl());
            lab.setLdl(evolucionDTO.resultados().ldl());
            lab.setTrig(evolucionDTO.resultados().trig());
            lab.setSonografias(evolucionDTO.resultados().sonografias());
            lab.setHojaEvolucion(nuevaEvolucion);
            nuevaEvolucion.setResultadosLaboratorio(lab);
        }

        HojaEvolucion evolucionGuardada = hojaEvolucionRepository.save(nuevaEvolucion);

        try {
            byte[] pdfBytes = pdfService.generarEvolucionPdf(paciente, evolucionGuardada);
            String rutaFisica = pdfService.guardarPdfEnCarpetaPaciente(pdfBytes, paciente.getCedula(), paciente.getNombreApellidos(), "Evolucion_" + LocalDate.now());
            if (rutaFisica != null) {
                evolucionGuardada.setRutaPdf(rutaFisica);
                hojaEvolucionRepository.save(evolucionGuardada);
            }
        } catch (Exception e) {
            log.error("Fallo la generación física del PDF.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<HojaEvolucionDTO> obtenerEvolucionesPorPaciente(UUID pacienteId) {
        List<HojaEvolucion> evoluciones = hojaEvolucionRepository.findByPacienteIdOrderByFechaDesc(pacienteId);

        return evoluciones.stream().map(evo -> {
            ResultadosEvolucionDTO resultadosDTO = null;
            if (evo.getResultadosLaboratorio() != null) {
                var res = evo.getResultadosLaboratorio();
                resultadosDTO = new ResultadosEvolucionDTO(res.getHb(), res.getHtco(), res.getPlaq(), res.getGlic(), res.getH1ac(), res.getColest(), res.getHdl(), res.getLdl(), res.getTrig(), res.getSonografias(), res.getOtrosResultados());
            }

            return new HojaEvolucionDTO(evo.getId(), evo.getFecha(), evo.getMotivoSeguimiento(), evo.getHistoriaEnfermedadActual(), evo.getDiagnostico(), evo.getTratamiento(), evo.getPlan(), evo.getRutaPdf(), resultadosDTO, evo.getMedicoAuditoria());
        }).toList();
    }

    @Transactional
    public Paciente obtenerOCrearPacienteBasico(String cedula, String nombre, String telefono, String seguro, LocalDate fechaNacimiento, String sexo) {
        if (cedula == null || cedula.isEmpty()){
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setNombreApellidos(nombre);
            nuevoPaciente.setTelefonos(telefono);
            nuevoPaciente.setSeguro(seguro);
            nuevoPaciente.setFechaNacimiento(fechaNacimiento);
            nuevoPaciente.setSexo(sexo);
            return pacienteRepository.save(nuevoPaciente);
        }

        return pacienteRepository.findByCedula(cedula).orElseGet(() -> {
            Paciente p = new Paciente();
            p.setCedula(cedula);
            p.setNombreApellidos(nombre);
            p.setTelefonos(telefono);
            p.setSeguro(seguro);
            p.setFechaNacimiento(fechaNacimiento);
            p.setSexo(sexo);
            return pacienteRepository.save(p);
        });
    }

    public boolean tieneHistoriaClinicaCompleta(UUID pacienteId) {
        return pacienteRepository.findById(pacienteId).map(p -> p.getRutaPdfHistoria() != null && !p.getRutaPdfHistoria().trim().isEmpty()).orElse(false);
    }

    public void agregarNota(UUID pacienteId, String contenido, String medicoAutor) {
        Paciente paciente = pacienteRepository.findById(pacienteId).orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        NotaMedica nota = new NotaMedica();
        nota.setPaciente(paciente);
        nota.setContenido(contenido);
        nota.setMedicoAutor(medicoAutor);
        notaMedicaRepository.save(nota);
    }

    public List<NotaMedica> obtenerNotasDelPaciente(UUID pacienteId) {
        return notaMedicaRepository.findByPacienteIdOrderByFechaCreacionDesc(pacienteId);
    }
}
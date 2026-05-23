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
    private final NotaMedicaRepository notaMedicaRepository;
    // INYECTAMOS TU NUEVA TABLA
    private final DiagnosticoTratamientoRepository diagnosticoTratamientoRepository;
    private final PdfService pdfService;

    // ==========================================
    // 1. ESCRITURA (Guardar en Base de Datos)
    // ==========================================

    @Transactional
    public String registrarNuevaHistoriaClinica(PacienteRegistroDTO dto) {
        log.info("Iniciando procesamiento de Historia Clínica para paciente con cédula: {}", dto.datosPersonales().cedula());

        // 1. LÓGICA DE RECUPERACIÓN O CREACIÓN (UPSERT)
        // En lugar de lanzar error, buscamos si ya existe el registro base de la secretaria
        Paciente paciente = pacienteRepository.findByCedula(dto.datosPersonales().cedula())
                .orElseGet(() -> {
                    log.info("Paciente no encontrado en BD. Creando nuevo registro.");
                    return new Paciente();
                });

        // Actualizamos/Seteamos los datos personales
        paciente.setNombreApellidos(dto.datosPersonales().nombreApellidos());
        paciente.setCedula(dto.datosPersonales().cedula());
        paciente.setFechaNacimiento(dto.datosPersonales().fechaNacimiento());
        paciente.setDireccion(dto.datosPersonales().direccion());
        paciente.setTelefonos(dto.datosPersonales().telefonos());
        paciente.setSeguro(dto.datosPersonales().seguro());
        paciente.setOcupacion(dto.datosPersonales().ocupacion());
        paciente.setSexo(dto.datosPersonales().sexo());

        // Guardamos el paciente (Si ya tenía ID, JPA hará un UPDATE; si no, un INSERT)
        paciente = pacienteRepository.save(paciente);

        // 2. ANTECEDENTES (Buscamos si ya tiene para actualizar o crear nuevo)
        AntecedentesPaciente antecedentes = antecedentesRepository.findByPaciente(paciente)
                .orElse(new AntecedentesPaciente());

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

        // 3. HÁBITOS TÓXICOS
        HabitosToxicos habitos = habitosToxicosRepository.findByPaciente(paciente)
                .orElse(new HabitosToxicos());

        habitos.setPaciente(paciente);
        habitos.setTabaco(dto.habitos().tabaco());
        habitos.setAlcohol(dto.habitos().alcohol());
        habitos.setCafe(dto.habitos().cafe());
        habitos.setDrogas(dto.habitos().drogas());
        habitos.setHooka(dto.habitos().hooka());
        habitos.setCigarroElectronico(dto.habitos().cigarrilloElectronico());
        habitosToxicosRepository.save(habitos);

        // 4. MOTIVO DE CONSULTA
        MotivoConsulta motivo = motivoConsultaRepository.findByPaciente(paciente)
                .orElse(new MotivoConsulta());

        motivo.setPaciente(paciente);
        motivo.setMotivoConsulta(dto.historiaEnfermedad().motivoConsulta());
        motivo.setHistoriaEnfermedadActual(dto.historiaEnfermedad().historiaEnfermedadActual());
        motivoConsultaRepository.save(motivo);

        // 5. EXAMEN FÍSICO
        ExamenFisico examen = examenFisicoRepository.findByPaciente(paciente)
                .orElse(new ExamenFisico());

        examen.setPaciente(paciente);
        examen.setPeso(dto.examenFisico().peso());
        examen.setTalla(dto.examenFisico().talla());
        examen.setTensionArterial(dto.examenFisico().tensionArterial());
        examen.setFrecuenciaCardiaca(dto.examenFisico().frecuenciaCardiaca());
        examen.setFrecuenciaRespiratoria(dto.examenFisico().frecuenciaRespiratoria());
        examen.setTemperatura(dto.examenFisico().temperatura());
        examen.setSpO2(dto.examenFisico().spO2());

        if (examen.getPeso() != null && examen.getTalla() != null && examen.getTalla() > 0) {
            double imc = examen.getPeso() / Math.pow(examen.getTalla(), 2);
            examen.setImc(Math.round(imc * 10.0) / 10.0);
        }
        examenFisicoRepository.save(examen);

        // 6. DIAGNÓSTICO Y TRATAMIENTO
        DiagnosticoTratamiento diagnostico = diagnosticoTratamientoRepository.findByPaciente(paciente)
                .orElse(new DiagnosticoTratamiento());

        diagnostico.setPaciente(paciente);
        diagnostico.setDiagnostico(dto.examenFisico().diagnostico());
        diagnostico.setTratamiento(dto.examenFisico().tratamiento());
        diagnostico.setEstudioComplementario(dto.examenFisico().estudioComplementarios());
        diagnosticoTratamientoRepository.save(diagnostico);

        // 7. GENERACIÓN Y GUARDADO DEL PDF
        log.info("Generando documento PDF...");
        try {
            byte[] pdfBytes = pdfService.generarHojaClinicaPdf(dto);
            String rutaFinal = pdfService.guardarPdfEnCarpetaPaciente(
                    pdfBytes,
                    paciente.getCedula(),
                    paciente.getNombreApellidos(),
                    "Historia_Clinica_Base"
            );

            if (rutaFinal != null) {
                paciente.setRutaPdfHistoria(rutaFinal);
                pacienteRepository.save(paciente);
            }
        } catch (Exception e) {
            log.error("Los datos se guardaron, pero ocurrió un error al generar el PDF físico.", e);
        }

        log.info("Operación completada con éxito para el paciente: {}", paciente.getNombreApellidos());
        return paciente.getRutaPdfHistoria();
    }


    // ==========================================
    // 2. LECTURA (Consultas para el Dashboard y Visor)
    // ==========================================

    public List<PacienteResumenDTO> buscarPacientes(String filtro) {
        List<Paciente> pacientes;

        // 1. Decidimos qué lista de Entidades traer de la BD
        if (filtro == null || filtro.trim().isEmpty()) {
            // En lugar de traer DTOs incompletos, traemos las Entidades
            pacientes = pacienteRepository.findAll();
        } else {
            pacientes = pacienteRepository.findByNombreApellidosContainingIgnoreCaseOrCedulaContaining(filtro, filtro);
        }

        // 2. Definimos el formato de fecha para la tabla
        java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 3. Convertimos todas las entidades a DTOs con su fecha real
        return pacientes.stream().map(p -> {
            String fechaVisita = "Sin registro";

            // Buscamos la última evolución de este paciente
            List<HojaEvolucion> evoluciones = hojaEvolucionRepository.findByPacienteIdOrderByFechaDesc(p.getId());

            if (!evoluciones.isEmpty() && evoluciones.get(0).getFecha() != null) {
                fechaVisita = evoluciones.get(0).getFecha().format(formato);
            }

            return new PacienteResumenDTO(
                    p.getId(),
                    p.getNombreApellidos(),
                    p.getCedula(),
                    fechaVisita // <--- ¡Aquí inyectamos la realidad!
            );
        }).toList();
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
        AntecedentesDTO antDTO = new AntecedentesDTO(ant.getAntecedentesFamiliares(), ant.getAntecedentesPersonales(), "", ant.getCirugias(), ant.getAlergias(),
                ant.getMenarquia(),     // <--- NUEVO
                ant.getFum(),           // <--- NUEVO
                ant.getGpca());

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
                ex.getPeso(), ex.getTalla(), ex.getTensionArterial(), ex.getFrecuenciaCardiaca(), ex.getFrecuenciaRespiratoria(), ex.getTemperatura(), ex.getSpO2(),
                ex.getCabeza(), ex.getCuello(), ex.getTorax(), ex.getCorazon(), ex.getPulmones(), ex.getAbdomen(), ex.getGenitalesExternos(),
                ex.getMiembroSuperior(), ex.getMiembroInferior(), ex.getPielYFaneras(),
                "", ex.getMamas(), ex.getEspeculoscopia(), ex.getTactoVaginal(), // Hallazgos generales (si lo tienes)
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
    public void agregarEvolucion(UUID pacienteId, HojaEvolucionDTO evolucionDTO) {
        log.info("Agregando nueva evolución al paciente ID: {}", pacienteId);

        // 1. Buscamos al paciente
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // 2. Creamos la Entidad principal
        HojaEvolucion nuevaEvolucion = new HojaEvolucion();
        nuevaEvolucion.setPaciente(paciente);
        nuevaEvolucion.setFecha(LocalDate.now());
        nuevaEvolucion.setMotivoSeguimiento(evolucionDTO.motivoSeguimiento());
        nuevaEvolucion.setHistoriaEnfermedadActual(evolucionDTO.historiaEnfermedadActual());
        nuevaEvolucion.setDiagnostico(evolucionDTO.diagnostico());
        nuevaEvolucion.setTratamiento(evolucionDTO.tratamiento());
        nuevaEvolucion.setPlan(evolucionDTO.plan());

        // ==========================================
        // 3. ¡NUEVA MAGIA! DESEMPACAR LABORATORIOS
        // ==========================================
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

            // Vinculamos de forma bidireccional
            lab.setHojaEvolucion(nuevaEvolucion);
            nuevaEvolucion.setResultadosLaboratorio(lab);
        }

        // 4. Guardamos la evolución en la Base de Datos
        HojaEvolucion evolucionGuardada = hojaEvolucionRepository.save(nuevaEvolucion);

        // 5. Generación Física del PDF
        try {
            byte[] pdfBytes = pdfService.generarEvolucionPdf(paciente, evolucionGuardada);
            String rutaFisica = pdfService.guardarPdfEnCarpetaPaciente(
                    pdfBytes,
                    paciente.getCedula(),
                    paciente.getNombreApellidos(),
                    "Evolucion_" + LocalDate.now()
            );

            // 6. Actualizamos la BD con la ruta física del archivo
            if (rutaFisica != null) {
                evolucionGuardada.setRutaPdf(rutaFisica);
                hojaEvolucionRepository.save(evolucionGuardada);
            }

        } catch (Exception e) {
            log.error("La evolución se guardó en BD, pero falló la generación física del PDF.", e);
        }
    }

    /**
     * Obtiene el historial completo de evoluciones de un paciente para mostrar en la UI.
     * Incluye los nuevos resultados de laboratorio.
     */
    @Transactional(readOnly = true)
    public List<HojaEvolucionDTO> obtenerEvolucionesPorPaciente(UUID pacienteId) {
        log.info("Recuperando historial de evoluciones del paciente ID: {}", pacienteId);

        // 1. Buscamos las entidades en la base de datos
        List<HojaEvolucion> evoluciones = hojaEvolucionRepository.findByPacienteIdOrderByFechaDesc(pacienteId);

        // 2. Convertimos las entidades a DTOs usando Stream
        return evoluciones.stream()
                .map(evo -> {
                    // --- LÓGICA DE MAPEO PARA LABORATORIOS ---
                    ResultadosEvolucionDTO resultadosDTO = null;

                    // Si la evolución tiene laboratorios, creamos el DTO interno
                    if (evo.getResultadosLaboratorio() != null) {
                        var res = evo.getResultadosLaboratorio();
                        resultadosDTO = new ResultadosEvolucionDTO(
                                res.getHb(), res.getHtco(), res.getPlaq(),
                                res.getGlic(), res.getH1ac(), res.getColest(),
                                res.getHdl(), res.getLdl(), res.getTrig(),
                                res.getSonografias(), res.getOtrosResultados()
                        );
                    }

                    // 3. Creamos el HojaEvolucionDTO con los 9 parámetros requeridos
                    return new HojaEvolucionDTO(
                            evo.getId(),
                            evo.getFecha(),
                            evo.getMotivoSeguimiento(),
                            evo.getHistoriaEnfermedadActual(),
                            evo.getDiagnostico(),
                            evo.getTratamiento(),
                            evo.getPlan(),
                            evo.getRutaPdf(),
                            resultadosDTO, // <--- El 9no parámetro (Laboratorios)
                            evo.getMedicoAuditoria()
                    );
                })
                .toList();
    }
    /**
     * Módulo Secretaria: Registra a un paciente solo con sus datos básicos o lo recupera si ya existe.
     */
    @Transactional
    public Paciente obtenerOCrearPacienteBasico(String cedula, String nombre, String telefono, String seguro, LocalDate fechaNacimiento, String sexo) {

            log.info("Creando nuevo paciente básico desde Recepción: {}", cedula);

            if (cedula == null || cedula.isEmpty()){
                Paciente nuevoPaciente = new Paciente();
                nuevoPaciente.setNombreApellidos(nombre);
                nuevoPaciente.setTelefonos(telefono);
                nuevoPaciente.setSeguro(seguro);
                nuevoPaciente.setFechaNacimiento(fechaNacimiento); // <--- NUEVO
                nuevoPaciente.setSexo(sexo);                       // <--- NUEVO
                return pacienteRepository.save(nuevoPaciente);
            }

            return pacienteRepository.findByCedula(cedula)
                    .orElseGet(() -> {
            Paciente p = new Paciente();
            p.setCedula(cedula);
            p.setNombreApellidos(nombre);
            p.setTelefonos(telefono);
            p.setSeguro(seguro);
            p.setFechaNacimiento(fechaNacimiento); // <--- NUEVO
            p.setSexo(sexo);
            return pacienteRepository.save(p);
        });
    }
    /**
     * Verifica si el paciente ya tiene una historia clínica completa cargada.
     * Lo sabemos porque el médico debió haberle generado su PDF inicial.
     */
    public boolean tieneHistoriaClinicaCompleta(UUID pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .map(p -> p.getRutaPdfHistoria() != null && !p.getRutaPdfHistoria().trim().isEmpty())
                .orElse(false);
    }
    public List<PacienteResumenDTO> buscarPacientesDelMedico(String filtro, UUID medicoId) {
        String filtroLimpio = (filtro == null) ? "" : filtro.trim();

        // 1. Obtenemos las entidades desde la base de datos
        List<Paciente> pacientes = pacienteRepository.findPacientesPorMedicoYFiltro(medicoId, filtroLimpio);

        // 2. Definimos el formato visual de la fecha
        java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 3. Mapeamos inyectando la fecha real de la última visita
        return pacientes.stream().map(p -> {
            String fechaVisita = "Sin registro";

            // Buscamos el historial del paciente
            List<HojaEvolucion> evoluciones = hojaEvolucionRepository.findByPacienteIdOrderByFechaDesc(p.getId());

            // Si tiene historial, tomamos la fecha más nueva (la posición 0)
            if (!evoluciones.isEmpty() && evoluciones.get(0).getFecha() != null) {
                fechaVisita = evoluciones.get(0).getFecha().format(formato);
            }

            return new PacienteResumenDTO(
                    p.getId(),
                    p.getNombreApellidos(),
                    p.getCedula(),
                    fechaVisita // <--- ¡Ahora el doctor verá la fecha real!
            );
        }).toList();
    }
    // Inyectar NotaMedicaRepository arriba en la clase

    public void agregarNota(UUID pacienteId, String contenido, String medicoAutor) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        NotaMedica nota = new NotaMedica();
        nota.setPaciente(paciente);
        nota.setContenido(contenido);
        nota.setMedicoAutor(medicoAutor);
        // La fecha se pone sola por el @PrePersist

        notaMedicaRepository.save(nota);
    }

    public List<NotaMedica> obtenerNotasDelPaciente(UUID pacienteId) {
        return notaMedicaRepository.findByPacienteIdOrderByFechaCreacionDesc(pacienteId);
    }
}
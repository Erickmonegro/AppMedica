package com.proyectomedico.appmedicacenfasies.service;

import com.proyectomedico.appmedicacenfasies.dto.sonografias.*;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import com.proyectomedico.appmedicacenfasies.model.sonografias.*;


import com.proyectomedico.appmedicacenfasies.repository.PacienteRepository;
import com.proyectomedico.appmedicacenfasies.repository.sonografia.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SonografiaService {

    private final PacienteRepository pacienteRepository;
    private final SonografiaBaseRepository sonografiaBaseRepository;
    private final SonografiaAbdominalRepository sonografiaAbdominalRepository;
    private final SonografiaMamasRepository sonografiaMamasRepository;
    private final SonografiaObstetricaRepository sonografiaObstetricaRepository;
    private final SonografiaPelvicaFemeninaRepository sonografiaPelvicaFemeninaRepository;
    private final SonografiaPelvicaMasculinaRepository sonografiaPelvicaMasculinaRepository;
    private final SonografiaTiroidesRepository sonografiaTiroidesRepository;
    private final SonografiaPdfService sonografiaPdfService;

    // Aquí inyectarás también el PDF Service que haremos más adelante
    // private final SonografiaPdfService pdfService;

    /**
     * Obtiene todo el historial de sonografías de un paciente para el Visor.
     */
    public List<SonografiaBase> obtenerHistorialSonografias(UUID pacienteId) {
        return sonografiaBaseRepository.findByPacienteIdOrderByFechaCreacionDesc(pacienteId);
    }

    /**
     * Guarda una nueva Sonografía Abdominal
     */
    @Transactional
    public void guardarSonografiaAbdominal(UUID pacienteId, SonografiaAbdominalDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        SonografiaAbdominal sonografia = new SonografiaAbdominal();

        // 1. Llenamos los datos heredados (Base)
        sonografia.setPaciente(paciente);
        sonografia.setMedicoRealizador(dto.medicoRealizador());
        sonografia.setTipoSonografia("ABDOMINAL");
        sonografia.setDiagnosticoConclusion(dto.diagnosticoConclusion());
        // sonografia.setRutaPdf(...) -> Esto lo llenaremos cuando integremos el generador PDF

        // 2. Llenamos los datos específicos
        sonografia.setHigado(dto.higado());
        sonografia.setLhd(dto.lhd());
        sonografia.setConductoColedocoVenaPorta(dto.conductoColedocoVenaPorta());
        sonografia.setVesiculaBiliar(dto.vesiculaBiliar());
        sonografia.setPancreas(dto.pancreas());
        sonografia.setBazo(dto.bazo());
        sonografia.setRinonDerecho(dto.rinonDerecho());
        sonografia.setRinonIzquierdo(dto.rinonIzquierdo());
        sonografia.setIntestinos(dto.intestinos());
        try {
            // 1. Generamos el PDF físico y obtenemos la ruta
            String rutaPdfGenerada = sonografiaPdfService.generarPdfAbdominal(paciente, dto);

            // 2. Le asignamos la ruta a la entidad
            sonografia.setRutaPdf(rutaPdfGenerada);
            log.info("PDF generado y enlazado correctamente: {}", rutaPdfGenerada);
        } catch (Exception e) {
            log.error("Error al generar el PDF de la sonografía. Se guardará sin PDF.", e);
        }

        // 3. Guardamos en Base de Datos
        sonografiaAbdominalRepository.save(sonografia);
        log.info("Sonografía Abdominal guardada con éxito para paciente: {}", paciente.getNombreApellidos());
    }
    @Transactional
    public void guardarSonografiaMamas(UUID pacienteId, SonografiaMamasDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        SonografiaMamas sonografia = new SonografiaMamas();

        // 1. Llenamos los datos heredados (Base)
        sonografia.setPaciente(paciente);
        sonografia.setMedicoRealizador(dto.medicoRealizador());
        sonografia.setTipoSonografia("MAMAS");
        sonografia.setDiagnosticoConclusion(dto.diagnosticoConclusion());
        // sonografia.setRutaPdf(...) -> Esto lo llenaremos cuando integremos el generador PDF

        // 2. Llenamos los datos específicos
        sonografia.setSuperficieCutanea(dto.superficieCutanea());
        sonografia.setPezonAreola(dto.pezonAreola());
        sonografia.setMamaDerecha(dto.mamaDerecha());
        sonografia.setMamaIzquierda(dto.mamaIzquierda());
        sonografia.setRegionesAxilares(dto.regionesAuxiliares());
        sonografia.setBiRads(dto.biRands());

        try {
            // Generar PDF y asignar ruta a la entidad
            String rutaPdfGenerada = sonografiaPdfService.generarPdfMamas(paciente, dto);
            sonografia.setRutaPdf(rutaPdfGenerada);
            log.info("PDF de Mamas generado y enlazado correctamente: {}", rutaPdfGenerada);
        } catch (Exception e) {
            log.error("Error al generar el PDF de Mamas. Se guardará en BD sin PDF.", e);
        }
        // 3. Guardamos en Base de Datos
        sonografiaMamasRepository.save(sonografia);
        log.info("Sonografía de Mamas guardada con éxito para paciente: {}", paciente.getNombreApellidos());
    }
    @Transactional
    public void guardarSonografiaObstetrica(UUID pacienteId, SonografiaObstetricaDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        SonografiaObstetrica sonografia = new SonografiaObstetrica();

        // 1. Llenamos los datos heredados (Base)
        sonografia.setPaciente(paciente);
        sonografia.setMedicoRealizador(dto.medicoRealizador());
        sonografia.setTipoSonografia("OBSTETRICA");
        sonografia.setDiagnosticoConclusion(dto.diagnosticoConclusion());
        // sonografia.setRutaPdf(...) -> Esto lo llenaremos cuando integremos el generador PDF

        // 2. Llenamos los datos específicos
        sonografia.setFetoResumen(dto.fetoResumen());
        sonografia.setFrecuenciaCardiacaFetal(dto.frecuenciaCardiacaFetal());
        sonografia.setExtremidades(dto.extremidades());
        sonografia.setBpd(dto.bpd());
        sonografia.setHc(dto.hc());
        sonografia.setAc(dto.ac());
        sonografia.setFl(dto.fl());
        sonografia.setPesoEstimado(dto.pesoEstimado());
        sonografia.setPlacenta(dto.placenta());
        sonografia.setLiquidoAmniotico(dto.liquidoAmniotico());
        sonografia.setCordonUmbilical(dto.cordonUmbilical());

        try {
            String rutaPdfGenerada = sonografiaPdfService.generarPdfObstetrica(paciente, dto);
            sonografia.setRutaPdf(rutaPdfGenerada);
            log.info("PDF de Obstetrica generado y enlazado correctamente: {}", rutaPdfGenerada);
        } catch (Exception e) {
            log.error("Error al generar el PDF de Obstetrica. Se guardará en BD sin PDF.", e);
        }


        // 3. Guardamos en Base de Datos
        sonografiaObstetricaRepository.save(sonografia);
        log.info("Sonografía Obstetrica guardada con éxito para paciente: {}", paciente.getNombreApellidos());
    }
    @Transactional
    public void guardarSonografiaPelvicaFemenina(UUID pacienteId, SonografiaPelvicaFemeninaDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        SonografiaPelvicaFemenina sonografia = new SonografiaPelvicaFemenina();

        // 1. Llenamos los datos heredados (Base)
        sonografia.setPaciente(paciente);
        sonografia.setMedicoRealizador(dto.medicoRealizador());
        sonografia.setTipoSonografia("PELVICA_TRANSVAGINAL");
        sonografia.setDiagnosticoConclusion(dto.diagnosticoConclusion());
        // sonografia.setRutaPdf(...) -> Esto lo llenaremos cuando integremos el generador PDF

        // 2. Llenamos los datos específicos
        sonografia.setUtero(dto.utero());
        sonografia.setMedidasUtero(dto.medidaUteros());
        sonografia.setCervix(dto.cervix());
        sonografia.setCavidadUterinaEndometrio(dto.cavidadUterinaEndometrio());
        sonografia.setOvarioDerecho(dto.ovarioDerecho());
        sonografia.setOvarioIzquierdo(dto.ovarioIzquierdo());
        sonografia.setFondoSacoDouglas(dto.fondoSacoDouglas());
// ... (mapeo del DTO a la entidad)

        try {
            String rutaPdfGenerada = sonografiaPdfService.generarPdfPelvicaFemenina(paciente, dto);
            sonografia.setRutaPdf(rutaPdfGenerada);
            log.info("PDF de Pélvica Femenina generado y enlazado correctamente: {}", rutaPdfGenerada);
        } catch (Exception e) {
            log.error("Error al generar el PDF de Pélvica Femenina. Se guardará en BD sin PDF.", e);
        }

        // 3. Guardamos en Base de Datos
        sonografiaPelvicaFemeninaRepository.save(sonografia);

        // 3. Guardamos en Base de Datos
        sonografiaPelvicaFemeninaRepository.save(sonografia);
        log.info("Sonografía Pelvica guardada con éxito para paciente: {}", paciente.getNombreApellidos());
    }
    @Transactional
    public void guardarSonografiaPelvicaMasculina(UUID pacienteId, SonografiaPelvicaMasculinaDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        SonografiaPelvicaMasculina sonografia = new SonografiaPelvicaMasculina();

        // 1. Llenamos los datos heredados (Base)
        sonografia.setPaciente(paciente);
        sonografia.setMedicoRealizador(dto.medicoRealizador());
        sonografia.setTipoSonografia("PELVICAFEMENINA");
        sonografia.setDiagnosticoConclusion(dto.diagnosticoConclusion());
        // sonografia.setRutaPdf(...) -> Esto lo llenaremos cuando integremos el generador PDF

        // 2. Llenamos los datos específicos
        sonografia.setVejiga(dto.vejiga());
        sonografia.setVolumenPremiccion(dto.volumenPremiccion());
        sonografia.setVolumenPostmicion(dto.volumenPostmicion());
        sonografia.setProstata(dto.prostata());
        sonografia.setVolumenProstatico(dto.volumenProstatico());

// ... (Tu código actual donde empaquetas los datos en la entidad 'sonografia') ...

        try {
            // Generar PDF y asignar ruta a la entidad
            String rutaPdfGenerada = sonografiaPdfService.generarPdfPelvicaMasculina(paciente, dto);
            sonografia.setRutaPdf(rutaPdfGenerada);
            log.info("PDF de Pélvica Masculina generado y enlazado correctamente: {}", rutaPdfGenerada);
        } catch (Exception e) {
            log.error("Error al generar el PDF de Pélvica Masculina. Se guardará en BD sin PDF.", e);
        }

        // 3. Guardamos en Base de Datos
        sonografiaPelvicaMasculinaRepository.save(sonografia);
        // ...

        // 3. Guardamos en Base de Datos
        sonografiaPelvicaMasculinaRepository.save(sonografia);
        log.info("Sonografía Pelvica masculina guardada con éxito para paciente: {}", paciente.getNombreApellidos());
    }
    /**
     * Recupera todo el historial de sonografías abdominales de un paciente.
     * @param pacienteId El ID único del paciente.
     * // @return Lista de sonografías ordenadas de más reciente a más antigua.
     */

    @Transactional
    public void guardarSonografiaTiroides(UUID pacienteId, SonografiaTiroidesDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        SonografiaTiroides sonografia = new SonografiaTiroides();

        // 1. Llenamos los datos heredados (Base)
        sonografia.setPaciente(paciente);
        sonografia.setMedicoRealizador(dto.medicoRealizador());
        sonografia.setTipoSonografia("TIROIDES");
        sonografia.setDiagnosticoConclusion(dto.diagnosticoConclusion());
        // sonografia.setRutaPdf(...) -> Esto lo llenaremos cuando integremos el generador PDF

        // 2. Llenamos los datos específicos
        sonografia.setGlandulaTiroides(dto.glandulaTiroides());
        sonografia.setLobuloDerecho(dto.lobuloDerecho());
        sonografia.setLobuloIzquierdo(dto.lobuloIzquierdo());
        sonografia.setIstmo(dto.istmo());
        sonografia.setTraquea(dto.traquea());
        sonografia.setPlanoMuscular(dto.planoMuscular());
        sonografia.setTiRads(dto.tiRads());

        try {
            String rutaPdfGenerada = sonografiaPdfService.generarPdfTiroides(paciente, dto);
            sonografia.setRutaPdf(rutaPdfGenerada);
            log.info("PDF de Tiroides generado y enlazado correctamente: {}", rutaPdfGenerada);
        } catch (Exception e) {
            log.error("Error al generar el PDF de Tiroides. Se guardará en BD sin PDF.", e);
        }

        // 3. Guardamos en Base de Datos
        sonografiaTiroidesRepository.save(sonografia);
        log.info("Sonografía de Tiroides guardada con éxito para paciente: {}", paciente.getNombreApellidos());
    }

    // TODO: Repetir el método de guardado para guardarSonografiaObstetrica, guardarSonografiaMamas, etc.
}
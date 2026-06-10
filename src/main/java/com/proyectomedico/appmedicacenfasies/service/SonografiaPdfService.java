package com.proyectomedico.appmedicacenfasies.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.proyectomedico.appmedicacenfasies.dto.sonografias.*;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil; // Asegura esta importación
// IMPORTA AQUÍ TU CLASE DE SESIÓN GLOBAL
import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class SonografiaPdfService {

    private final TemplateEngine templateEngine;
    private final FileStorageService fileStorageService;

    // 1. AÑADIDO: Inyectamos el gestor de sesión para saber quién está logueado
    private final SesionGlobal sesionGlobal;

    // ===================================================================================
    // 🛠️ MÉTODO MAESTRO: INYECTA LOGO, PACIENTE Y FIRMA DINÁMICA A CUALQUIER REPORTE
    // ===================================================================================
    private void inyectarDatosComunes(Context context, Paciente paciente) {
        // --- 1. LOGO ---
        try {
            ClassPathResource imgFile = new ClassPathResource("img/image_3ba2fd.png");
            byte[] bytesImagen = StreamUtils.copyToByteArray(imgFile.getInputStream());
            String base64Image = Base64.getEncoder().encodeToString(bytesImagen);
            context.setVariable("logoBase64", "data:image/png;base64," + base64Image);
        } catch (Exception e) {
            log.warn("No se pudo cargar el membrete para la sonografía.", e);
            context.setVariable("logoBase64", "");
        }

        // --- 2. PACIENTE Y FECHA ---
        context.setVariable("nombrePaciente", paciente.getNombreApellidos());
        String edadStr = FormatoClinicoUtil.calcularEdadDesdeFecha(paciente.getFechaNacimiento());
        context.setVariable("edadPaciente", edadStr.isEmpty() ? "N/A AÑOS" : edadStr + " AÑOS");
        context.setVariable("fechaEstudio", LocalDate.now().format(DateTimeFormatter.ofPattern("dd / MM / yyyy")));

        // --- 3. FIRMA DINÁMICA (DR. / DRA.) ---
        String tituloMedico = "Dr."; // Por defecto
        String nombreMedico = "Médico Sonografista";

        if (sesionGlobal != null && sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico doctor) {
            if (doctor.getSexo() != null && doctor.getSexo().equalsIgnoreCase("Femenino")) {
                tituloMedico = "Dra.";
            } else {
                tituloMedico = "Dr.";
            }
            nombreMedico = doctor.getNombreCompleto();
        }

        context.setVariable("prefijoMedico", tituloMedico);
        context.setVariable("firmaMedico", nombreMedico);
    }

    // ===================================================================================
    // MÉTODOS DE GENERACIÓN (Ahora limpios y enfocados solo en los órganos)
    // ===================================================================================

    public String generarPdfAbdominal(Paciente paciente, SonografiaAbdominalDTO dto) {
        log.info("Generando Sonografía Abdominal para: {}", paciente.getNombreApellidos());
        try {
            Context context = new Context();
            inyectarDatosComunes(context, paciente); // <--- MAGIA SENIOR AQUÍ

            context.setVariable("diagnostico", dto.diagnosticoConclusion());
            context.setVariable("higado", dto.higado());
            context.setVariable("lhd", dto.lhd());
            context.setVariable("conductoColedoco", dto.conductoColedocoVenaPorta());
            context.setVariable("vesicula", dto.vesiculaBiliar());
            context.setVariable("pancreas", dto.pancreas());
            context.setVariable("bazo", dto.bazo());
            context.setVariable("rinonDerecho", dto.rinonDerecho());
            context.setVariable("rinonIzquierdo", dto.rinonIzquierdo());

            return renderizarYGuardar(context, "sonografia_abdominal", paciente, "ABDOMINAL");
        } catch (Exception e) {
            log.error("Error al generar PDF", e); throw new RuntimeException(e);
        }
    }

    public String generarPdfMamas(Paciente paciente, SonografiaMamasDTO dto) {
        log.info("Generando Sonografía Mamas para: {}", paciente.getNombreApellidos());
        try {
            Context context = new Context();
            inyectarDatosComunes(context, paciente);

            context.setVariable("diagnostico", dto.diagnosticoConclusion());
            context.setVariable("superficieCutanea", dto.superficieCutanea());
            context.setVariable("pezonAreola", dto.pezonAreola());
            context.setVariable("mamaDerecha", dto.mamaDerecha());
            context.setVariable("mamaIzquierda", dto.mamaIzquierda());
            context.setVariable("regionesAxilares", dto.regionesAuxiliares());
            context.setVariable("biRads", dto.biRands());

            return renderizarYGuardar(context, "sonografia_mamas", paciente, "MAMAS");
        } catch (Exception e) {
            log.error("Error al generar PDF", e); throw new RuntimeException(e);
        }
    }

    public String generarPdfPelvicaFemenina(Paciente paciente, SonografiaPelvicaFemeninaDTO dto) {
        log.info("Generando PDF Pélvica Femenina para: {}", paciente.getNombreApellidos());
        try {
            Context context = new Context();
            inyectarDatosComunes(context, paciente);

            context.setVariable("diagnostico", dto.diagnosticoConclusion());
            context.setVariable("utero", dto.utero());
            context.setVariable("medidaUteros", dto.medidaUteros());
            context.setVariable("cervix", dto.cervix());
            context.setVariable("cavidadUterinaEndometrio", dto.cavidadUterinaEndometrio());
            context.setVariable("ovarioDerecho", dto.ovarioDerecho());
            context.setVariable("ovarioIzquierdo", dto.ovarioIzquierdo());
            context.setVariable("fondoSacoDouglas", dto.fondoSacoDouglas());

            return renderizarYGuardar(context, "sonografia_pelvica_femenina", paciente, "PELVICA_FEMENINA");
        } catch (Exception e) {
            log.error("Error al generar PDF", e); throw new RuntimeException(e);
        }
    }

    public String generarPdfObstetrica(Paciente paciente, SonografiaObstetricaDTO dto) {
        log.info("Generando PDF Obstétrica para: {}", paciente.getNombreApellidos());
        try {
            Context context = new Context();
            inyectarDatosComunes(context, paciente);

            context.setVariable("diagnostico", dto.diagnosticoConclusion());
            context.setVariable("fetoResumen", dto.fetoResumen());
            context.setVariable("fcf", dto.frecuenciaCardiacaFetal());
            context.setVariable("extremidades", dto.extremidades());
            context.setVariable("bpd", dto.bpd() != null && !dto.bpd().isEmpty() ? dto.bpd() : "-");
            context.setVariable("hc", dto.hc() != null && !dto.hc().isEmpty() ? dto.hc() : "-");
            context.setVariable("ac", dto.ac() != null && !dto.ac().isEmpty() ? dto.ac() : "-");
            context.setVariable("fl", dto.fl() != null && !dto.fl().isEmpty() ? dto.fl() : "-");
            context.setVariable("pesoEstimado", dto.pesoEstimado() != null && !dto.pesoEstimado().isEmpty() ? dto.pesoEstimado() : "-");
            context.setVariable("placenta", dto.placenta());
            context.setVariable("liquidoAmniotico", dto.liquidoAmniotico());
            context.setVariable("cordonUmbilical", dto.cordonUmbilical());

            return renderizarYGuardar(context, "sonografia_obstetrica", paciente, "OBSTETRICA");
        } catch (Exception e) {
            log.error("Error al generar PDF", e); throw new RuntimeException(e);
        }
    }

    public String generarPdfPelvicaMasculina(Paciente paciente, SonografiaPelvicaMasculinaDTO dto) {
        log.info("Generando PDF Pélvica Masculina para: {}", paciente.getNombreApellidos());
        try {
            Context context = new Context();
            inyectarDatosComunes(context, paciente);

            context.setVariable("diagnostico", dto.diagnosticoConclusion());
            context.setVariable("vejiga", dto.vejiga());
            context.setVariable("volumenPremiccion", dto.volumenPremiccion());
            context.setVariable("volumenPostmicion", dto.volumenPostmicion());
            context.setVariable("prostata", dto.prostata());
            context.setVariable("volumenProstatico", dto.volumenProstatico());

            return renderizarYGuardar(context, "sonografia_pelvica_masculina", paciente, "PELVICA_MASCULINA");
        } catch (Exception e) {
            log.error("Error al generar PDF", e); throw new RuntimeException(e);
        }
    }

    public String generarPdfTiroides(Paciente paciente, SonografiaTiroidesDTO dto) {
        log.info("Generando PDF Tiroides para: {}", paciente.getNombreApellidos());
        try {
            Context context = new Context();
            inyectarDatosComunes(context, paciente);

            context.setVariable("diagnostico", dto.diagnosticoConclusion());
            context.setVariable("glandulaTiroides", dto.glandulaTiroides());
            context.setVariable("lobuloDerecho", dto.lobuloDerecho());
            context.setVariable("lobuloIzquierdo", dto.lobuloIzquierdo());
            context.setVariable("istmo", dto.istmo());
            context.setVariable("traquea", dto.traquea());
            context.setVariable("planoMuscular", dto.planoMuscular());
            context.setVariable("tiRads", dto.tiRads());

            return renderizarYGuardar(context, "sonografia_tiroides", paciente, "TIROIDES");
        } catch (Exception e) {
            log.error("Error al generar PDF", e); throw new RuntimeException(e);
        }
    }

    // ===================================================================================
    // ⚙️ MÉTODOS DE RENDERIZADO Y GUARDADO (También unificados para no repetir código)
    // ===================================================================================

    private String renderizarYGuardar(Context context, String nombrePlantilla, Paciente paciente, String tipoEstudio) throws Exception {
        String htmlRenderizado = templateEngine.process(nombrePlantilla, context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(htmlRenderizado, "/");
        builder.toStream(outputStream);
        builder.run();

        return guardarSonografiaFisicamente(outputStream.toByteArray(), paciente, tipoEstudio);
    }

    private String guardarSonografiaFisicamente(byte[] pdfBytes, Paciente paciente, String tipoEstudio) {
        try {
            Path carpetaPaciente = fileStorageService.obtenerOCrearCarpetaPaciente(paciente.getCedula(), paciente.getNombreApellidos());
            if (carpetaPaciente == null) return null;

            Path carpetaSonografias = carpetaPaciente.resolve("Sonografias");
            if (!Files.exists(carpetaSonografias)) Files.createDirectories(carpetaSonografias);

            String nombreSeguro = paciente.getNombreApellidos().replaceAll("[^a-zA-Z0-9.-]", "_");
            String nombreArchivo = tipoEstudio + "_" + nombreSeguro + "_" + System.currentTimeMillis() + ".pdf";

            Path archivoFinalPath = carpetaSonografias.resolve(nombreArchivo);
            Files.write(archivoFinalPath, pdfBytes);

            log.info("📄 PDF Sonografía guardado exitosamente: {}", archivoFinalPath.toAbsolutePath());
            return archivoFinalPath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("Error de E/S al guardar la sonografía.", e);
            return null;
        }
    }
}
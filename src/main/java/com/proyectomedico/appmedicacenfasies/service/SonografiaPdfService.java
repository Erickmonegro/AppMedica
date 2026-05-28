package com.proyectomedico.appmedicacenfasies.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.proyectomedico.appmedicacenfasies.dto.sonografias.SonografiaAbdominalDTO;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
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

    // 1. Inyectamos las herramientas que ya usas en tu otro servicio
    private final TemplateEngine templateEngine;
    private final FileStorageService fileStorageService;

    /**
     * Rellena el HTML con los datos, genera el archivo .pdf y lo guarda.
     */
    public String generarPdfAbdominal(Paciente paciente, SonografiaAbdominalDTO dto) {
        log.info("Iniciando generación de Sonografía Abdominal para paciente: {}", paciente.getNombreApellidos());

        try {
            Context context = new Context();

            // =================================================================
            // INYECCIÓN DEL LOGO (Igual que en PdfService)
            // =================================================================
            try {
                ClassPathResource imgFile = new ClassPathResource("img/image_3ba2fd.png");
                byte[] bytesImagen = StreamUtils.copyToByteArray(imgFile.getInputStream());
                String base64Image = Base64.getEncoder().encodeToString(bytesImagen);
                context.setVariable("logoBase64", "data:image/png;base64," + base64Image);
            } catch (Exception e) {
                log.warn("No se pudo cargar el membrete para la sonografía.", e);
                context.setVariable("logoBase64", "");
            }

            // =================================================================
            // DATOS DEL PACIENTE
            // =================================================================
            context.setVariable("nombrePaciente", paciente.getNombreApellidos());

            // Calculamos la edad usando el mismo método de java.time.Period
            String edadStr = "N/A";
            if (paciente.getFechaNacimiento() != null) {
                edadStr = String.valueOf(java.time.Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears());
            }
            context.setVariable("edadPaciente", edadStr + " años");
            context.setVariable("fechaEstudio", LocalDate.now().format(DateTimeFormatter.ofPattern("dd / MM / yyyy")));

            // =================================================================
            // DATOS DEL REPORTE
            // =================================================================
            context.setVariable("medicoRealizador", dto.medicoRealizador());
            context.setVariable("diagnostico", dto.diagnosticoConclusion());
            context.setVariable("higado", dto.higado());
            context.setVariable("lhd", dto.lhd());
            context.setVariable("conductoColedoco", dto.conductoColedocoVenaPorta());
            context.setVariable("vesicula", dto.vesiculaBiliar());
            context.setVariable("pancreas", dto.pancreas());
            context.setVariable("bazo", dto.bazo());
            context.setVariable("rinonDerecho", dto.rinonDerecho());
            context.setVariable("rinonIzquierdo", dto.rinonIzquierdo());

            // 1. Procesar la plantilla HTML (usando el HTML que diseñamos antes)
            String htmlRenderizado = templateEngine.process("sonografia_abdominal", context);

            // 2. Generar el PDF en memoria usando OpenHTMLToPDF (¡Como en tu PdfService!)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlRenderizado, "/");
            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();

            // 3. Guardar físicamente y retornar la ruta
            return guardarSonografiaFisicamente(pdfBytes, paciente, "ABDOMINAL");

        } catch (Exception e) {
            log.error("Error al generar el PDF de Sonografía Abdominal", e);
            throw new RuntimeException("Error en generación de PDF", e);
        }
    }

    /**
     * Usa el FileStorageService para ubicar la carpeta del paciente, y crea la subcarpeta "Sonografias".
     */
    private String guardarSonografiaFisicamente(byte[] pdfBytes, Paciente paciente, String tipoEstudio) {
        try {
            // 1. Obtener la carpeta principal del paciente desde tu FileStorageService
            Path carpetaPaciente = fileStorageService.obtenerOCrearCarpetaPaciente(paciente.getCedula(), paciente.getNombreApellidos());

            // 2. Crear la subcarpeta 'Sonografias' si no existe
            Path carpetaSonografias = carpetaPaciente.resolve("Sonografias");
            if (!Files.exists(carpetaSonografias)) {
                Files.createDirectories(carpetaSonografias);
            }

            // 3. Limpiar el nombre para Windows/Mac
            String nombreSeguro = paciente.getNombreApellidos().replaceAll("[^a-zA-Z0-9.-]", "_");
            String nombreArchivo = tipoEstudio + "_" + nombreSeguro + "_" + System.currentTimeMillis() + ".pdf";

            // 4. Escribir el archivo
            Path archivoFinalPath = carpetaSonografias.resolve(nombreArchivo);
            Files.write(archivoFinalPath, pdfBytes);

            log.info("📄 PDF Sonografía guardado físicamente en: {}", archivoFinalPath.toAbsolutePath());
            return archivoFinalPath.toAbsolutePath().toString();

        } catch (Exception e) {
            log.error("Error crítico al intentar guardar la sonografía en la carpeta del paciente.", e);
            return null;
        }
    }
}
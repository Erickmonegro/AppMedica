package com.proyectomedico.appmedicacenfasies.service;

import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.dto.PacienteRegistroDTO;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import com.proyectomedico.appmedicacenfasies.model.HojaEvolucion;
import java.time.Period;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import java.util.Base64;
// Tus otros imports...

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {

    // Spring Boot inyecta mágicamente el motor de Thymeleaf que configuramos en el pom.xml
    private final TemplateEngine templateEngine;
    private final FileStorageService fileStorageService;
    private final SesionGlobal sesionGlobal;

    /**
     * Genera un PDF en memoria basado en la plantilla hoja_clinica.html
     * @param pacienteData Los datos recolectados en la UI (Controller)
     * @return Arreglo de bytes que representa el archivo PDF
     */
    public byte[] generarHojaClinicaPdf(PacienteRegistroDTO pacienteData) {
        log.info("Iniciando generación de PDF para el paciente: {}", pacienteData.datosPersonales().nombreApellidos());
        Context context = new Context();

        // =================================================================
        // --- MAGIA SENIOR: INYECCIÓN DE LOGO EN BASE64 ---
        // Leemos la imagen desde el classpath (dentro del .jar compilado)
        // y la convertimos a Base64 para que el PDF no dependa de rutas físicas.
        // =================================================================
        try {
            org.springframework.core.io.ClassPathResource imgFile = new org.springframework.core.io.ClassPathResource("img/image_3ba2fd.png");
            byte[] bytesImagen = org.springframework.util.StreamUtils.copyToByteArray(imgFile.getInputStream());
            String base64Image = java.util.Base64.getEncoder().encodeToString(bytesImagen);

            // Creamos la etiqueta de origen (src) lista para HTML
            context.setVariable("logoBase64", "data:image/png;base64," + base64Image);
        } catch (Exception e) {
            log.warn("No se pudo cargar el membrete image_3ba2fd.png. El PDF se generará sin logo.", e);
            context.setVariable("logoBase64", ""); // Fallback de seguridad para no romper la app
        }
        try {
            // 1. Crear el "Contexto" (El puente entre Java y las variables de Thymeleaf)

            // --- ZONA A: INYECCIÓN MULTI-TENANT ---
            // (En el futuro, esto se extraerá del Login del usuario activo)
            // --- ZONA B: INYECCIÓN DE DATOS DEL PACIENTE ---
            // =========================================================================
            // MODIFICADO: Unificación del cálculo de Edad con FormatoUtil
            // =========================================================================
            context.setVariable("fechaActual", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("pacienteSeguro", pacienteData.datosPersonales().seguro());
            context.setVariable("pacienteNombre", pacienteData.datosPersonales().nombreApellidos());
            context.setVariable("pacienteFechaNac", pacienteData.datosPersonales().fechaNacimiento() != null ? pacienteData.datosPersonales().fechaNacimiento().toString() : "");
            context.setVariable("pacienteCedula", pacienteData.datosPersonales().cedula());

            // Usamos nuestra utilidad global para evitar discrepancias
            String edadCalculada = com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.calcularEdadDesdeFecha(pacienteData.datosPersonales().fechaNacimiento());
            context.setVariable("pacienteEdad", edadCalculada.isEmpty() ? "N/A" : edadCalculada);

            context.setVariable("pacienteTelefonos", pacienteData.datosPersonales().telefonos());
            context.setVariable("pacienteDireccion", pacienteData.datosPersonales().direccion());
            context.setVariable("pacienteOcupacion", pacienteData.datosPersonales().ocupacion());

            // --- ZONA C: ANTECEDENTES Y HÁBITOS ---
            context.setVariable("antFamiliares", pacienteData.antecedentes().antecedentesFamiliares());
            context.setVariable("antPersonales", pacienteData.antecedentes().antecedentesPersonales());
            context.setVariable("transfusion", pacienteData.antecedentes().transfusiones());
            context.setVariable("cirugias", pacienteData.antecedentes().cirugias());
            context.setVariable("alergias", pacienteData.antecedentes().alergias());

            // =========================================================
            // NUEVO: DATOS GINECOLÓGICOS Y SEXO
            // =========================================================
            context.setVariable("sexo", pacienteData.datosPersonales().sexo() != null ? pacienteData.datosPersonales().sexo() : "");
            context.setVariable("menarquia", pacienteData.antecedentes().menarquia() != null ? pacienteData.antecedentes().menarquia() : "");
            context.setVariable("gpca", pacienteData.antecedentes().gpca() != null ? pacienteData.antecedentes().gpca() : "");

            // Formatear la fecha FUM
            String fumStr = "";
            if (pacienteData.antecedentes().fum() != null) {
                fumStr = pacienteData.antecedentes().fum().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            context.setVariable("fum", fumStr);
            // --- HÁBITOS TÓXICOS (Lista Dinámica Inteligente) ---
            java.util.List<String> habitosActivos = new java.util.ArrayList<>();

            if (pacienteData.habitos().tabaco()) habitosActivos.add("Tabaco");
            if (pacienteData.habitos().alcohol()) habitosActivos.add("Alcohol");
            if (pacienteData.habitos().cafe()) habitosActivos.add("Café");
            if (pacienteData.habitos().hooka()) habitosActivos.add("Hooka");
            if (pacienteData.habitos().cigarrilloElectronico()) habitosActivos.add("Cigarrillo Electrónico");
            if (pacienteData.habitos().drogas()) habitosActivos.add("Drogas");

            // Si no hay ninguno marcado, ponemos "Ninguno". Si hay, los separamos por comas.
            String habitosTexto = habitosActivos.isEmpty() ? "Ninguno" : String.join(", ", habitosActivos);

            // Le pasamos la frase ya armada al HTML
            context.setVariable("habitosToxicos", habitosTexto);

            // --- ZONA D: EXAMEN FÍSICO ---
            // Si tienes un campo de motivo de consulta en la UI, mándalo aquí. Por ahora, lo dejamos vacío si no existe en el DTO
            context.setVariable("motivoConsulta", pacienteData.historiaEnfermedad().motivoConsulta());
            context.setVariable("historiaEnfermedad", pacienteData.historiaEnfermedad().historiaEnfermedadActual());

            context.setVariable("ta", pacienteData.examenFisico().tensionArterial());
            context.setVariable("fc", pacienteData.examenFisico().frecuenciaCardiaca());
            context.setVariable("fr", pacienteData.examenFisico().frecuenciaRespiratoria());
            context.setVariable("temp", pacienteData.examenFisico().temperatura() + " °C"); // <--- NUEVO: Faltaba la temperatura
            context.setVariable("spo2", pacienteData.examenFisico().spO2() != null ? pacienteData.examenFisico().spO2() + " %" : "N/A"); // <--- NUEVO
            context.setVariable("peso", pacienteData.examenFisico().peso() + " kg");
            context.setVariable("talla", pacienteData.examenFisico().talla() + " m");

            // --- ZONA D: EXAMEN FÍSICO ---
            // Extraemos los valores limpios
            double peso = pacienteData.examenFisico().peso();
            double talla = pacienteData.examenFisico().talla();

            // MAGIA: Autocorrección si el doctor ingresó centímetros (ej: 175) en lugar de metros (ej: 1.75)
            if (talla > 3.0) {
                talla = talla / 100.0;
            }

            // Cálculo del IMC
            double imc = 0.0;
            if (peso > 0 && talla > 0) {
                imc = peso / Math.pow(talla, 2);
            }

            // Inyectamos las variables formateadas al PDF
            context.setVariable("peso", peso > 0 ? peso + " kg" : "");
            // Imprimimos la talla siempre en formato metros con 2 decimales (ej: 1.75 m)
            context.setVariable("talla", talla > 0 ? String.format("%.2f m", talla) : "");
            context.setVariable("imc", imc > 0 ? String.format("%.1f", imc) : "");
            context.setVariable("cabeza", pacienteData.examenFisico().cabeza());
            context.setVariable("cuello", pacienteData.examenFisico().cuello());
            context.setVariable("torax", pacienteData.examenFisico().torax());
            context.setVariable("corazon", pacienteData.examenFisico().corazon());
            context.setVariable("pulmones", pacienteData.examenFisico().pulmones());
            context.setVariable("abdomen", pacienteData.examenFisico().abdomen());
            context.setVariable("genitales", pacienteData.examenFisico().genitalesExternos());
            context.setVariable("miembroSup", pacienteData.examenFisico().miembroSuperior());
            context.setVariable("miembroInf", pacienteData.examenFisico().miembroInferior());
            context.setVariable("piel", pacienteData.examenFisico().pielYFaneras());

            // --- ZONA E: DIAGNÓSTICO ---
            context.setVariable("estudios", pacienteData.examenFisico().estudioComplementarios()); // Asegúrate de tener este campo en el DTO
            context.setVariable("diagnostico", pacienteData.examenFisico().diagnostico());
            context.setVariable("tratamiento", pacienteData.examenFisico().tratamiento());

            // =========================================================
            // ZONA F: FIRMA DINÁMICA DEL MÉDICO (DR. / DRA.)
            // =========================================================
            String tituloMedico = "Dr/Dra.";
            String nombreMedico = "Médico Tratante";

            // Verificamos quién está logueado en el sistema
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof com.proyectomedico.appmedicacenfasies.model.Medico doctor) {
                // Asumiendo que el médico tiene un campo "sexo" en la base de datos
                if (doctor.getSexo() != null && doctor.getSexo().equalsIgnoreCase("Femenino")) {
                    tituloMedico = "Dra.";
                } else {
                    tituloMedico = "Dr.";
                }
                nombreMedico = doctor.getNombreCompleto();
            }

            // Enviamos las variables al HTML
            context.setVariable("prefijoMedico", tituloMedico);
            context.setVariable("firmaMedico", nombreMedico);

            // 2. Procesar la plantilla (Busca el archivo "hoja_clinica.html" en /templates/)

            String htmlProcesado = templateEngine.process("hoja_clinica", context);

            // 3. Configurar el motor de conversión a PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.useFastMode();
            // Le pasamos el HTML ya lleno de datos y le indicamos la ruta base por si hay imágenes o CSS externos
            builder.withHtmlContent(htmlProcesado, "/");
            builder.toStream(outputStream);

            // 4. Ejecutar la renderización
            builder.run();

            log.info("PDF generado exitosamente en la memoria RAM.");
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error crítico al renderizar el documento PDF", e);
            throw new RuntimeException("No se pudo generar el documento PDF", e);
        }
    }


    /*
     * Guarda el PDF en el disco duro local de forma segura y devuelve la ruta.
     */
    /**
     * Guarda el PDF físicamente usando la arquitectura de Primary/Fallback Storage.
     * Crea una carpeta unificada para el paciente basada en su Cédula y Nombre.
     *
     * @param prefijoArchivo Ej: "Historia_Clinica" o "Evolucion_Visita"
     */
    public String guardarPdfEnCarpetaPaciente(byte[] pdfBytes, String cedula, String nombrePaciente, String prefijoArchivo) {
        try {
            // 1. Obtener la carpeta segura del paciente usando nuestro FileStorageService
            Path carpetaPaciente = fileStorageService.obtenerOCrearCarpetaPaciente(cedula, nombrePaciente);

            // 2. Limpiar el nombre para que Windows/Mac no den error
            String nombreSeguro = nombrePaciente.replaceAll("[^a-zA-Z0-9.-]", "_");

            // 3. Armar el nombre del archivo (Ej: Evolucion_Visita_Juan_Perez_1684343.pdf)
            String nombreArchivo = prefijoArchivo + "_" + nombreSeguro + "_" + System.currentTimeMillis() + ".pdf";
            Path archivoFinalPath = carpetaPaciente.resolve(nombreArchivo);

            // 4. Escribir los bytes físicamente en la carpeta del paciente
            Files.write(archivoFinalPath, pdfBytes);

            log.info("PDF guardado físicamente en la carpeta unificada: {}", archivoFinalPath.toAbsolutePath());
            return archivoFinalPath.toAbsolutePath().toString();

        } catch (Exception e) {
            log.error("Error crítico al intentar guardar el PDF en la carpeta del paciente.", e);
            return null; // Retornamos null para que el Service principal sepa que hubo un fallo
        }
    }

    public byte[] generarEvolucionPdf(Paciente paciente, HojaEvolucion evolucion) {
        log.info("Generando PDF de Evolución para el paciente: {}", paciente.getNombreApellidos());

        try {
            Context context = new Context();
            try {
                org.springframework.core.io.ClassPathResource imgFile = new org.springframework.core.io.ClassPathResource("img/image_3ba2fd.png");
                byte[] bytesImagen = org.springframework.util.StreamUtils.copyToByteArray(imgFile.getInputStream());
                String base64Image = java.util.Base64.getEncoder().encodeToString(bytesImagen);
                context.setVariable("logoBase64", "data:image/png;base64," + base64Image);
            } catch (Exception e) {
                context.setVariable("logoBase64", "");
            }

            // 1. Datos del Paciente
            context.setVariable("pacienteNombre", paciente.getNombreApellidos());
            context.setVariable("fechaEvolucion", evolucion.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Cálculo dinámico de la edad
            String edadStr = com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.calcularEdadDesdeFecha(paciente.getFechaNacimiento());
            context.setVariable("pacienteEdad", edadStr.isEmpty() ? "N/A" : edadStr);

            // 2. Datos Clínicos
            context.setVariable("motivoSeguimiento", evolucion.getMotivoSeguimiento() != null ? evolucion.getMotivoSeguimiento() : "");
            context.setVariable("historiaEnfermedad", evolucion.getHistoriaEnfermedadActual() != null ? evolucion.getHistoriaEnfermedadActual() : "");
            context.setVariable("diagnostico", evolucion.getDiagnostico() != null ? evolucion.getDiagnostico() : "");
            context.setVariable("tratamiento", evolucion.getTratamiento() != null ? evolucion.getTratamiento() : "");
            context.setVariable("plan", evolucion.getPlan() != null ? evolucion.getPlan() : "");

            // ==========================================
            // 3. NUEVO: RESULTADOS DE LABORATORIO
            // ==========================================
            if (evolucion.getResultadosLaboratorio() != null) {
                var lab = evolucion.getResultadosLaboratorio();
                context.setVariable("hb", lab.getHb() != null ? lab.getHb() : "");
                context.setVariable("htco", lab.getHtco() != null ? lab.getHtco() : "");
                context.setVariable("plaq", lab.getPlaq() != null ? lab.getPlaq() : "");
                context.setVariable("glic", lab.getGlic() != null ? lab.getGlic() : "");
                context.setVariable("h1ac", lab.getH1ac() != null ? lab.getH1ac() : "");
                context.setVariable("colest", lab.getColest() != null ? lab.getColest() : "");
                context.setVariable("hdl", lab.getHdl() != null ? lab.getHdl() : "");
                context.setVariable("ldl", lab.getLdl() != null ? lab.getLdl() : "");
                context.setVariable("trig", lab.getTrig() != null ? lab.getTrig() : "");
                context.setVariable("sonografias", lab.getSonografias() != null ? lab.getSonografias() : "");
                context.setVariable("otrosResultados", lab.getOtrosResultados() != null ? lab.getOtrosResultados() : "");
            } else {
                // Si no hay laboratorios (evoluciones viejas), mandamos vacío para que no falle el HTML
                context.setVariable("hb", "");
                context.setVariable("htco", "");
                context.setVariable("plaq", "");
                context.setVariable("glic", "");
                context.setVariable("h1ac", "");
                context.setVariable("colest", "");
                context.setVariable("hdl", "");
                context.setVariable("ldl", "");
                context.setVariable("trig", "");
                context.setVariable("sonografias", "");
                context.setVariable("otrosResultados", "");
            }

            // 4. Procesamiento y Renderizado (Asegúrate de que el nombre coincida con tu archivo HTML)
            // NOTA: En tus mensajes anteriores, la plantilla se llamaba "hoja_evolucion.html".
            // Aquí en tu código original dice "hoja_evolucion_template". Usa el que corresponda a tu archivo físico.
            String htmlProcesado = templateEngine.process("hoja_evolucion_template", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlProcesado, "/");
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error al renderizar el documento PDF de Evolución", e);
            throw new RuntimeException("No se pudo generar el PDF de Evolución", e);
        }
    }
}
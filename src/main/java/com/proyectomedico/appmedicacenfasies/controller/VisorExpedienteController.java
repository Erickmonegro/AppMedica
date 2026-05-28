package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.controller.sonografia.NuevaSonografiaAbdominalController;
import com.proyectomedico.appmedicacenfasies.dto.PacienteRegistroDTO;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.NotaMedica;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.service.SonografiaPdfService;
import com.proyectomedico.appmedicacenfasies.service.SonografiaService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Period;
import java.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;


import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class VisorExpedienteController {

    private final PacienteService pacienteService;
    private final SesionGlobal sesionGlobal;
    private UUID pacienteIdActual;
    private final ApplicationContext applicationContext;
    // Variable para almacenar la ruta del PDF principal
    private String rutaPdfHistoriaActual;
    // Aquí luego inyectaremos el PdfService también
    private final SonografiaService sonografiaService; // ¡AÑADIR ESTE!
    private final SonografiaPdfService sonografiaPdfService;

    @FXML private VBox vboxListaSonografias; // ¡AÑADIR ESTE (Debe coincidir con el fx:id de tu FXML)!

    // --- PANEL IZQUIERDO (Perfil) ---
    @FXML
    private Label lblNombrePerfil;
    @FXML
    private Label lblCedulaPerfil;
    @FXML
    private Label lblEdad;
    @FXML
    private Label lblSeguro;
    @FXML
    private Label lblTelefono;
    @FXML
    private VBox vboxListaEvoluciones;
    @FXML
    private ListView<String> listaNotas;
    @FXML
    private TextArea txtNuevaNota;

    // Guardaremos el DTO maestro en memoria mientras la ventana esté abierta
    private PacienteRegistroDTO pacienteActual;

    @FXML
    public void initialize() {
        log.info("Ventana de Visor de Expediente inicializada.");
    }

    /**
     * Este método es llamado desde el Dashboard inmediatamente después de abrir la ventana.
     */
    public void cargarDatosPaciente(UUID pacienteId) {
        this.pacienteIdActual = pacienteId;
        log.info("Descargando historial completo de BD para el paciente: {}", pacienteId);

        try {
            // 1. Descargamos el DTO de la base de datos
            this.pacienteActual = pacienteService.obtenerExpedienteCompleto(pacienteId);

            // ==========================================
            // 2. ¡AQUÍ VA LA MAGIA DE LA RUTA DEL PDF!
            // ==========================================
            this.rutaPdfHistoriaActual = this.pacienteActual.datosPersonales().rutaPdfHistoria();

            // 3. Evitamos errores de NullPointerException con validaciones ternarias (Buena práctica Senior)
            String nombre = pacienteActual.datosPersonales().nombreApellidos();
            lblNombrePerfil.setText(nombre != null ? nombre : "Nombre no registrado");

            String cedula = pacienteActual.datosPersonales().cedula();
            lblCedulaPerfil.setText("Cédula: " + (cedula != null ? cedula : "N/A"));

            String seguro = pacienteActual.datosPersonales().seguro();
            lblSeguro.setText(seguro != null && !seguro.isEmpty() ? seguro : "Sin Seguro");

            String telefono = pacienteActual.datosPersonales().telefonos();
            lblTelefono.setText(telefono != null && !telefono.isEmpty() ? telefono : "No registrado");

            // --- MAGIA SENIOR: CÁLCULO DE EDAD REAL ---
            LocalDate fechaNac = pacienteActual.datosPersonales().fechaNacimiento();
            if (fechaNac != null) {
                int edadCalculada = Period.between(fechaNac, LocalDate.now()).getYears();
                lblEdad.setText(edadCalculada + " años");
            } else {
                lblEdad.setText("Edad desconocida");
            }

        } catch (Exception e) {
            log.error("No se pudo cargar la información del paciente.", e);
            lblNombrePerfil.setText("Error al cargar paciente");
        }
        cargarEvolucionesEnPanelDerecho();
        cargarHistorialNotas();
        cargarHistorialSonografias();
    }

    @FXML
    public void generarPdfInicial() {
        // Aquí conectaremos el PdfService en el próximo paso
        log.info("Botón de PDF Inicial presionado.");
    }


    @FXML
    public void abrirModalNuevaEvolucion() {
        if (pacienteIdActual == null) return; // Validación de seguridad

        try {
            log.info("Abriendo modal para nueva evolución...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nueva_evolucion.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);



            Parent root = fxmlLoader.load();

            // Le pasamos el ID del paciente al modal antes de mostrarlo
            NuevaEvolucionController modalController = fxmlLoader.getController();
            modalController.inicializarParaPaciente(pacienteIdActual);

            Stage stage = new Stage();
            stage.setTitle("Nueva Evolución Médica");
            stage.setScene(new Scene(root));

            // Modalidad que bloquea la ventana de atrás hasta que el médico guarde o cancele
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // showAndWait detiene el código aquí hasta que la ventana se cierre
            stage.showAndWait();

            // ¡MAGIA SENIOR! Cuando la ventana se cierra, recargamos el panel derecho para mostrar la nota recién creada
            log.info("Modal cerrado. Refrescando lista de evoluciones...");
            // showAndWait detiene el código aquí hasta que la ventana se cierre
            stage.showAndWait();

            // Cuando la ventana se cierra, recargamos el panel derecho para mostrar la nota recién creada
            log.info("Modal cerrado. Refrescando lista de evoluciones...");
            cargarEvolucionesEnPanelDerecho(); // ¡LLAMADA MÁGICA AQUÍ!

        } catch (Exception e) {
            log.error("Error al abrir el modal de evolución", e);
        }
    }

    @FXML
    public void volverAlDashboard(ActionEvent event) {
        // Obtenemos la ventana actual y la cerramos suavemente
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Motor de renderizado dinámico. Construye la interfaz visual basándose en los datos.
     */
    private void cargarEvolucionesEnPanelDerecho() {
        if (pacienteIdActual == null) return;

        log.info("Renderizando tarjetas de evolución en la UI...");

        // 1. Limpiamos el panel por si ya tenía tarjetas viejas
        vboxListaEvoluciones.getChildren().clear();

        try {
            // 2. Traemos la lista de la Base de Datos (ordenada desde la más nueva a la más vieja)
            var evoluciones = pacienteService.obtenerEvolucionesPorPaciente(pacienteIdActual);

            if (evoluciones.isEmpty()) {
                Label lblVacio = new Label("No hay evoluciones registradas para este paciente.");
                lblVacio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                vboxListaEvoluciones.getChildren().add(lblVacio);
                return;
            }

            // 3. Iteramos y creamos una "Tarjeta" visual por cada evolución
            java.time.format.DateTimeFormatter formatoFecha = java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy");

            for (var evo : evoluciones) {
                VBox tarjeta = new VBox(8); // VBox con 8px de separación interna
                tarjeta.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                tarjeta.setPadding(new javafx.geometry.Insets(15));

                // --- CABECERA DE LA TARJETA (FECHA Y BOTÓN PDF) ---
                // Usamos un HBox para poner la fecha a la izquierda y el botón a la derecha
                javafx.scene.layout.HBox cabecera = new javafx.scene.layout.HBox();
                cabecera.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                cabecera.setSpacing(10);

                // Un "Region" vacío que empuja el botón hacia la derecha (truco Senior de UI)
                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                Label lblFecha = new Label("Evolución del: " + evo.fecha().format(formatoFecha));
                lblFecha.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                cabecera.getChildren().addAll(lblFecha, spacer);

                // --- LÓGICA DEL BOTÓN "VER PDF" ---
                if (evo.rutaPdf() != null && !evo.rutaPdf().trim().isEmpty()) {
                    javafx.scene.control.Button btnVerPdf = new javafx.scene.control.Button("📄 Ver PDF");
                    btnVerPdf.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-border-color: #bfdbfe; -fx-border-radius: 4px; -fx-cursor: hand;");

                    // Acción al hacer clic
                    btnVerPdf.setOnAction(event -> abrirDocumentoPdf(evo.rutaPdf()));

                    cabecera.getChildren().add(btnVerPdf);
                }

                // Agregamos la cabecera y el separador a la tarjeta
                tarjeta.getChildren().add(cabecera);
                tarjeta.getChildren().add(new javafx.scene.control.Separator());

                // Agregamos los bloques de texto
                agregarFilaATarjeta(tarjeta, "Motivo:", evo.motivoSeguimiento());
                agregarFilaATarjeta(tarjeta, "Historia Actual:", evo.historiaEnfermedadActual());
                agregarFilaATarjeta(tarjeta, "Diagnóstico:", evo.diagnostico());
                agregarFilaATarjeta(tarjeta, "Tratamiento:", evo.tratamiento());
                agregarFilaATarjeta(tarjeta, "Plan:", evo.plan());

                // Insertamos la tarjeta terminada en el contenedor principal
                vboxListaEvoluciones.getChildren().add(tarjeta);
            }

        } catch (Exception e) {
            log.error("Error al cargar las evoluciones en la UI", e);
        }
    }

    /**
     * Método utilitario de UI para no repetir código al construir las tarjetas.
     */
    private void agregarFilaATarjeta(VBox tarjeta, String titulo, String contenido) {
        if (contenido != null && !contenido.trim().isEmpty()) {
            VBox fila = new VBox(2);
            Label lblTitulo = new Label(titulo);
            lblTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 11px;");

            Label lblContenido = new Label(contenido);
            lblContenido.setWrapText(true); // Muy importante para que el texto largo salte de línea
            lblContenido.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");

            fila.getChildren().addAll(lblTitulo, lblContenido);
            tarjeta.getChildren().add(fila);
        }
    }

    /*
     * Método utilitario de Arquitectura: Abre un archivo físico utilizando el lector nativo del OS.
     */
    /*
     * Método utilitario de Arquitectura: Abre un archivo físico utilizando el lector nativo del OS.
     * Incluye contingencia para el modo Headless de Spring Boot.
     */
    private void abrirDocumentoPdf(String rutaFisica) {
        try {
            java.io.File archivoPdf = new java.io.File(rutaFisica);

            if (!archivoPdf.exists()) {
                log.error("El archivo no existe en la ruta especificada: {}", rutaFisica);
                // TODO: Mostrar un Alert de JavaFX indicando que el archivo no se encontró
                return;
            }

            // INTENTO 1: Usar la vía tradicional de Java (AWT)
            if (java.awt.Desktop.isDesktopSupported() && !java.awt.GraphicsEnvironment.isHeadless()) {
                log.info("Abriendo PDF nativamente vía Java AWT: {}", rutaFisica);
                java.awt.Desktop.getDesktop().open(archivoPdf);

            } else {
                // INTENTO 2: Plan de contingencia Senior (Comandos nativos del OS)
                log.info("AWT bloqueado por Spring Boot. Invocando comandos nativos del Sistema Operativo...");

                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("win")) {
                    // Hablamos directamente con el CMD de Windows
                    new ProcessBuilder("cmd", "/c", "start", "", archivoPdf.getAbsolutePath()).start();
                } else if (os.contains("mac")) {
                    // Hablamos directamente con la terminal de macOS
                    new ProcessBuilder("open", archivoPdf.getAbsolutePath()).start();
                } else {
                    log.warn("Sistema operativo no reconocido. Abra el archivo manualmente: {}", rutaFisica);
                }
            }
        } catch (Exception e) {
            log.error("Error crítico al intentar abrir el archivo PDF.", e);
        }
    }

    @FXML
    public void verHistoriaInicialPdf(javafx.event.ActionEvent event) {
        log.info("Botón de Historia Inicial presionado.");

        if (this.rutaPdfHistoriaActual != null && !this.rutaPdfHistoriaActual.trim().isEmpty()) {
            // 1. EL PACIENTE TIENE HISTORIA: La abrimos como visor PDF
            abrirDocumentoPdf(this.rutaPdfHistoriaActual);
        } else {
            // 2. EL PACIENTE NO TIENE HISTORIA: Levantamos el formulario
            log.info("El paciente no tiene historia clínica base. Abriendo formulario de creación...");
            abrirFormularioHistoriaClinicaFaltante();
        }
    }

    private void abrirFormularioHistoriaClinicaFaltante() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nuevo_paciente.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();

            // =========================================================
            // ¡MAGIA!: Le pasamos los datos básicos que ya tenemos
            // al controlador de Nuevo Paciente para que no empiece de cero.
            // =========================================================
            NuevoPacienteController controller = fxmlLoader.getController();

            // Asumiendo que tu método cargarDatosPreliminares acepta un DTO o Entidad
            // (Ajusta los parámetros según como tengas programado ese método)
            com.proyectomedico.appmedicacenfasies.model.Paciente pBasico = new com.proyectomedico.appmedicacenfasies.model.Paciente();
            pBasico.setId(this.pacienteIdActual);
            pBasico.setNombreApellidos(this.pacienteActual.datosPersonales().nombreApellidos());
            pBasico.setCedula(this.pacienteActual.datosPersonales().cedula());

            controller.cargarDatosPreliminares(pBasico, "GENERAL");

            Stage stage = new Stage();
            stage.setTitle("Completar Historia Clínica - " + pBasico.getNombreApellidos());
            stage.setScene(new Scene(root, 1000, 800));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Cuando cierre el modal, recargamos el visor entero para ver si ya generó el PDF
            cargarDatosPaciente(this.pacienteIdActual);

        } catch (Exception e) {
            log.error("Error al abrir el formulario de historia clínica faltante", e);
            mostrarAlerta("Error", "No se pudo abrir el formulario de historia clínica.");
        }
    }


    /**
     * Método utilitario de UI: Muestra un popup de advertencia en pantalla.
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


    // Llama a esto dentro de tu método donde cargas los datos del paciente al abrir el visor
    private void cargarHistorialNotas() {
        listaNotas.getItems().clear();
        List<NotaMedica> notas = pacienteService.obtenerNotasDelPaciente(pacienteIdActual);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

        for (NotaMedica nota : notas) {
            String encabezado = nota.getFechaCreacion().format(formatter) + " - " + nota.getMedicoAutor();
            String item = encabezado + "\n" + nota.getContenido();
            listaNotas.getItems().add(item);
        }
    }

    @FXML
    public void guardarNotaRapida() {

        try {
            String contenido = txtNuevaNota.getText().trim();
            if (contenido.isEmpty()) return;


            // Sacar el nombre del doctor de la sesión
            String medico = "Médico";
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico) {
                medico = "Dr. " + ((Medico) sesionGlobal.getUsuarioLogueado()).getNombreCompleto();
            }

            // Guardar, limpiar y recargar
            pacienteService.agregarNota(pacienteIdActual, contenido, medico);
            txtNuevaNota.clear();
            cargarHistorialNotas(); // Refrescar la lista en tiempo real
            System.out.println("Funcionando");
        } catch (Exception e) {
            log.error("Hay un bobo, ta aqui", e);
        }
    }
    @FXML
    public void abrirModalSonografiaAbdominal() {
        if (pacienteIdActual == null) {
            log.warn("No se puede abrir la sonografía porque no hay un paciente seleccionado.");
            return;
        }

        try {
            log.info("Abriendo modal para Sonografía Abdominal...");

            // 1. RUTA RELATIVA CORRECTA (Desde el Classpath)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nueva_sonografia_abdominal.fxml"));

            // 2. DESCOMENTADO: Conectamos Spring Boot al modal para que los Servicios se inyecten
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();

            // 3. Pasarle el ID del paciente al modal
            // OJO: Usamos 'pacienteIdActual', que es la variable que tú usas para todo en este controlador
            NuevaSonografiaAbdominalController modalController = fxmlLoader.getController();
            modalController.inicializarParaPaciente(pacienteIdActual);

            // 4. Mostrar la ventana emergente (Modal)
            Stage stage = new Stage();
            stage.setTitle("Nueva Sonografía Abdominal");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait(); // Espera a que el doctor guarde y cierre la ventana
            cargarHistorialSonografias();
            // 5. ¡Próximamente! Refrescar la UI
            log.info("Modal cerrado. (Aquí luego llamaremos a cargarHistorialSonografias())");

        } catch (Exception e) {
            log.error("Error crítico al abrir modal de sonografía abdominal", e);
            mostrarAlerta("Error de Interfaz", "No se pudo cargar la pantalla de Sonografía: " + e.getMessage());
        }

    }
    private void cargarHistorialSonografias() {
        if (pacienteIdActual == null || vboxListaSonografias == null) return;

        log.info("Renderizando tarjetas de sonografía en la UI...");
        vboxListaSonografias.getChildren().clear(); // Limpiamos el panel

        try {
            // Asume que tienes un método en tu servicio que busca las sonografías de este paciente
            var sonografias = sonografiaService.obtenerSonografiasPorPaciente(pacienteIdActual);

            if (sonografias.isEmpty()) {
                Label lblVacio = new Label("No hay reportes sonográficos registrados para este paciente.");
                lblVacio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                vboxListaSonografias.getChildren().add(lblVacio);
                return;
            }

            java.time.format.DateTimeFormatter formatoFecha = java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy");

            for (var sono : sonografias) {
                VBox tarjeta = new VBox(8);
                tarjeta.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                tarjeta.setPadding(new javafx.geometry.Insets(15));

                javafx.scene.layout.HBox cabecera = new javafx.scene.layout.HBox();
                cabecera.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                cabecera.setSpacing(10);

                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                // ========================================================
                // 1. BLINDAJE DE FECHA (Evita que la pantalla quede en blanco)
                // ========================================================
                String fechaTexto = "Fecha no registrada";
                if (sono.getFechaCreacion() != null) {
                    fechaTexto = sono.getFechaCreacion().format(formatoFecha);
                }

                Label lblTitulo = new Label("Ecografía Abdominal - " + fechaTexto);
                lblTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                cabecera.getChildren().addAll(lblTitulo, spacer);

                // ========================================================
                // 2. BLINDAJE DEL PDF
                // ========================================================
                if (sono.getRutaPdf() != null && !sono.getRutaPdf().trim().isEmpty()) {
                    javafx.scene.control.Button btnVerPdf = new javafx.scene.control.Button("📄 Ver PDF");
                    btnVerPdf.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-border-color: #bfdbfe; -fx-border-radius: 4px; -fx-cursor: hand;");
                    btnVerPdf.setOnAction(event -> abrirDocumentoPdf(sono.getRutaPdf()));
                    cabecera.getChildren().add(btnVerPdf);
                } else {
                    Label lblSinPdf = new Label("Sin PDF generado");
                    lblSinPdf.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-style: italic;");
                    cabecera.getChildren().add(lblSinPdf);
                }

                tarjeta.getChildren().add(cabecera);
                tarjeta.getChildren().add(new javafx.scene.control.Separator());

                // --- DATOS RÁPIDOS EN LA TARJETA ---
                agregarFilaATarjeta(tarjeta, "Médico encargado:", sono.getMedicoRealizador());
                agregarFilaATarjeta(tarjeta, "Diagnóstico:", sono.getDiagnosticoConclusion());

                vboxListaSonografias.getChildren().add(tarjeta);
            }
        } catch (Exception e) {
            log.error("Error al cargar las sonografías en la UI", e);
        }
    }
    public void ejecutarAutoAperturaSonografia(String tipoEstudio) {
        log.info("Recibida orden de auto-apertura para el estudio: {}", tipoEstudio);

        if (tipoEstudio == null) {
            log.warn("El tipo de estudio es nulo. No se puede abrir la plantilla automática.");
            return;
        }

        switch (tipoEstudio.toUpperCase()) {
            case "ABDOMINAL":
                abrirModalSonografiaAbdominal();
                break;
            case "OBSTETRICA":
                mostrarAlerta("En Desarrollo", "La plantilla para Sonografía Obstétrica estará lista pronto.");
                break;
            case "MAMAS":
                mostrarAlerta("En Desarrollo", "La plantilla para Sonografía de Mamas estará lista pronto.");
                break;
            case "PELVICA_FEMENINA":
            case "PELVICA_MASCULINA":
                mostrarAlerta("En Desarrollo", "La plantilla para Sonografía Pélvica estará lista pronto.");
                break;
            default:
                log.warn("No hay una plantilla automática configurada para el estudio: {}", tipoEstudio);
                mostrarAlerta("Atención", "No se encontró la plantilla para: " + tipoEstudio);
                break;
        }
    }
}
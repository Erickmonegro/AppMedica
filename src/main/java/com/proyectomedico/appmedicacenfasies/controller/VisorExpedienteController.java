package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.controller.sonografia.*;
import com.proyectomedico.appmedicacenfasies.dto.PacienteRegistroDTO;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.NotaMedica;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.service.SonografiaPdfService;
import com.proyectomedico.appmedicacenfasies.service.SonografiaService;
import javafx.application.Platform;
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
    private String rutaPdfHistoriaActual;

    private final SonografiaService sonografiaService;
    private final SonografiaPdfService sonografiaPdfService;

    @FXML private VBox vboxListaSonografias;

    // =========================================================
    // NUEVO: Nodo genérico para el botón/menú de Sonografías
    // ¡Asegúrate de ponerle fx:id="btnNuevaSonografia" en tu FXML!
    // =========================================================
    @FXML private Node btnNuevaSonografia;

    // --- PANEL IZQUIERDO (Perfil) ---
    @FXML private Label lblNombrePerfil;
    @FXML private Label lblCedulaPerfil;
    @FXML private Label lblEdad;
    @FXML private Label lblSeguro;
    @FXML private Label lblTelefono;
    @FXML private VBox vboxListaEvoluciones;
    @FXML private ListView<String> listaNotas;
    @FXML private TextArea txtNuevaNota;

    private PacienteRegistroDTO pacienteActual;

    @FXML
    public void initialize() {
        log.info("Ventana de Visor de Expediente inicializada.");

        // =========================================================================
        // 1. RBAC (Control de Roles): Ocultar sonografía si no es especialista
        // =========================================================================
        if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico doctor) {

            // MAGIA SENIOR (Java Streams): Revisamos si en su SET de especialidades hay alguna de Sonografía
            boolean esSonografista = doctor.getEspecialidades() != null &&
                    doctor.getEspecialidades().stream()
                            .anyMatch(esp -> esp.name().toUpperCase().contains("SONOGRAF"));

            // Si NO es sonografista, desaparecemos el botón
            if (!esSonografista) {
                if (btnNuevaSonografia != null) {
                    btnNuevaSonografia.setVisible(false);
                    btnNuevaSonografia.setManaged(false); // Libera el espacio en la interfaz
                }
            }
        }

        // =========================================================================
        // 2. UX: Truco para Maximizar la Ventana desde su propio controlador
        // =========================================================================
        Platform.runLater(() -> {
            if (lblNombrePerfil != null && lblNombrePerfil.getScene() != null) {
                Stage stage = (Stage) lblNombrePerfil.getScene().getWindow();
                if (stage != null) {
                    stage.setMaximized(true); // ¡Pone la pantalla en gigante automáticamente!
                }
            }
        });
    }

    public void cargarDatosPaciente(UUID pacienteId) {
        this.pacienteIdActual = pacienteId;
        log.info("Descargando historial completo de BD para el paciente: {}", pacienteId);

        try {
            this.pacienteActual = pacienteService.obtenerExpedienteCompleto(pacienteId);
            this.rutaPdfHistoriaActual = this.pacienteActual.datosPersonales().rutaPdfHistoria();

            String nombre = pacienteActual.datosPersonales().nombreApellidos();
            lblNombrePerfil.setText(nombre != null ? nombre : "Nombre no registrado");

            String cedula = pacienteActual.datosPersonales().cedula();
            lblCedulaPerfil.setText("Cédula: " + (cedula != null ? cedula : "N/A"));

            String seguro = pacienteActual.datosPersonales().seguro();
            lblSeguro.setText(seguro != null && !seguro.isEmpty() ? seguro : "Sin Seguro");

            String telefono = pacienteActual.datosPersonales().telefonos();
            lblTelefono.setText(telefono != null && !telefono.isEmpty() ? telefono : "No registrado");

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
        log.info("Botón de PDF Inicial presionado.");
    }

    @FXML
    public void abrirModalNuevaEvolucion() {
        if (pacienteIdActual == null) return;

        try {
            log.info("Abriendo modal para nueva evolución...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nueva_evolucion.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();
            NuevaEvolucionController modalController = fxmlLoader.getController();
            modalController.inicializarParaPaciente(pacienteIdActual);

            // 1. PRIMERO: Instanciamos el Stage
            Stage stage = new Stage();
            stage.setTitle("Nueva Evolución Médica");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // 2. SEGUNDO: Aplicamos la magia responsiva con el nombre correcto de tu clase
            com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.configurarVentanaResponsiva(stage, root, 900, 650);

            // 3. FINALMENTE: Mostramos la ventana
            stage.showAndWait();

            log.info("Modal cerrado. Refrescando lista de evoluciones...");
            cargarEvolucionesEnPanelDerecho();

        } catch (Exception e) {
            log.error("Error al abrir el modal de evolución", e);
        }
    }

    @FXML
    public void volverAlDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void cargarEvolucionesEnPanelDerecho() {
        if (pacienteIdActual == null) return;
        log.info("Renderizando tarjetas de evolución en la UI...");
        vboxListaEvoluciones.getChildren().clear();

        try {
            var evoluciones = pacienteService.obtenerEvolucionesPorPaciente(pacienteIdActual);

            if (evoluciones.isEmpty()) {
                Label lblVacio = new Label("No hay evoluciones registradas para este paciente.");
                lblVacio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                vboxListaEvoluciones.getChildren().add(lblVacio);
                return;
            }

            java.time.format.DateTimeFormatter formatoFecha = java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy");

            for (var evo : evoluciones) {
                VBox tarjeta = new VBox(8);
                tarjeta.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                tarjeta.setPadding(new javafx.geometry.Insets(15));

                javafx.scene.layout.HBox cabecera = new javafx.scene.layout.HBox();
                cabecera.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                cabecera.setSpacing(10);

                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                Label lblFecha = new Label("Evolución del: " + evo.fecha().format(formatoFecha));
                lblFecha.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                cabecera.getChildren().addAll(lblFecha, spacer);

                if (evo.rutaPdf() != null && !evo.rutaPdf().trim().isEmpty()) {
                    javafx.scene.control.Button btnVerPdf = new javafx.scene.control.Button("📄 Ver PDF");
                    btnVerPdf.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-border-color: #bfdbfe; -fx-border-radius: 4px; -fx-cursor: hand;");
                    btnVerPdf.setOnAction(event -> abrirDocumentoPdf(evo.rutaPdf()));
                    cabecera.getChildren().add(btnVerPdf);
                }

                tarjeta.getChildren().add(cabecera);
                tarjeta.getChildren().add(new javafx.scene.control.Separator());

                agregarFilaATarjeta(tarjeta, "Motivo:", evo.motivoSeguimiento());
                agregarFilaATarjeta(tarjeta, "Historia Actual:", evo.historiaEnfermedadActual());
                agregarFilaATarjeta(tarjeta, "Diagnóstico:", evo.diagnostico());
                agregarFilaATarjeta(tarjeta, "Tratamiento:", evo.tratamiento());
                agregarFilaATarjeta(tarjeta, "Plan:", evo.plan());

                vboxListaEvoluciones.getChildren().add(tarjeta);
            }

        } catch (Exception e) {
            log.error("Error al cargar las evoluciones en la UI", e);
        }
    }

    private void agregarFilaATarjeta(VBox tarjeta, String titulo, String contenido) {
        if (contenido != null && !contenido.trim().isEmpty()) {
            VBox fila = new VBox(2);
            Label lblTitulo = new Label(titulo);
            lblTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 11px;");

            Label lblContenido = new Label(contenido);
            lblContenido.setWrapText(true);
            lblContenido.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");

            fila.getChildren().addAll(lblTitulo, lblContenido);
            tarjeta.getChildren().add(fila);
        }
    }

    private void abrirDocumentoPdf(String rutaFisica) {
        try {
            java.io.File archivoPdf = new java.io.File(rutaFisica);

            if (!archivoPdf.exists()) {
                log.error("El archivo no existe en la ruta especificada: {}", rutaFisica);
                return;
            }

            if (java.awt.Desktop.isDesktopSupported() && !java.awt.GraphicsEnvironment.isHeadless()) {
                log.info("Abriendo PDF nativamente vía Java AWT: {}", rutaFisica);
                java.awt.Desktop.getDesktop().open(archivoPdf);
            } else {
                log.info("AWT bloqueado por Spring Boot. Invocando comandos nativos del Sistema Operativo...");
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    new ProcessBuilder("cmd", "/c", "start", "", archivoPdf.getAbsolutePath()).start();
                } else if (os.contains("mac")) {
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
            abrirDocumentoPdf(this.rutaPdfHistoriaActual);
        } else {
            log.info("El paciente no tiene historia clínica base. Abriendo formulario de creación...");
            abrirFormularioHistoriaClinicaFaltante();
        }
    }

    private void abrirFormularioHistoriaClinicaFaltante() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nuevo_paciente.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();
            NuevoPacienteController controller = fxmlLoader.getController();

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

            cargarDatosPaciente(this.pacienteIdActual);

        } catch (Exception e) {
            log.error("Error al abrir el formulario de historia clínica faltante", e);
            mostrarAlerta("Error", "No se pudo abrir el formulario de historia clínica.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

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

            String medico = "Médico";
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico) {
                medico = "Dr. " + ((Medico) sesionGlobal.getUsuarioLogueado()).getNombreCompleto();
            }

            pacienteService.agregarNota(pacienteIdActual, contenido, medico);
            txtNuevaNota.clear();
            cargarHistorialNotas();
        } catch (Exception e) {
            log.error("Error al guardar nota rápida", e);
        }
    }

    @FXML
    public void abrirModalSonografiaAbdominal() {
        if (pacienteIdActual == null) return;
        try {
            log.info("Abriendo modal para Sonografía Abdominal...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sonografias/nueva_sonografia_abdominal.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            NuevaSonografiaAbdominalController modalController = fxmlLoader.getController();
            modalController.inicializarParaPaciente(pacienteIdActual);
            Stage stage = new Stage();
            stage.setTitle("Nueva Sonografía Abdominal");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarHistorialSonografias();
        } catch (Exception e) {
            log.error("Error crítico al abrir modal de sonografía abdominal", e);
            mostrarAlerta("Error de Interfaz", "No se pudo cargar la pantalla: " + e.getMessage());
        }
    }

    private void cargarHistorialSonografias() {
        if (pacienteIdActual == null || vboxListaSonografias == null) return;

        log.info("Renderizando tarjetas de sonografía en la UI...");
        vboxListaSonografias.getChildren().clear();

        try {
            var sonografias = sonografiaService.obtenerHistorialSonografias(pacienteIdActual);

            if (sonografias.isEmpty()) {
                Label lblVacio = new Label("No hay reportes sonográficos registrados para este paciente.");
                lblVacio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                vboxListaSonografias.getChildren().add(lblVacio);
                return;
            }

            java.time.format.DateTimeFormatter formatoFecha = java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy");

            // --- MAGIA SENIOR: Determinamos el prefijo visual una sola vez fuera del bucle ---
            String prefijoVisual = "Dr."; // Por defecto
            if (sesionGlobal != null && sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof com.proyectomedico.appmedicacenfasies.model.Medico doctor) {
                if ("Femenino".equalsIgnoreCase(doctor.getSexo())) {
                    prefijoVisual = "Dra.";
                }
            }

            for (var sono : sonografias) {
                VBox tarjeta = new VBox(8);
                tarjeta.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                tarjeta.setPadding(new javafx.geometry.Insets(15));

                javafx.scene.layout.HBox cabecera = new javafx.scene.layout.HBox();
                cabecera.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                cabecera.setSpacing(10);

                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                String fechaTexto = (sono.getFechaCreacion() != null) ? sono.getFechaCreacion().format(formatoFecha) : "Fecha no registrada";
                String tipoEstudio = (sono.getTipoSonografia() != null) ? sono.getTipoSonografia() : "General";

                Label lblTitulo = new Label("Ecografía " + tipoEstudio + " - " + fechaTexto);
                lblTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                cabecera.getChildren().addAll(lblTitulo, spacer);

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

                // --- APLICACIÓN DEL PREFIJO DINÁMICO ---
                // Limpiamos la base de datos al vuelo por si el nombre guardado ya traía "Dra. " o "Dr. " escrito.
                String nombreLimpio = sono.getMedicoRealizador() != null ? sono.getMedicoRealizador().replace("Dra. ", "").replace("Dr. ", "") : "Desconocido";
                String medicoFormateado = prefijoVisual + " " + nombreLimpio;

                agregarFilaATarjeta(tarjeta, "Médico encargado:", medicoFormateado);
                agregarFilaATarjeta(tarjeta, "Diagnóstico:", sono.getDiagnosticoConclusion());

                vboxListaSonografias.getChildren().add(tarjeta);
            }
        } catch (Exception e) {
            log.error("Error al cargar las sonografías en la UI", e);
        }
    }

    public void ejecutarAutoAperturaSonografia(String tipoEstudio) {
        if (tipoEstudio == null) return;
        switch (tipoEstudio.toUpperCase()) {
            case "ABDOMINAL": abrirModalSonografiaAbdominal(); break;
            case "OBSTETRICA": abrirModalSonografiaObstetrica(); break;
            case "MAMAS": abrirModalSonografiaMamas(); break;
            case "PELVICA_FEMENINA": abrirModalSonografiaPelvicaFemenina(); break;
            case "PELVICA_MASCULINA": abrirModalSonografiaPelvicaMasculina(); break;
            case "TIROIDES": abrirModalSonografiaTiroides(); break;
        }
    }

    @FXML
    public void abrirModalSonografiaMamas() {
        if (pacienteIdActual == null) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sonografias/nueva_sonografia_mamas.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            NuevaSonografiaMamasController modalController = fxmlLoader.getController();
            modalController.inicializarParaPaciente(pacienteIdActual);
            Stage stage = new Stage();
            stage.setTitle("Nueva Sonografía de Mamas");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarHistorialSonografias();
        } catch (Exception e) {
            log.error("Error crítico al abrir modal de sonografía de Mamas", e);
        }
    }

    @FXML
    public void abrirModalSonografiaPelvicaFemenina() {
        if (pacienteIdActual == null) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sonografias/nueva_sonografia_pelvica_femenina.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            NuevaSonografiaPelvicaFemeninaController modalController = fxmlLoader.getController();
            modalController.inicializarParaPaciente(pacienteIdActual);
            Stage stage = new Stage();
            stage.setTitle("Nueva Sonografía Pélvica Femenina");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarHistorialSonografias();
        } catch (Exception e) {
            log.error("Error crítico al abrir modal", e);
        }
    }

    @FXML
    public void abrirModalSonografiaObstetrica() {
        if (pacienteIdActual == null) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sonografias/nueva_sonografia_obstetrica.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            NuevaSonografiaObstetricaController modalController = fxmlLoader.getController();
            modalController.inicializarParaPaciente(pacienteIdActual);
            Stage stage = new Stage();
            stage.setTitle("Nueva Sonografía Obstétrica");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarHistorialSonografias();
        } catch (Exception e) {
            log.error("Error crítico al abrir modal", e);
        }
    }

    @FXML
    public void abrirModalSonografiaPelvicaMasculina() {
        if (pacienteIdActual == null) return;
        try {
            // ==============================================================
            // BUG CORREGIDO: Antes apuntaba a nueva_sonografia_obstetrica.fxml
            // ==============================================================
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sonografias/nueva_sonografia_pelvica_masculina.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            NuevaSonografiaPelvicaMasculinaController modalController = fxmlLoader.getController();
            modalController.inicializarParaPaciente(pacienteIdActual);
            Stage stage = new Stage();
            stage.setTitle("Nueva Sonografía Pélvica Masculina");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarHistorialSonografias();
        } catch (Exception e) {
            log.error("Error crítico al abrir modal", e);
        }
    }

    @FXML
    public void abrirModalSonografiaTiroides() {
        if (pacienteIdActual == null) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sonografias/nueva_sonografia_tiroides.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            NuevaSonografiaTiroidesController modalController = fxmlLoader.getController();
            modalController.inicializarParaPaciente(pacienteIdActual);
            Stage stage = new Stage();
            stage.setTitle("Nueva Sonografía de Tiroides");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarHistorialSonografias();
        } catch (Exception e) {
            log.error("Error crítico al abrir modal", e);
        }
    }
}
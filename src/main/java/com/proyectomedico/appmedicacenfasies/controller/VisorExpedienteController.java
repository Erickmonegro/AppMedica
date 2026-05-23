package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.dto.PacienteRegistroDTO;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.NotaMedica;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
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
    private UUID pacienteSeleccionadoId;

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
        log.info("Botón de PDF Inicial presionado.");

        if (this.rutaPdfHistoriaActual != null && !this.rutaPdfHistoriaActual.trim().isEmpty()) {
            // El paciente tiene su archivo, lo abrimos.
            abrirDocumentoPdf(this.rutaPdfHistoriaActual);
        } else {
            // El paciente no tiene archivo (Registro antiguo o fallido). Avisamos al UI.
            log.warn("Intento de abrir PDF fallido: La ruta es nula o vacía en la BD.");
            mostrarAlerta(
                    "Documento no disponible",
                    "Este paciente no tiene una Historia Clínica en PDF generada. Es posible que sea un registro antiguo anterior a la actualización del sistema."
            );
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
}
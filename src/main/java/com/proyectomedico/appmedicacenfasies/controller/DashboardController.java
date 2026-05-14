package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.dto.PacienteResumenDTO;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardController {

    private final PacienteService pacienteService;
    // ÚNICA INYECCIÓN DEL CONTEXTO DE SPRING (Cerebro de la App)
    private final ApplicationContext applicationContext;
    private final com.proyectomedico.appmedicacenfasies.service.CitaService citaService;

    @FXML private TextField txtBuscar;
    @FXML private ListView<PacienteResumenDTO> listaPacientes;
    @FXML private Button btnNuevoPaciente;
    @FXML private Button btnAgendarCita;
    @FXML private javafx.scene.layout.VBox vboxCitasDashboard;

    @FXML
    public void initialize() {
        log.info("Inicializando eventos del Dashboard...");

        configurarDisenoDeLaLista();
        cargarPacientes("");
        cargarWidgetCitas(); // <--- NUEVA LÍNEA: Carga las citas al iniciar la app

        // EVENTO: Búsqueda en tiempo real
        txtBuscar.textProperty().addListener((observable, textoViejo, textoNuevo) -> {
            cargarPacientes(textoNuevo);
        });

        // EVENTO: Botón de Nuevo Paciente
        btnNuevoPaciente.setOnAction(event -> abrirVentanaNuevoPaciente());

        // EVENTO: Agendar Cita (Pendiente para la Épica 3)
        // EVENTO: Agendar Cita
        btnAgendarCita.setOnAction(event -> abrirVentanaNuevaCita());

        // EVENTO: Doble Clic en un paciente de la lista (Nuestra Épica 1 actual)
        listaPacientes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && listaPacientes.getSelectionModel().getSelectedItem() != null) {
                PacienteResumenDTO pacienteSeleccionado = listaPacientes.getSelectionModel().getSelectedItem();
                abrirVisorExpediente(pacienteSeleccionado.id());
            }
        });
    }

    /**
     * Levanta el formulario de Historia Clínica como una ventana Modal (Bloqueante).
     */
    private void abrirVentanaNuevoPaciente() {
        try {
            log.info("Cargando vista de Nuevo Paciente...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nuevo_paciente.fxml"));

            // Magia de Spring para inyectar los servicios
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1000, 800);

            // Estilos
            scene.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Registro de Historia Clínica");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refrescamos la lista al cerrar por si hubo un registro nuevo
            cargarPacientes("");

        } catch (IOException e) {
            log.error("Error crítico al intentar abrir la ventana de Nuevo Paciente", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Abre el visor del expediente histórico del paciente seleccionado.
     */
    private void abrirVisorExpediente(UUID pacienteId) {
        log.info("Abriendo expediente del paciente con ID: {}", pacienteId);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/visor_expediente.fxml"));

            // Usamos la misma variable unificada de Spring
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            // Capturamos el controlador y le inyectamos el ID
            VisorExpedienteController visorController = loader.getController();
            visorController.cargarDatosPaciente(pacienteId);

            Stage stage = new Stage();
            stage.setTitle("Expediente Médico - CENFASIES");
            stage.setScene(new Scene(root, 1000, 700));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            log.error("Error crítico al abrir la ventana del expediente", e);
        }
    }

    // --- MÉTODOS DE UTILIDAD ---

    private void cargarPacientes(String filtro) {
        Platform.runLater(() -> {
            List<PacienteResumenDTO> resultados = pacienteService.buscarPacientes(filtro);
            listaPacientes.getItems().setAll(resultados);
        });
    }

    private void configurarDisenoDeLaLista() {
        listaPacientes.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(PacienteResumenDTO paciente, boolean empty) {
                super.updateItem(paciente, empty);
                if (empty || paciente == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(paciente.nombreApellidos() + "\nCédula: " + paciente.cedula());
                    setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-padding: 10px; -fx-border-color: transparent transparent #4a80c9 transparent; -fx-border-width: 1px;");
                }
            }
        });
    }
    /**
     * Levanta el formulario de Nueva Cita como una ventana Modal (Bloqueante).
     */
    private void abrirVentanaNuevaCita() {
        try {
            log.info("Cargando vista de Nueva Cita...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nueva_cita.fxml"));

            // Magia de Spring para inyectar los servicios (CitaService, PacienteService en el NuevaCitaController)
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            // Nota Senior: No le pongo tamaño fijo a la Scene (como 1000x800) para que
            // JavaFX envuelva la ventana al tamaño exacto de nuestro VBox en el FXML.

            Stage stage = new Stage();
            stage.setTitle("Agendar Nueva Cita - CENFASIES");
            stage.setScene(scene);

            // Regla UX: Modalidad bloqueante para que no hagan clic en el Dashboard mientras agendan
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            // Pausa la ejecución de este código hasta que la ventana se cierre
            // Pausa la ejecución de este código hasta que la ventana se cierre
            stage.showAndWait();

            log.info("Ventana de Nueva Cita cerrada.");
            cargarWidgetCitas(); // <--- NUEVA LÍNEA: Recarga el cuadro inmediatamente después de agendar

            log.info("Ventana de Nueva Cita cerrada.");
            // TODO: En nuestro próximo paso, aquí llamaremos a un método "cargarWidgetCitas()"
            // para que el Dashboard muestre la cita recién creada.

        } catch (Exception e) {
            log.error("Error crítico al intentar abrir la ventana de Nueva Cita", e);
        }
    }

    /**
     * Módulo Dashboard: Consulta la BD y dibuja las tarjetas de las próximas citas.
     */
    private void cargarWidgetCitas() {
        log.info("Actualizando widget de próximas citas...");

        // Usamos Platform.runLater por si este método es llamado desde un hilo secundario
        Platform.runLater(() -> {
            vboxCitasDashboard.getChildren().clear(); // Limpiamos la pantalla antes de recargar

            // 1. Pedimos los Datos ligeros al Service
            List<com.proyectomedico.appmedicacenfasies.dto.CitaDashboardDTO> proximasCitas = citaService.obtenerCitasParaDashboard();

            if (proximasCitas.isEmpty()) {
                javafx.scene.control.Label lblVacio = new javafx.scene.control.Label("No hay citas programadas próximamente.");
                lblVacio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 20px;");
                vboxCitasDashboard.getChildren().add(lblVacio);
                return;
            }

            // 2. Construimos una tarjeta visual para cada cita
            for (com.proyectomedico.appmedicacenfasies.dto.CitaDashboardDTO cita : proximasCitas) {
                javafx.scene.layout.VBox tarjeta = new javafx.scene.layout.VBox(5);
                tarjeta.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;");

                // Fila 1: Nombre del Paciente (Negrita)
                javafx.scene.control.Label lblPaciente = new javafx.scene.control.Label("👤 " + cita.nombrePaciente());
                lblPaciente.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 15px;");

                // Fila 2: Fecha y Hora (Azul médico)
                javafx.scene.control.Label lblFechaHora = new javafx.scene.control.Label("📅 " + cita.fecha().toString() + "  ⏰ " + cita.hora().toString());
                lblFechaHora.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");

                // Fila 3: Médico
                javafx.scene.control.Label lblMedico = new javafx.scene.control.Label("👨‍⚕️ Médico: " + cita.medicoAsignado());
                lblMedico.setStyle("-fx-text-fill: #64748b;");

                // Fila 4: Motivo
                javafx.scene.control.Label lblMotivo = new javafx.scene.control.Label("📝 Motivo: " + cita.motivo());
                lblMotivo.setStyle("-fx-text-fill: #475569; -fx-wrap-text: true;");

                tarjeta.getChildren().addAll(lblPaciente, lblFechaHora, lblMedico, lblMotivo);

                // Efecto Hover: Cambia ligeramente de color al pasar el ratón
                tarjeta.setOnMouseEntered(e -> tarjeta.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;"));
                tarjeta.setOnMouseExited(e -> tarjeta.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;"));

                // Añadimos la tarjeta completa al contenedor principal
                vboxCitasDashboard.getChildren().add(tarjeta);
            }
        });
    }
}
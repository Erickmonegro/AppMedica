package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.dto.PacienteResumenDTO;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.Turno;
import com.proyectomedico.appmedicacenfasies.repository.TurnoRepository;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardMedicoController {

    // --- DEPENDENCIAS DEL BACKEND ---
    private final PacienteService pacienteService;
    private final TurnoRepository turnoRepository;
    private final SesionGlobal sesionGlobal;
    private final ApplicationContext applicationContext;

    // --- ELEMENTOS DE LA INTERFAZ ---
    @FXML private TextField txtBuscar;
    @FXML private ListView<PacienteResumenDTO> listaPacientes; // Recuperamos tu lista original
    @FXML private VBox vboxCitasDashboard;
    @FXML private Label lblBienvenidaDoctor;

    @FXML
    public void initialize() {
        log.info("Inicializando Panel Médico...");

        // 1. Identidad del Doctor y Sala de Espera
        if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico) {
            Medico doctor = (Medico) sesionGlobal.getUsuarioLogueado();
            lblBienvenidaDoctor.setText("Dr. " + doctor.getNombreCompleto());
            cargarSalaDeEspera(doctor);
        }

        // 2. Panel Izquierdo: Configurar lista y cargar pacientes
        configurarDisenoDeLaLista();
        cargarPacientes("");

        // 3. EVENTO: Búsqueda en tiempo real
        txtBuscar.textProperty().addListener((observable, textoViejo, textoNuevo) -> {
            cargarPacientes(textoNuevo);
        });

        // 4. EVENTO: Doble Clic en la lista izquierda
        listaPacientes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && listaPacientes.getSelectionModel().getSelectedItem() != null) {
                PacienteResumenDTO pacienteSeleccionado = listaPacientes.getSelectionModel().getSelectedItem();
                abrirVisorExpediente(pacienteSeleccionado.id());
            }
        });
    }

    // ==========================================================
    // --- MAGIA NUEVA: SALA DE ESPERA CON BOTÓN INTEGRADO ---
    // ==========================================================

    private void cargarSalaDeEspera(Medico doctor) {
        Platform.runLater(() -> {
            vboxCitasDashboard.getChildren().clear();

            List<Turno> pacientesEsperando = turnoRepository
                    .findByMedicoAsignadoIdAndEstadoOrderByFechaEntradaAsc(doctor.getId(), "EN_ESPERA");

            if (pacientesEsperando.isEmpty()) {
                Label lblVacio = new Label("No hay pacientes en sala de espera.");
                lblVacio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                vboxCitasDashboard.getChildren().add(lblVacio);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

            for (Turno turno : pacientesEsperando) {
                HBox tarjeta = crearTarjetaPaciente(turno, formatter);
                vboxCitasDashboard.getChildren().add(tarjeta);
            }
        });
    }

    private HBox crearTarjetaPaciente(Turno turno, DateTimeFormatter formatter) {
        HBox tarjeta = new HBox(15);
        tarjeta.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        tarjeta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Indicador visual
        Circle indicador = new Circle(6, Color.web("#28a745"));

        // Textos del paciente
        VBox datos = new VBox(5);
        Label lblNombre = new Label("👤 " + turno.getPaciente().getNombreApellidos());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1e293b;");

        Label lblDetalles = new Label("Llegó: " + turno.getFechaEntrada().format(formatter) + " | Motivo: " + turno.getAreaDestino());
        lblDetalles.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        datos.getChildren().addAll(lblNombre, lblDetalles);

        // "Resorte" invisible para empujar el botón a la derecha
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // EL BOTÓN "ATENDER" INTEGRADO EN LA TARJETA
        Button btnAtender = new Button("Atender");
        btnAtender.setStyle("-fx-background-color: #0b4a9e; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");

        btnAtender.setOnAction(event -> {
            log.info("Atendiendo paciente: {}", turno.getPaciente().getNombreApellidos());
            decidirRutaAtencion(turno);
        });

        tarjeta.getChildren().addAll(indicador, datos, spacer, btnAtender);

        // Efecto Hover suave (opcional)
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8;"));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;"));

        return tarjeta;
    }

    // ==========================================================
    // --- MÉTODOS HEREDADOS (EL MERGE PERFECTO) ---
    // ==========================================================

    @FXML
    public void abrirNuevoPaciente() {
        try {
            log.info("Cargando vista de Nuevo Paciente...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nuevo_paciente.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1000, 800);
            scene.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Registro de Historia Clínica");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refrescamos al cerrar
            cargarPacientes("");

        } catch (IOException e) {
            log.error("Error crítico al intentar abrir la ventana de Nuevo Paciente", e);
            throw new RuntimeException(e);
        }
    }

    private void abrirVisorExpediente(UUID pacienteId) {
        log.info("Abriendo expediente del paciente con ID: {}", pacienteId);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/visor_expediente.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            VisorExpedienteController visorController = loader.getController();
            visorController.cargarDatosPaciente(pacienteId);

            Stage stage = new Stage();
            stage.setTitle("Expediente Médico - CENFASIES");
            stage.setScene(new Scene(root, 1000, 700));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error crítico al abrir la ventana del expediente", e);
        }
    }

    // Reemplaza tu antiguo cargarPacientes con este:
    private void cargarPacientes(String filtro) {
        Platform.runLater(() -> {
            // Verificamos que haya un médico logueado
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico) {
                Medico doctor = (Medico) sesionGlobal.getUsuarioLogueado();

                // Usamos el nuevo método seguro
                List<PacienteResumenDTO> resultados = pacienteService.buscarPacientesDelMedico(filtro, doctor.getId());
                listaPacientes.getItems().setAll(resultados);
            }
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

    @FXML
    public void cerrarSesion(ActionEvent event) {
        sesionGlobal.cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // MAGIA SENIOR: En lugar de usar el 'event' (que es un MenuItem),
            // usamos el 'txtBuscar' que ya está inyectado y SÍ es un Node,
            // para extraer en qué ventana estamos parados.
            Stage stage = (Stage) txtBuscar.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setTitle("Login - CENFASIES");
        } catch (Exception e) {
            log.error("Error al cerrar sesión", e);
        }
    }
    private void decidirRutaAtencion(Turno turno) {
        UUID pacienteId = turno.getPaciente().getId();

        boolean yaTieneHistoria = pacienteService.tieneHistoriaClinicaCompleta(pacienteId);

        if (yaTieneHistoria) {
            log.info("Paciente recurrente. Abriendo Visor de Expediente.");
            abrirVisorExpediente(pacienteId);
        } else {
            log.info("Paciente nuevo de recepción. Abriendo Formulario Inicial.");
            // ==========================================
            // MAGIA: Ahora le pasamos el paciente Y la especialidad (AreaDestino)
            // ==========================================
            abrirNuevoPacienteDesdeRecepcion(turno.getPaciente(), turno.getAreaDestino());
        }

        log.info("Consulta terminada. Marcando turno como ATENDIDO...");

        turno.setEstado("ATENDIDO");
        turnoRepository.save(turno);

        Medico doctor = (Medico) sesionGlobal.getUsuarioLogueado();
        cargarSalaDeEspera(doctor);
    }

    // 2. EL MÉTODO PARA ABRIR FORMULARIO PRE-LLENADO
    // 2. EL MÉTODO PARA ABRIR FORMULARIO PRE-LLENADO
    // AÑADIMOS EL SEGUNDO PARÁMETRO EN LA FIRMA DEL MÉTODO 👇
    private void abrirNuevoPacienteDesdeRecepcion(com.proyectomedico.appmedicacenfasies.model.Paciente pacienteBasico, String especialidadTurno) {
        try {
            log.info("Cargando vista de Nuevo Paciente para pre-llenado...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nuevo_paciente.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();

            // --- MAGIA: Le pasamos los datos básicos Y LA ESPECIALIDAD al formulario ---
            NuevoPacienteController controller = fxmlLoader.getController();
            controller.cargarDatosPreliminares(pacienteBasico, especialidadTurno); // <--- AQUÍ SE CONECTAN

            Scene scene = new Scene(root, 1000, 800);
            scene.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Registro de Historia Clínica - " + pacienteBasico.getNombreApellidos());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refrescamos al cerrar
            cargarPacientes("");

        } catch (Exception e) {
            log.error("Error al abrir la ventana de Nuevo Paciente desde recepción", e);
        }
    }

}
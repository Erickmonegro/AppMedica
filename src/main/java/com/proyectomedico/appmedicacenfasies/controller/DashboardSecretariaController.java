package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.dto.PacienteResumenDTO;
import com.proyectomedico.appmedicacenfasies.repository.TurnoRepository;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j // Agregado para logs de nivel empresarial
@Component
@RequiredArgsConstructor
public class DashboardSecretariaController {

    private final SesionGlobal sesionGlobal;
    private final ApplicationContext applicationContext;
    private final TurnoRepository turnoRepository;
    private final PacienteService pacienteService;

    @FXML private ListView<String> listaSalaEspera;
    @FXML private TableView<PacienteResumenDTO> tablaPacientesGlobal;
    @FXML private TableColumn<PacienteResumenDTO, String> colCedula;
    @FXML private TableColumn<PacienteResumenDTO, String> colNombre;
    @FXML private TableColumn<PacienteResumenDTO, String> colUltimaVisita;
    @FXML private TextField txtBuscador;

    @FXML
    public void initialize() {
        log.info("Inicializando Dashboard de Secretaría...");

        // 1. FORMA A PRUEBA DE BALAS PARA JAVA RECORDS
        colCedula.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().cedula())
        );

        colNombre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().nombreApellidos())
        );
        colUltimaVisita.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().ultimaVisita())
        );

        // =====================================================================
        // MAGIA UI: Arreglo del texto "Fantasma" en la lista de espera
        // =====================================================================
        listaSalaEspera.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    // Forzamos el color del texto a gris pizarra oscuro (#0f172a)
                    // y añadimos un pequeño padding para que respire.
                    setStyle("-fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-padding: 8px; -fx-background-color: transparent; -fx-border-color: transparent transparent #e2e8f0 transparent; -fx-border-width: 1px;");
                }
            }
        });

        // 2. Cargar datos iniciales
        cargarListaEspera();
        cargarTablaPacientesGlobal("");

        // 3. EVENTO: Buscador en tiempo real
        if (txtBuscador != null) {
            txtBuscador.textProperty().addListener((observable, oldValue, newValue) -> {
                cargarTablaPacientesGlobal(newValue);
            });
        }

        // 4. EVENTO: DOBLE CLIC EN LA TABLA
        tablaPacientesGlobal.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaPacientesGlobal.getSelectionModel().getSelectedItem() != null) {
                PacienteResumenDTO pacienteSeleccionado = tablaPacientesGlobal.getSelectionModel().getSelectedItem();
                abrirModalAsignacionRapida(pacienteSeleccionado.id(), pacienteSeleccionado.nombreApellidos());
            }
        });
        // =========================================================================
        // MOTOR DE SINCRONIZACIÓN ASÍNCRONA EN TIEMPO REAL (Secretaría / Recepción)
        // =========================================================================
        javafx.animation.Timeline relojSincronizador = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(6), event -> {

                    // Salvaguarda UX: Verificamos si la secretaria está buscando un paciente activo
                    String filtroActual = txtBuscador.getText() != null ? txtBuscador.getText().trim() : "";

                    // Hilo Secundario: Ejecuta las consultas pesadas a través del Wi-Fi en background
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                        var turnosEnEspera = turnoRepository.findByEstadoOrderByFechaEntradaAsc("EN_ESPERA");
                        List<String> itemsSalaEspera = turnosEnEspera.stream()
                                .map(t -> "👤 " + t.getPaciente().getNombreApellidos() + "\n👨‍⚕️ Dr. " + t.getMedicoAsignado().getNombreCompleto())
                                .toList();

                        List<PacienteResumenDTO> pacientesGlobales = filtroActual.isEmpty() ?
                                pacienteService.buscarPacientes("") : null;

                        return new Object[]{itemsSalaEspera, pacientesGlobales};
                    }).thenAcceptAsync(datos -> {

                        // Hilo de la UI: Renderizado seguro en JavaFX
                        Platform.runLater(() -> {
                            List<String> espera = (List<String>) datos[0];
                            List<PacienteResumenDTO> globales = (List<PacienteResumenDTO>) datos[1];

                            // A. Sincronizar la Sala de Espera de Recepción
                            listaSalaEspera.getItems().setAll(espera);

                            // B. Sincronizar la Tabla Global (SOLO si no está usando el buscador)
                            if (filtroActual.isEmpty() && globales != null) {
                                tablaPacientesGlobal.getItems().setAll(globales);
                            }
                        });
                    }).exceptionally(ex -> {
                        log.error("Error en la sincronización en segundo plano de secretaría", ex);
                        return null;
                    });
                })
        );
        relojSincronizador.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        relojSincronizador.play();

    }

    @FXML
    public void abrirVentanaIngresoRapido() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registro_rapido_secretaria.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();
            Stage modalStage = new Stage();
            modalStage.setTitle("Ingreso Rápido - Triaje");
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.centerOnScreen();

            modalStage.showAndWait();

            // Refrescamos la lista al cerrar el modal
            cargarListaEspera();

        } catch (Exception e) {
            log.error("Error al abrir ventana de ingreso rápido", e);
        }
    }

    private void abrirModalAsignacionRapida(UUID pacienteId, String nombreCompleto) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/asignacion_rapida.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();

            AsignacionRapidaController controller = fxmlLoader.getController();
            controller.cargarPaciente(pacienteId, nombreCompleto);

            Stage stage = new Stage();
            stage.setTitle("Asignar Turno Rápido");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

            cargarListaEspera();

        } catch (Exception e) {
            log.error("Error al abrir modal de asignación rápida", e);
        }
    }

    @FXML
    public void cerrarSesion() {
        sesionGlobal.cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) listaSalaEspera.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setTitle("Login - CENFASIES");
        } catch (Exception e) {
            log.error("Error al cerrar sesión", e);
        }
    }

    private void cargarListaEspera() {
        Platform.runLater(() -> {
            try {
                var turnosEnEspera = turnoRepository.findByEstadoOrderByFechaEntradaAsc("EN_ESPERA");
                var items = turnosEnEspera.stream()
                        .map(t -> "👤 " + t.getPaciente().getNombreApellidos() + "\n👨‍⚕️ Dr. " + t.getMedicoAsignado().getNombreCompleto())
                        .toList();

                listaSalaEspera.getItems().setAll(items);
            } catch (Exception e) {
                log.error("Error al cargar la sala de espera", e);
            }
        });
    }

    private void cargarTablaPacientesGlobal(String filtro) {
        Platform.runLater(() -> {
            try {
                List<PacienteResumenDTO> pacientes = pacienteService.buscarPacientes(filtro);
                log.info("Pacientes encontrados para la tabla global: {}", pacientes.size());
                tablaPacientesGlobal.getItems().setAll(pacientes);
            } catch (Exception e) {
                log.error("Error al cargar la tabla de pacientes", e);
            }
        });
    }
}
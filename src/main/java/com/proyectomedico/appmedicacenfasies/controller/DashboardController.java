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

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardController {

    private final PacienteService pacienteService;
    // INYECTAMOS EL CONTEXTO DE SPRING PARA CONSTRUIR NUEVAS VENTANAS
    private final ApplicationContext applicationContext;

    @FXML private TextField txtBuscar;
    @FXML private ListView<PacienteResumenDTO> listaPacientes;
    @FXML private Button btnNuevoPaciente;
    @FXML private Button btnAgendarCita;

    @FXML
    public void initialize() {
        log.info("Inicializando eventos del Dashboard...");

        configurarDisenoDeLaLista();
        cargarPacientes("");

        txtBuscar.textProperty().addListener((observable, textoViejo, textoNuevo) -> {
            cargarPacientes(textoNuevo);
        });

        // CONECTAMOS EL BOTÓN CON EL MÉTODO QUE ABRE LA VENTANA
        btnNuevoPaciente.setOnAction(event -> abrirVentanaNuevoPaciente());

        btnAgendarCita.setOnAction(event -> log.info("Abriendo calendario de citas..."));
    }

    /**
     * Levanta el formulario de Historia Clínica como una ventana Modal (Bloqueante).
     */
    private void abrirVentanaNuevoPaciente() {
        try {
            log.info("Cargando vista de Nuevo Paciente...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nuevo_paciente.fxml"));

            // MAGIA DE SPRING: Le decimos a JavaFX que use Spring para manejar el controlador
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1000, 800);

            // Aplicamos tus estilos (tanto el global como el nuevo que creaste para la hoja)
            scene.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());
            // Si llamaste a tu nuevo archivo "estiloHoja.css" y está en la carpeta css, lo agregamos así:
            // scene.getStylesheets().add(getClass().getResource("/css/estiloHoja.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Registro de Historia Clínica");
            stage.setScene(scene);

            // MODALITY.APPLICATION_MODAL: Obliga al usuario a terminar este formulario antes de volver al Dashboard
            stage.initModality(Modality.APPLICATION_MODAL);

            // showAndWait detiene la ejecución aquí hasta que se cierre la ventana
            stage.showAndWait();

            // Una vez que el médico cierra o guarda la ventana, refrescamos la lista por si hay un paciente nuevo
            cargarPacientes("");

        } catch (IOException e) {
            log.error("Error crítico al intentar abrir la ventana de Nuevo Paciente", e);
            throw new RuntimeException(e);
        }
    }

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
}
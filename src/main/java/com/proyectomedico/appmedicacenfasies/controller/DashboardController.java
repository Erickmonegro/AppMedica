package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.dto.PacienteResumenDTO;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardController {

    private final PacienteService pacienteService;

    @FXML private TextField txtBuscar;

    // Cambiamos a PacienteResumenDTO para manejar objetos reales, no solo texto plano
    @FXML private ListView<PacienteResumenDTO> listaPacientes;

    @FXML private Button btnNuevoPaciente;
    @FXML private Button btnAgendarCita;

    @FXML
    public void initialize() {
        log.info("Inicializando eventos del Dashboard...");

        // 1. Configurar cómo se ve cada "cuadrito" de paciente en la lista
        configurarDisenoDeLaLista();

        // 2. Cargar todos los pacientes al abrir la pantalla
        cargarPacientes("");

        // 3. Activar el buscador en tiempo real (Reactividad)
        txtBuscar.textProperty().addListener((observable, textoViejo, textoNuevo) -> {
            cargarPacientes(textoNuevo);
        });

        // 4. Programar los clics de los botones (Por ahora solo dejan un mensaje en consola)
        btnNuevoPaciente.setOnAction(event -> log.info("Abriendo pantalla de Nuevo Paciente..."));
        btnAgendarCita.setOnAction(event -> log.info("Abriendo calendario de citas..."));
    }

    /**
     * Consulta a la base de datos y actualiza la vista.
     */
    private void cargarPacientes(String filtro) {
        // En un hilo paralelo evitamos que la pantalla se congele mientras la base de datos responde
        Platform.runLater(() -> {
            List<PacienteResumenDTO> resultados = pacienteService.buscarPacientes(filtro);
            listaPacientes.getItems().setAll(resultados);
        });
    }

    /**
     * Le enseña a JavaFX cómo transformar un objeto PacienteResumenDTO en texto visual.
     */
    private void configurarDisenoDeLaLista() {
        listaPacientes.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(PacienteResumenDTO paciente, boolean empty) {
                super.updateItem(paciente, empty);

                if (empty || paciente == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Formato profesional: Nombre arriba, Cédula abajo
                    setText(paciente.nombreApellidos() + "\nCédula: " + paciente.cedula());
                    setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-padding: 10px; -fx-border-color: transparent transparent #4a80c9 transparent; -fx-border-width: 1px;");
                }
            }
        });
    }
}
package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.dto.PacienteResumenDTO;
import com.proyectomedico.appmedicacenfasies.repository.TurnoRepository;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;



@Component
@RequiredArgsConstructor
public class DashboardSecretariaController {

    private final SesionGlobal sesionGlobal;
    private final ApplicationContext applicationContext; // Necesario para inyectar Spring en el modal
    private final TurnoRepository turnoRepository;

    @FXML private ListView<String> listaSalaEspera;

    private final PacienteService pacienteService; // <--- Asegúrate de tener esto inyectado

    @FXML private TableView<PacienteResumenDTO> tablaPacientesGlobal;
    @FXML private TableColumn<PacienteResumenDTO, String> colCedula;
    @FXML private TableColumn<PacienteResumenDTO, String> colNombre;
    @FXML private TableColumn<PacienteResumenDTO, String> colUltimaVisita; // Opcional por ahora
    @FXML private TextField txtBuscador;// Luego definiremos el DTO para esta tabla


    @FXML
    public void initialize() {
        System.out.println("Inicializando Dashboard de Secretaría...");

        // 1. FORMA A PRUEBA DE BALAS PARA JAVA RECORDS:
        colCedula.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().cedula())
        );

        colNombre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().nombreApellidos())
        );
        colUltimaVisita.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().ultimaVisita())
        );

        // 2. Cargar datos iniciales
        cargarListaEspera();
        cargarTablaPacientesGlobal("");

        // 3. EVENTO: Buscador en tiempo real
        if (txtBuscador != null) {
            txtBuscador.textProperty().addListener((observable, oldValue, newValue) -> {
                cargarTablaPacientesGlobal(newValue);
            });
        }

        // =====================================================================
        // --- 4. NUEVO: EVENTO DOBLE CLIC EN LA TABLA ---
        // =====================================================================
        tablaPacientesGlobal.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaPacientesGlobal.getSelectionModel().getSelectedItem() != null) {
                PacienteResumenDTO pacienteSeleccionado = tablaPacientesGlobal.getSelectionModel().getSelectedItem();
                abrirModalAsignacionRapida(pacienteSeleccionado.id(), pacienteSeleccionado.nombreApellidos());
            }
        });
    }

    @FXML
    public void abrirVentanaIngresoRapido() {
        try {
            // Cargamos la vista del registro rápido (Modal)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registro_rapido_secretaria.fxml"));
            loader.setControllerFactory(applicationContext::getBean); // Conectamos con Spring Boot

            Parent root = loader.load();
            Stage modalStage = new Stage();
            modalStage.setTitle("Ingreso Rápido - Triaje");
            modalStage.setScene(new Scene(root));

            // Hacemos que la ventana sea un "Modal" (No puedes tocar el fondo hasta cerrarla)
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.centerOnScreen();
            modalStage.showAndWait();

            cargarListaEspera();

            // Cuando el modal se cierre, aquí actualizaremos la lista de "Sala de Espera"

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    // --- NUEVO MÉTODO: ABRE EL MODAL CHIQUITO DEL DOBLE CLIC ---
    // =====================================================================
    private void abrirModalAsignacionRapida(java.util.UUID pacienteId, String nombreCompleto) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/asignacion_rapida.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();

            // Pasamos los datos
            AsignacionRapidaController controller = fxmlLoader.getController();
            controller.cargarPaciente(pacienteId, nombreCompleto);

            Stage stage = new Stage();
            stage.setTitle("Asignar Turno Rápido");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refrescar tu lista de turnos (sala de espera) al cerrar el modal
            cargarListaEspera();

        } catch (Exception e) {
            System.err.println("Error al abrir modal de asignación rápida");
            e.printStackTrace();
        }
    }

    @FXML
    public void cerrarSesion() {
        sesionGlobal.cerrarSesion();

        try {
            // Volver al Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) listaSalaEspera.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setTitle("Login - CENFASIES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void cargarListaEspera() {
        javafx.application.Platform.runLater(() -> {
            // Buscamos todos los turnos que estén "EN_ESPERA"
            var turnosEnEspera = turnoRepository.findByEstadoOrderByFechaEntradaAsc("EN_ESPERA");

            // Los transformamos a un texto bonito para la lista
            var items = turnosEnEspera.stream()
                    .map(t -> "👤 " + t.getPaciente().getNombreApellidos() + "\n👨‍⚕️ Dr. " + t.getMedicoAsignado().getNombreCompleto())
                    .toList();

            listaSalaEspera.getItems().setAll(items);
        });
    }
    private void cargarTablaPacientesGlobal(String filtro) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Buscamos los pacientes
                java.util.List<com.proyectomedico.appmedicacenfasies.dto.PacienteResumenDTO> pacientes =
                        pacienteService.buscarPacientes(filtro);

                // Imprimimos en consola para asegurarnos de que la BD sí está respondiendo
                System.out.println("Pacientes encontrados para la tabla: " + pacientes.size());

                // Llenamos la tabla
                tablaPacientesGlobal.getItems().setAll(pacientes);
            } catch (Exception e) {
                System.err.println("Error al cargar la tabla de pacientes: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
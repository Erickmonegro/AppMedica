package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.model.Especialidad;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import com.proyectomedico.appmedicacenfasies.repository.MedicoRepository;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import com.proyectomedico.appmedicacenfasies.service.TurnoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistroRapidoController {

    private final MedicoRepository medicoRepository;
    private final PacienteService pacienteService;
    private final TurnoService turnoService;

    @FXML
    private TextField txtCedula;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtTelefono;

    @FXML
    private ComboBox<Especialidad> cmbEspecialidad;
    @FXML
    private ComboBox<Medico> cmbMedico;

    @FXML
    private ComboBox<String> cmbSeguro;

    private final List<String> LISTADO_ARS = Arrays.asList(
            "SENASA Contributivo", "SENASA Subsidiado", "Humano Seguros",
            "Mapfre Salud ARS", "ARS Universal", "ARS Reservas",
            "ARS Monumental", "ARS Sigma", "ARS Renacer", "Privado / Ninguno"
    );

    @FXML
    public void initialize() {
        // 1. Llenar el ComboBox del Seguro
        cmbSeguro.setItems(FXCollections.observableArrayList(LISTADO_ARS));

        // 2. Llenar el ComboBox de Especialidad con los valores de tu Enum
        cmbEspecialidad.setItems(FXCollections.observableArrayList(Especialidad.values()));

        // 2. MAGIA REACTIVA: Si el secretario cambia la especialidad, actualizamos los médicos
        cmbEspecialidad.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarMedicosPorEspecialidad(newValue);
            }
        });

        // 3. Formatear el ComboBox de Médicos para que muestre el Nombre y no un código raro de memoria
        cmbMedico.setCellFactory(param -> new ListCell<Medico>() {
            @Override
            protected void updateItem(Medico item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreCompleto());
            }
        });
        cmbMedico.setButtonCell(cmbMedico.getCellFactory().call(null));
    }

    private void cargarMedicosPorEspecialidad(Especialidad especialidad) {
        var medicos = medicoRepository.findByEspecialidad(especialidad);
        cmbMedico.setItems(FXCollections.observableArrayList(medicos));
        cmbMedico.getSelectionModel().clearSelection(); // Limpiar selección anterior
    }

    @FXML
    public void registrarYAsignar() {
        try {
            // Validaciones básicas
            if (txtCedula.getText().isEmpty() || txtNombre.getText().isEmpty() || cmbMedico.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Debe ingresar Cédula, Nombre y seleccionar un Médico.");
                return;
            }
            //Extraemos el texto del ComboBox
            String seguroSeleccionado = cmbSeguro.getEditor().getText();

            // 1. Guardar o recuperar al paciente (Datos básicos)
            Paciente paciente = pacienteService.obtenerOCrearPacienteBasico(
                    txtCedula.getText(), txtNombre.getText(), txtTelefono.getText(), seguroSeleccionado
            );
            // 2. Mandarlo a la Sala de Espera (Crear Turno)
            turnoService.crearTurnoParaPaciente(
                    paciente.getId(),
                    cmbMedico.getValue().getId(),
                    cmbEspecialidad.getValue().name() // Guardamos el nombre del área
            );

            mostrarAlerta(Alert.AlertType.CONFIRMATION, "Éxito", "Paciente enviado a sala de espera correctamente.");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error al registrar paciente rápido", e);
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error al registrar: " + e.getMessage());
        }
    }

    @FXML
    public void cerrarVentana() {
        Stage stage = (Stage) txtCedula.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
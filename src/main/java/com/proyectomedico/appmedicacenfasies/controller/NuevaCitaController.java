package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.dto.CitaRegistroDTO;
import com.proyectomedico.appmedicacenfasies.model.Paciente;
import com.proyectomedico.appmedicacenfasies.service.CitaService;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NuevaCitaController {

    private final CitaService citaService;
    private final PacienteService pacienteService;

    // --- Elementos de la UI (Asegúrate de ponerles estos fx:id en Scene Builder) ---
    @FXML private TextField txtCedulaBuscador;
    @FXML private Label lblNombrePacienteEncontrado; // Para confirmar a quién le agendamos
    @FXML private DatePicker dpFechaCita;
    @FXML private ComboBox<String> cmbHora;
    @FXML private ComboBox<String> cmbMedico;
    @FXML private TextArea txtMotivo;

    // Variable temporal para recordar al paciente que buscamos
    private UUID idPacienteSeleccionado = null;

    @FXML
    public void initialize() {
        // Llenamos los horarios disponibles (Intervalos de 30 mins)
        cmbHora.getItems().addAll(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "14:00", "14:30", "15:00", "15:30", "16:00"
        );

        // Llenamos los médicos de CENFASIES
        cmbMedico.getItems().addAll("Dr. Miguel Soler", "Dra. Ana López", "Dr. Carlos Ruiz");
    }

    /**
     * Paso 1: La recepcionista escribe la cédula y presiona un botón de "Buscar"
     */
    @FXML
    public void buscarPacientePorCedula(ActionEvent event) {
        String cedula = txtCedulaBuscador.getText().trim();
        if (cedula.isEmpty()) return;

        try {
            // Buscamos al paciente. (Asegúrate de tener un método buscarPorCedula en tu PacienteService)
            Paciente paciente = pacienteService.buscarPorCedulaExacta(cedula);

            this.idPacienteSeleccionado = paciente.getId();
            lblNombrePacienteEncontrado.setText("Paciente: " + paciente.getNombreApellidos());
            lblNombrePacienteEncontrado.setStyle("-fx-text-fill: green;");

        } catch (Exception e) {
            log.warn("No se encontró paciente con la cédula: {}", cedula);
            this.idPacienteSeleccionado = null;
            lblNombrePacienteEncontrado.setText("Paciente no encontrado");
            lblNombrePacienteEncontrado.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Paso 2: Guardar la cita médica
     */
    @FXML
    public void guardarCita(ActionEvent event) {
        log.info("Intentando agendar nueva cita...");

        // 1. Validaciones básicas de UI
        if (idPacienteSeleccionado == null) {
            mostrarAlerta("Error", "Debe buscar y seleccionar un paciente primero.");
            return;
        }
        if (dpFechaCita.getValue() == null || cmbHora.getValue() == null || cmbMedico.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor seleccione fecha, hora y médico.");
            return;
        }

        try {
            // 2. Convertir el texto de la hora a LocalTime
            LocalTime horaFormateada = LocalTime.parse(cmbHora.getValue(), DateTimeFormatter.ofPattern("HH:mm"));

            // 3. Armar el paquete de transporte (DTO)
            CitaRegistroDTO dto = new CitaRegistroDTO(
                    dpFechaCita.getValue(),
                    horaFormateada,
                    cmbMedico.getValue(),
                    txtMotivo.getText()
            );

            // 4. Delegar la responsabilidad al cerebro (Service)
            citaService.programarNuevaCita(idPacienteSeleccionado, dto);

            mostrarAlerta("Éxito", "La cita se ha agendado correctamente.");
            limpiarFormulario();

            // Opcional: Cerrar la ventana modal aquí

        } catch (IllegalStateException | IllegalArgumentException e) {
            // Atrapamos las reglas de negocio (Colisiones, Fechas pasadas) que configuramos en el Service
            mostrarAlerta("No se pudo agendar", e.getMessage());
            log.warn("Regla de negocio no cumplida: {}", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta("Error Crítico", "Ocurrió un error inesperado al guardar la cita.");
            log.error("Error al guardar cita", e);
        }
    }

    private void limpiarFormulario() {
        txtCedulaBuscador.clear();
        lblNombrePacienteEncontrado.setText("");
        dpFechaCita.setValue(null);
        cmbHora.setValue(null);
        cmbMedico.setValue(null);
        txtMotivo.clear();
        this.idPacienteSeleccionado = null;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
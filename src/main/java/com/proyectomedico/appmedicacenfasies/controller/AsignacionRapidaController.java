package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.model.Especialidad;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.repository.MedicoRepository;
import com.proyectomedico.appmedicacenfasies.service.TurnoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsignacionRapidaController {

    private final MedicoRepository medicoRepository;
    private final TurnoService turnoService;

    @FXML private Label lblNombrePaciente;
    @FXML private ComboBox<String> cmbMotivo;
    @FXML private ComboBox<Especialidad> cmbEspecialidad;
    @FXML private ComboBox<Medico> cmbMedico;
    @FXML private VBox vboxTipoEstudio;
    @FXML private ComboBox<String> cmbTipoEstudio;

    private UUID pacienteIdActual;

    @FXML
    public void initialize() {
        // 1. Opciones Predefinidas de Motivo
        cmbMotivo.setItems(FXCollections.observableArrayList(
                "Consulta de Rutina",
                "Entrega de Resultados",
                "Sonografía",
                "Chequeo de Presión/Azúcar",
                "Procedimiento Menor",
                "Emergencia"
        ));

        // 2. Llenar Especialidades
        cmbEspecialidad.setItems(FXCollections.observableArrayList(Especialidad.values()));

        // 3. Cascada: Especialidad -> Médicos
        cmbEspecialidad.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                var medicos = medicoRepository.findByEspecialidad(newVal);
                cmbMedico.setItems(FXCollections.observableArrayList(medicos));
                cmbMedico.getSelectionModel().clearSelection();
            }
        });

        // 4. Formatear lista de médicos
        cmbMedico.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Medico item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreCompleto());
            }
        });
        cmbMedico.setButtonCell(cmbMedico.getCellFactory().call(null));

        cmbTipoEstudio.getItems().addAll(
                "ABDOMINAL",
                "OBSTETRICA",
                "MAMAS",
                "PELVICA_FEMENINA",
                "PELVICA_MASCULINA"
        );

        // B. EL ESCUCHADOR MÁGICO (Listener)
        cmbEspecialidad.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Aquí conviertes el valor seleccionado a String para validarlo.
                // Si usas Enums, podría ser newValue.name().equals("SONOGRAFISTA")
                String especialidadSeleccionada = newValue.toString().toUpperCase();

                if (especialidadSeleccionada.contains("SONOGRAF")) {
                    // Mostrar menú de estudios
                    vboxTipoEstudio.setVisible(true);
                    vboxTipoEstudio.setManaged(true);
                } else {
                    // Ocultar menú y limpiar selección
                    vboxTipoEstudio.setVisible(false);
                    vboxTipoEstudio.setManaged(false);
                    cmbTipoEstudio.getSelectionModel().clearSelection();
                }

                // C. RECARGAR MÉDICOS (Para que filtre a los que tienen esa especialidad en su List/Set)
                // cargarMedicosPorEspecialidad(newValue); // Debes crear/adaptar este método
            }
        });
    }

    // El Dashboard de la secretaria llama a este método al hacer doble clic
    public void cargarPaciente(UUID pacienteId, String nombreCompleto) {
        this.pacienteIdActual = pacienteId;
        lblNombrePaciente.setText("Paciente: " + nombreCompleto);
    }

    @FXML
    public void asignarTurno() {
        // 1. Validación original
        if (cmbMotivo.getValue() == null || cmbMedico.getValue() == null || cmbEspecialidad.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleccione el motivo, la especialidad y el médico.");
            alert.showAndWait();
            return;
        }

        // =====================================================================
        // 2. NUEVA VALIDACIÓN: CAPTURAR EL TIPO DE ESTUDIO (Si es Sonografía)
        // =====================================================================
        String tipoDeEstudio = "CONSULTA_ESTANDAR"; // Valor por defecto si va a Medicina General

        // Asumiendo que el VBox oculto de sonografías se llama vboxTipoEstudio
        if (vboxTipoEstudio.isVisible()) {
            if (cmbTipoEstudio.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Debe seleccionar el tipo exacto de Sonografía.");
                alert.showAndWait();
                return;
            }
            tipoDeEstudio = cmbTipoEstudio.getValue(); // Ej: "ABDOMINAL", "MAMAS", etc.
        }

        try {
            // Unimos el motivo y la especialidad para que el doctor lo vea claro
            String areaYMotivo = cmbEspecialidad.getValue().name() + " - " + cmbMotivo.getValue();

            // 3. ¡AQUÍ PASAMOS EL CUARTO PARÁMETRO AL SERVICIO!
            turnoService.crearTurnoParaPaciente(
                    pacienteIdActual,
                    cmbMedico.getValue().getId(),
                    areaYMotivo,
                    tipoDeEstudio // <--- ENVIAMOS EL DATO RECIÉN CAPTURADO
            );

            cerrarVentana();
        } catch (Exception e) {
            log.error("Error al asignar turno rápido", e);
        }
    }

    @FXML
    public void cerrarVentana() {
        Stage stage = (Stage) cmbMotivo.getScene().getWindow();
        stage.close();
    }
}
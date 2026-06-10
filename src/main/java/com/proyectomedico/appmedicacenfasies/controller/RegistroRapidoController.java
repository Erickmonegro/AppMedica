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
import javafx.scene.layout.VBox;
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
    private CheckBox chkExtranjero;
    @FXML
    private TextField txtCedula;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtTelefono;

    @FXML
    private ComboBox<String> cmbSexo;
    @FXML
    private DatePicker dpFechaNacimiento;
    @FXML
    private ComboBox<Especialidad> cmbEspecialidad;
    @FXML
    private ComboBox<Medico> cmbMedico;

    @FXML
    private ComboBox<String> cmbSeguro;

    @FXML private VBox vboxTipoEstudio;
    @FXML private ComboBox<String> cmbTipoEstudio;
    @FXML
    private ComboBox<String> cmbMotivo; // <--- NUEVA VARIABLE


    private final List<String> LISTADO_ARS = Arrays.asList(
            "SENASA Contributivo", "SENASA Subsidiado", "Humano Seguros",
            "Mapfre Salud ARS", "ARS Universal", "ARS Reservas",
            "ARS Monumental", "ARS Sigma", "ARS Renacer", "Privado / Ninguno"
    );

    @FXML
    public void initialize() {

        // 1. Opciones Predefinidas de Motivo (Igual que Asignación Rápida)
        cmbMotivo.setItems(FXCollections.observableArrayList(
                "Consulta de Rutina",
                "Entrega de Resultados",
                "Sonografía",
                "Chequeo de Presión/Azúcar",
                "Procedimiento Menor",
                "Emergencia"
        ));

        cmbTipoEstudio.getItems().addAll(
                "ABDOMINAL",
                "OBSTETRICA",
                "MAMAS",
                "PELVICA_FEMENINA",
                "PELVICA_MASCULINA"
        );

        // B. EL ESCUCHADOR MÁGICO (Listener) para el combobox de especialidad
        cmbEspecialidad.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String especialidadSeleccionada = newValue.toString().toUpperCase();

                // Si la secretaria eligió algo que contiene "SONOGRAF"
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
            }
        });
        // 1. Llenar el ComboBox del Seguro
        cmbSeguro.setItems(FXCollections.observableArrayList(LISTADO_ARS));

        if (cmbSexo != null) {
            cmbSexo.setItems(javafx.collections.FXCollections.observableArrayList(
                    "Masculino",
                    "Femenino"
            ));
        }

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
        // Activamos la magia de los guiones
        // Activamos la magia de los guiones (Llamando a la utilidad global)
        com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.aplicarFormatoCedula(txtCedula, chkExtranjero);
        com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.aplicarFormatoTelefono(txtTelefono);

        // Si marca "Extranjero", cambiamos el texto de ayuda visual
        if (chkExtranjero != null) {
            chkExtranjero.selectedProperty().addListener((obs, oldVal, esExtranjero) -> {
                if (esExtranjero) {
                    txtCedula.setPromptText("Pasaporte / Doc. Identidad");
                } else {
                    txtCedula.setPromptText("XXX-XXXXXXX-X");
                }
            });
        }
    }

    private void cargarMedicosPorEspecialidad(Especialidad especialidad) {
        log.info("Buscando médicos para la especialidad: {}", especialidad);
        var medicos = medicoRepository.findByEspecialidad(especialidad);
        log.info("Médicos encontrados: {}", medicos.size());
        cmbMedico.setItems(FXCollections.observableArrayList(medicos));
        cmbMedico.getSelectionModel().clearSelection();
    }

    @FXML
    public void registrarYAsignar() {
        try {
            // 1. Validaciones básicas incluyendo el NUEVO MOTIVO
            if (txtNombre.getText().isEmpty() || cmbMedico.getValue() == null || cmbEspecialidad.getValue() == null || cmbMotivo.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Debe ingresar Cédula, Nombre, Especialidad, Motivo y Médico.");
                return;
            }

            // 2. CAPTURAR EL TIPO DE ESTUDIO (Si es Sonografía)
            String tipoDeEstudio = "CONSULTA_ESTANDAR";

            if (vboxTipoEstudio != null && vboxTipoEstudio.isVisible()) {
                if (cmbTipoEstudio.getValue() == null) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Debe seleccionar el tipo exacto de Sonografía.");
                    return;
                }
                tipoDeEstudio = cmbTipoEstudio.getValue();
            }

            // 3. PREPARACIÓN DE CÉDULA:
            String cedulaFinal = txtCedula.getText().trim();
            if (cedulaFinal.isEmpty()) {
                cedulaFinal = null;
            }

            String seguroSeleccionado = cmbSeguro.getEditor().getText();

            // 4. Guardar o recuperar al paciente
            Paciente paciente = pacienteService.obtenerOCrearPacienteBasico(
                    cedulaFinal,
                    txtNombre.getText(),
                    txtTelefono.getText(),
                    seguroSeleccionado,
                    dpFechaNacimiento.getValue(),
                    cmbSexo.getValue()
            );

            // =================================================================
            // MAGIA: Unimos Especialidad + Motivo (Igual que Asignación Rápida)
            // =================================================================
            String areaYMotivo = cmbEspecialidad.getValue().name() + " - " + cmbMotivo.getValue();

            // 5. Mandarlo a la Sala de Espera
            turnoService.crearTurnoParaPaciente(
                    paciente.getId(),
                    cmbMedico.getValue().getId(),
                    areaYMotivo, // <--- Mandamos el texto combinado a la BD
                    tipoDeEstudio
            );

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Paciente enviado a sala de espera correctamente.");
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

    /**
     * Auto-formatea la cédula a XXX-XXXXXXX-X si es dominicano.
     */


    /**
     * Auto-formatea el teléfono a XXX-XXX-XXXX
     */

}
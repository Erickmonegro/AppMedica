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



    private final List<String> LISTADO_ARS = Arrays.asList(
            "SENASA Contributivo", "SENASA Subsidiado", "Humano Seguros",
            "Mapfre Salud ARS", "ARS Universal", "ARS Reservas",
            "ARS Monumental", "ARS Sigma", "ARS Renacer", "Privado / Ninguno"
    );

    @FXML
    public void initialize() {

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
        configurarFormatoCedula();
        configurarFormatoTelefono();

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
            // Validaciones básicas
            if (txtNombre.getText().isEmpty() || cmbMedico.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Debe ingresar Cédula, Nombre y seleccionar un Médico.");
                return;
            }

            // =====================================================================
            // NUEVA VALIDACIÓN: CAPTURAR EL TIPO DE ESTUDIO (Si es Sonografía)
            // =====================================================================
            String tipoDeEstudio = "CONSULTA_ESTANDAR";

            if (vboxTipoEstudio != null && vboxTipoEstudio.isVisible()) {
                if (cmbTipoEstudio.getValue() == null) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Debe seleccionar el tipo exacto de Sonografía.");
                    return;
                }
                tipoDeEstudio = cmbTipoEstudio.getValue();
            }

            // 2. PREPARACIÓN DE CÉDULA:
            String cedulaFinal = txtCedula.getText().trim();
            if (cedulaFinal.isEmpty()) {
                cedulaFinal = null;
            }

            // Extraemos el texto del ComboBox
            String seguroSeleccionado = cmbSeguro.getEditor().getText();

            // 1. Guardar o recuperar al paciente (Datos básicos)
            Paciente paciente = pacienteService.obtenerOCrearPacienteBasico(
                    cedulaFinal, // ¡Corregido! Usamos la cédula procesada para evitar errores en BD
                    txtNombre.getText(),
                    txtTelefono.getText(),
                    seguroSeleccionado,
                    dpFechaNacimiento.getValue(),
                    cmbSexo.getValue()
            );

            // 2. Mandarlo a la Sala de Espera (Crear Turno con 4 parámetros)
            turnoService.crearTurnoParaPaciente(
                    paciente.getId(),
                    cmbMedico.getValue().getId(),
                    cmbEspecialidad.getValue().name(), // Guardamos el nombre del área
                    tipoDeEstudio // <--- EL CUARTO PARÁMETRO ESPERADO POR EL SERVICIO
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
    private void configurarFormatoCedula() {
        txtCedula.textProperty().addListener((observable, oldValue, newValue) -> {
            // Si es extranjero, le permitimos escribir libremente (pasaportes tienen letras)
            if (chkExtranjero != null && chkExtranjero.isSelected()) return;
            if (newValue == null) return;

            // 1. Extraemos solo los números
            String numeros = newValue.replaceAll("[^\\d]", "");

            // 2. Limitamos a 11 dígitos máximo
            if (numeros.length() > 11) numeros = numeros.substring(0, 11);

            // 3. Ensamblamos con los guiones
            StringBuilder formateado = new StringBuilder();
            for (int i = 0; i < numeros.length(); i++) {
                if (i == 3 || i == 10) formateado.append("-");
                formateado.append(numeros.charAt(i));
            }

            // 4. Actualizamos el campo de texto (evitando loops infinitos)
            if (!newValue.equals(formateado.toString())) {
                txtCedula.setText(formateado.toString());
            }
        });
    }

    /**
     * Auto-formatea el teléfono a XXX-XXX-XXXX
     */
    private void configurarFormatoTelefono() {
        txtTelefono.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            String numeros = newValue.replaceAll("[^\\d]", "");
            if (numeros.length() > 10) numeros = numeros.substring(0, 10);

            StringBuilder formateado = new StringBuilder();
            for (int i = 0; i < numeros.length(); i++) {
                if (i == 3 || i == 6) formateado.append("-");
                formateado.append(numeros.charAt(i));
            }

            if (!newValue.equals(formateado.toString())) {
                txtTelefono.setText(formateado.toString());
            }
        });
    }
}
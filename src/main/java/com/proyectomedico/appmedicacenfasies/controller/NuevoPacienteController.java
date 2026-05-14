package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.dto.*;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
public class NuevoPacienteController {

    // Solo inyectamos el PacienteService. ¡El controlador es flaco y limpio!
    private final PacienteService pacienteService;
    @FXML
    private ComboBox<String> cmbSeguro;

    private final List<String> LISTADO_ARS = Arrays.asList(
            "SENASA Contributivo", "SENASA Subsidiado", "Humano Seguros",
            "Mapfre Salud ARS", "ARS Universal", "ARS Reservas",
            "ARS Monumental", "ARS Sigma", "ARS Renacer", "Privado / Ninguno"
    );

    // --- SECCIÓN 1: DATOS PERSONALES ---
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtCedula;
    @FXML
    private DatePicker dpFechaNacimiento;
    @FXML
    private TextField txtTelefonos;
    @FXML
    private TextField txtOcupacion;


    @FXML
    private TextField txtDireccion;

    // --- SECCIÓN 2: ANTECEDENTES ---
    @FXML
    private TextArea txtAntecedentesFam;
    @FXML
    private TextArea txtAntecedentesPers;
    @FXML
    private TextArea txtAlergias;
    @FXML
    private TextArea txtCirugias;
    @FXML
    private TextArea txtTransfusiones;


    // --- SECCIÓN 3: HÁBITOS TÓXICOS ---
    @FXML
    private CheckBox chkFuma;
    @FXML
    private CheckBox chkAlcohol;
    @FXML
    private CheckBox chkCafe;
    @FXML
    private CheckBox chkHooka;
    @FXML
    private CheckBox chkVape;
    @FXML
    private CheckBox chkDrogas;

    @FXML
    private TextField txtMotivoConsulta;
    @FXML
    private TextArea txtHistoriaEnfermedad;


    // --- SECCIÓN 4: EXAMEN FÍSICO Y SIGNOS VITALES ---
    @FXML
    private TextField txtPeso, txtTalla, txtTA, txtFC, txtFR, txtTemp;
    @FXML
    private TextArea txtCabeza, txtCuello, txtTorax, txtCorazon, txtPulmones, txtAbdomen;
    @FXML
    private TextArea txtMiembrosSup, txtMiembrosInf, txtGenitales, txtPiel, txtHallazgosGen;

    // --- SECCIÓN 5: CONCLUSIÓN ---
    @FXML
    private TextArea txtEstudios, txtDiagnostico, txtTratamiento;

    @FXML
    public void initialize() {
        log.info("Pantalla de Registro cargada con todas las secciones.");
        cmbSeguro.setItems(FXCollections.observableArrayList(LISTADO_ARS));
    }

    @FXML
    public void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    public void guardarPaciente(ActionEvent event) {
        log.info("Empaquetando datos del formulario en DTOs modulares...");

        try {
            // =====================================================================
            // --- 1. CAPTURAR DATOS DE LA INTERFAZ Y CREAR LOS "CAJONES" (DTOs) ---
            // =====================================================================

            // Sección 1: Datos Personales
            // Extraemos el texto del nuevo ComboBox
            String seguroSeleccionado = cmbSeguro.getEditor().getText();

            // Sección 1: Datos Personales
            DatosPersonalesDTO datosPersonales = new DatosPersonalesDTO(
                    txtNombre.getText(), txtCedula.getText(), dpFechaNacimiento.getValue(),
                    "", "", "", txtDireccion.getText(), txtTelefonos.getText(),
                    "", seguroSeleccionado, txtOcupacion.getText(), // <--- CORREGIDO
                    null
            );

            // Sección 2: Motivo y Enfermedad Actual (NUESTRO NUEVO BLOQUE)
            HistoriaEnfermedadDTO historia = new HistoriaEnfermedadDTO(
                    txtMotivoConsulta.getText(),
                    txtHistoriaEnfermedad.getText()
            );

            // Sección 3: Antecedentes
            AntecedentesDTO antecedentes = new AntecedentesDTO(
                    txtAntecedentesFam.getText(), txtAntecedentesPers.getText(),
                    txtTransfusiones.getText(), txtCirugias.getText(), txtAlergias.getText()
            );

            // Sección 4: Hábitos Tóxicos
            HabitosToxicosDTO habitos = new HabitosToxicosDTO(
                    chkFuma.isSelected(), chkAlcohol.isSelected(), chkHooka.isSelected(),
                    chkVape.isSelected(), chkCafe.isSelected(), chkDrogas.isSelected()
            );

            // Sección 5 y 6: Examen Físico y Conclusión
            ExamenFisicoDTO examenFisico = new ExamenFisicoDTO(
                    parsearDoble(txtPeso.getText()), parsearDoble(txtTalla.getText()), txtTA.getText(),
                    parsearDoble(txtFC.getText()), parsearDoble(txtFR.getText()), parsearDoble(txtTemp.getText()),
                    txtCabeza.getText(), txtCuello.getText(), txtTorax.getText(), txtCorazon.getText(),
                    txtPulmones.getText(), txtAbdomen.getText(), txtGenitales.getText(), txtMiembrosSup.getText(),
                    txtMiembrosInf.getText(), txtPiel.getText(), txtHallazgosGen.getText(),
                    txtEstudios.getText(), txtDiagnostico.getText(), txtTratamiento.getText()
            );

            // =====================================================================
            // --- 2. ENSAMBLAR EL DTO MAESTRO ---
            // =====================================================================
            PacienteRegistroDTO nuevoRegistro = new PacienteRegistroDTO(
                    datosPersonales,
                    historia,       // <--- Aquí incrustamos el nuevo cajón
                    antecedentes,
                    habitos,
                    examenFisico
            );

            log.info("DTO construido con éxito para el paciente: {}", nuevoRegistro.datosPersonales().nombreApellidos());

            // =====================================================================
            // --- 3. ENVIAR AL BACKEND (SERVICE) ---
            // =====================================================================
            String rutaPdfGenerado = pacienteService.registrarNuevaHistoriaClinica(nuevoRegistro);

            // =====================================================================
            // --- 4. RESPUESTA VISUAL AL USUARIO ---
            // =====================================================================
            if (rutaPdfGenerado != null && !rutaPdfGenerado.trim().isEmpty()) {
                java.io.File archivoPdf = new java.io.File(rutaPdfGenerado);
                if (java.awt.Desktop.isDesktopSupported() && archivoPdf.exists()) {
                    java.awt.Desktop.getDesktop().open(archivoPdf);
                    log.info("PDF abierto exitosamente en la máquina local.");
                }
            } else {
                log.warn("El paciente se guardó en BD, pero hubo un problema al generar el archivo PDF.");
            }

            // Cerramos la ventana de registro
            cerrarVentana(event);

        } catch (Exception e) {
            log.error("Error crítico al procesar los datos del formulario.", e);
            // TODO: En una futura refactorización, pondremos una alerta visual aquí.
        }
    }

    /**
     * Método utilitario de Senior: Evita que la app explote si el campo de peso/talla está vacío
     */
    private Double parsearDoble(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            log.warn("No se pudo parsear el valor numérico: {}. Se asignará 0.0", valor);
            return 0.0;
        }
    }

    /**
     * Este método es llamado por el Dashboard del Médico cuando el paciente
     * viene de la Sala de Espera (Recepción). Pre-llena los campos básicos.
     */
    /**
     * Este método es llamado por el Dashboard del Médico cuando el paciente
     * viene de la Sala de Espera (Recepción). Pre-llena los campos básicos.
     */
    public void cargarDatosPreliminares(com.proyectomedico.appmedicacenfasies.model.Paciente paciente) {
        if (paciente != null) {
            txtCedula.setText(paciente.getCedula());
            txtNombre.setText(paciente.getNombreApellidos());

            if (paciente.getTelefonos() != null) {
                txtTelefonos.setText(paciente.getTelefonos());
            }

            // --- AQUÍ ESTÁ LA CORRECCIÓN ---
            // Solo usamos el ComboBox (cmbSeguro), NUNCA txtSeguro
            if (paciente.getSeguro() != null) {
                cmbSeguro.getEditor().setText(paciente.getSeguro());
            }

            // Bloqueamos la cédula y nombre por seguridad
            txtCedula.setEditable(false);
            txtNombre.setEditable(false);
        }
    }
}
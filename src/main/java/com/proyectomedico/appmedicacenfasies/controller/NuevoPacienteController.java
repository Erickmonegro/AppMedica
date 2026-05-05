package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.dto.*;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NuevoPacienteController {

    // Solo inyectamos el PacienteService. ¡El controlador es flaco y limpio!
    private final PacienteService pacienteService;

    // --- SECCIÓN 1: DATOS PERSONALES ---
    @FXML private TextField txtNombre;
    @FXML private TextField txtCedula;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtTelefonos;
    @FXML private TextField txtOcupacion;
    @FXML private TextField txtSeguro;
    @FXML private TextField txtDireccion;

    // --- SECCIÓN 2: ANTECEDENTES ---
    @FXML private TextArea txtAntecedentesFam;
    @FXML private TextArea txtAntecedentesPers;
    @FXML private TextArea txtAlergias;
    @FXML private TextArea txtCirugias;

    // --- SECCIÓN 3: HÁBITOS TÓXICOS ---
    @FXML private CheckBox chkFuma;
    @FXML private CheckBox chkAlcohol;
    @FXML private CheckBox chkCafe;
    @FXML private CheckBox chkHooka;
    @FXML private CheckBox chkVape;
    @FXML private CheckBox chkDrogas;

    // --- SECCIÓN 4: EXAMEN FÍSICO Y SIGNOS VITALES ---
    @FXML private TextField txtPeso, txtTalla, txtTA, txtFC, txtFR, txtTemp;
    @FXML private TextArea txtCabeza, txtCuello, txtTorax, txtCorazon, txtPulmones, txtAbdomen;
    @FXML private TextArea txtMiembrosSup, txtMiembrosInf, txtGenitales, txtPiel, txtHallazgosGen;

    // --- SECCIÓN 5: CONCLUSIÓN ---
    @FXML private TextArea txtEstudios, txtDiagnostico, txtTratamiento;

    @FXML
    public void initialize() {
        log.info("Pantalla de Registro cargada con todas las secciones.");
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
            // 1. Armamos los bloques (Exactamente igual que tu lógica original)
            DatosPersonalesDTO datosPersonales = new DatosPersonalesDTO(
                    txtNombre.getText(), txtCedula.getText(), dpFechaNacimiento.getValue(),
                    "", "", "", txtDireccion.getText(), txtTelefonos.getText(),
                    "", txtSeguro.getText(), txtOcupacion.getText(),
                    null
            );

            AntecedentesDTO antecedentes = new AntecedentesDTO(
                    txtAntecedentesFam.getText(), txtAntecedentesPers.getText(),
                    "", "", txtCirugias.getText(), txtAlergias.getText()
            );

            HabitosToxicosDTO habitos = new HabitosToxicosDTO(
                    chkFuma.isSelected(), chkAlcohol.isSelected(), chkHooka.isSelected(),
                    chkVape.isSelected(), chkCafe.isSelected(), chkDrogas.isSelected()
            );

            ExamenFisicoDTO examenFisico = new ExamenFisicoDTO(
                    parsearDoble(txtPeso.getText()), parsearDoble(txtTalla.getText()), txtTA.getText(),
                    parsearDoble(txtFC.getText()), parsearDoble(txtFR.getText()), parsearDoble(txtTemp.getText()),
                    txtCabeza.getText(), txtCuello.getText(), txtTorax.getText(), txtCorazon.getText(),
                    txtPulmones.getText(), txtAbdomen.getText(), txtGenitales.getText(), txtMiembrosSup.getText(),
                    txtMiembrosInf.getText(), txtPiel.getText(), txtHallazgosGen.getText(),
                    txtEstudios.getText(), txtDiagnostico.getText(), txtTratamiento.getText()
            );

            PacienteRegistroDTO nuevoRegistro = new PacienteRegistroDTO(
                    datosPersonales, antecedentes, habitos, examenFisico
            );

            log.info("DTO construido con éxito para el paciente: {}", nuevoRegistro.datosPersonales().nombreApellidos());

            // 2. MAGIA DE ARQUITECTURA: Un solo llamado al Service.
            // El Service guarda en BD, gestiona carpetas, crea el PDF y nos devuelve la ruta.
            String rutaPdfGenerado = pacienteService.registrarNuevaHistoriaClinica(nuevoRegistro);

            // 3. RESPUESTA VISUAL: Abrimos el PDF si la ruta es válida
            if (rutaPdfGenerado != null && !rutaPdfGenerado.trim().isEmpty()) {
                java.io.File archivoPdf = new java.io.File(rutaPdfGenerado);
                if (java.awt.Desktop.isDesktopSupported() && archivoPdf.exists()) {
                    java.awt.Desktop.getDesktop().open(archivoPdf);
                    log.info("PDF abierto exitosamente en la máquina local.");
                }
            } else {
                log.warn("El paciente se guardó en BD, pero hubo un problema al generar el archivo PDF.");
            }

            // 4. Cerramos la ventana de registro
            cerrarVentana(event);

        } catch (Exception e) {
            log.error("Error crítico al procesar los datos del formulario.", e);
            // Aquí puedes agregar un Alert de JavaFX para avisarle al usuario
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
}
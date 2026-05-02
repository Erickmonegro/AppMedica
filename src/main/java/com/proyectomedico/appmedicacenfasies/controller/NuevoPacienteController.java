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

    // Debajo de tus declaraciones de @FXML...
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
            // 1. Armamos el bloque de Datos Personales
            DatosPersonalesDTO datosPersonales = new DatosPersonalesDTO(
                    txtNombre.getText(),
                    txtCedula.getText(),
                    dpFechaNacimiento.getValue(),
                    "", // sexo (Pendiente en UI MVP)
                    "", // tipoSangre (Pendiente en UI MVP)
                    "", // estadoCivil (Pendiente en UI MVP)
                    txtDireccion.getText(),
                    txtTelefonos.getText(),
                    "", // contactoEmergencia (Pendiente en UI MVP)
                    txtSeguro.getText(),
                    txtOcupacion.getText()
            );

            // 2. Armamos el bloque de Antecedentes
            AntecedentesDTO antecedentes = new AntecedentesDTO(
                    txtAntecedentesFam.getText(),
                    txtAntecedentesPers.getText(),
                    "", // personales no patologicos
                    "", // transfusiones
                    txtCirugias.getText(),
                    txtAlergias.getText()
            );

            // 3. Armamos el bloque de Hábitos Tóxicos
            HabitosToxicosDTO habitos = new HabitosToxicosDTO(
                    chkFuma.isSelected(),
                    chkAlcohol.isSelected(),
                    chkHooka.isSelected(),
                    chkVape.isSelected(),
                    chkCafe.isSelected(),
                    chkDrogas.isSelected()
            );

            // 4. Armamos el bloque de Consulta Médica (Examen Físico)
            ExamenFisicoDTO examenFisico = new ExamenFisicoDTO(
                    parsearDoble(txtPeso.getText()),
                    parsearDoble(txtTalla.getText()),
                    txtTA.getText(),
                    parsearDoble(txtFC.getText()),
                    parsearDoble(txtFR.getText()),
                    parsearDoble(txtTemp.getText()),
                    txtCabeza.getText(),
                    txtCuello.getText(),
                    txtTorax.getText(),
                    txtCorazon.getText(),
                    txtPulmones.getText(),
                    txtAbdomen.getText(),
                    txtGenitales.getText(),
                    txtMiembrosSup.getText(),
                    txtMiembrosInf.getText(),
                    txtPiel.getText(),
                    txtHallazgosGen.getText(),
                    txtEstudios.getText(),
                    txtDiagnostico.getText(),
                    txtTratamiento.getText()
            );

            // 5. Unimos todo en el DTO Maestro
            PacienteRegistroDTO nuevoRegistro = new PacienteRegistroDTO(
                    datosPersonales,
                    antecedentes,
                    habitos,
                    examenFisico
            );

            // Fíjate cómo ahora navegamos al nombre a través de datosPersonales()
            log.info("DTO construido con éxito para el paciente: {}", nuevoRegistro.datosPersonales().nombreApellidos());

            pacienteService.registrarNuevaHistoriaClinica(nuevoRegistro);
            // TODO: Enviar al PacienteService y cerrar la ventana
            cerrarVentana(event);

        } catch (Exception e) {
            log.error("Error al procesar los datos del formulario: Revisa los campos numéricos.", e);
        }
    }
    /**
     * Método utilitario de Senior: Evita que la app explote si el campo de peso/talla está vacío
     */
    private Double parsearDoble(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(valor.trim());
    }
}
package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.dto.*;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
    @FXML private VBox vboxMamas, vboxExploracionGineco;
    @FXML private TextArea txtMamas, txtEspeculoscopia, txtTactoVaginal;
    // Nuevas variables inyectadas
    @FXML private ComboBox<String> cmbSexo; // Asegúrate de añadir esto también a tus datos personales
    @FXML private VBox vboxGinecologia;
    @FXML private TextField txtMenarquia, txtGPCA;
    @FXML private DatePicker dpFUM;

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
    private TextField txtPeso, txtTalla, txtTA, txtFC, txtFR, txtTemp, txtIMC, txtSpO2;
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

        // Lógicas de auto-cálculo
        FormatoClinicoUtil.configurarCalculoIMC(txtPeso, txtTalla, txtIMC);
        FormatoClinicoUtil.configurarFormatoTA(txtTA);

        com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.aplicarFormatoCedula(txtCedula, null);
        com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.aplicarFormatoTelefono(txtTelefonos);

        // =========================================================================
        // RUTA ESTRICTA DE TABULACIÓN (Todo el formulario conectado)
        // =========================================================================
        FormatoClinicoUtil.configurarTabulacionRapida(
                // 1. Datos Personales
                txtNombre, txtCedula, dpFechaNacimiento, txtTelefonos, txtOcupacion, cmbSeguro, txtDireccion,

                // 2. Antecedentes
                txtAntecedentesFam, txtAntecedentesPers, txtAlergias, txtCirugias, txtTransfusiones,

                // 4. Motivo de Consulta
                txtMotivoConsulta, txtHistoriaEnfermedad,

                // --- NUEVO ORDEN AQUÍ ---
                // 5. Hallazgos Generales (Pasó arriba)
                txtHallazgosGen,

                // 6. Signos Vitales
                txtPeso, txtTalla, txtTA, txtFC, txtFR, txtTemp, txtSpO2,

                // 7. Examen Físico
                txtCabeza, txtCuello, txtTorax, txtCorazon, txtPulmones, txtAbdomen,
                txtMiembrosSup, txtMiembrosInf, txtGenitales, txtPiel,

                // Estos 3 saltarán solos si están visibles (Ginecología)
                txtMamas, txtEspeculoscopia, txtTactoVaginal,

                // 8. Conclusión
                txtEstudios, txtDiagnostico, txtTratamiento
        );
        if(cmbSexo != null) {
            cmbSexo.setItems(javafx.collections.FXCollections.observableArrayList("Masculino", "Femenino"));
            cmbSexo.valueProperty().addListener((obs, oldVal, newVal) -> {
                boolean esMujer = "Femenino".equalsIgnoreCase(newVal);
                vboxGinecologia.setVisible(esMujer);
                vboxGinecologia.setManaged(esMujer);
            });
        }
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
            String seguroSeleccionado = cmbSeguro.getEditor().getText();
            String sexoSeleccionado = cmbSexo.getValue(); // <-- SOLUCIÓN 1: Declaramos la variable aquí

            DatosPersonalesDTO datosPersonales = new DatosPersonalesDTO(
                    txtNombre.getText(), txtCedula.getText(), dpFechaNacimiento.getValue(),
                    sexoSeleccionado, "", "", txtDireccion.getText(), txtTelefonos.getText(),
                    "", seguroSeleccionado, txtOcupacion.getText(),
                    null
            );

            // Sección 2: Motivo y Enfermedad Actual
            HistoriaEnfermedadDTO historia = new HistoriaEnfermedadDTO(
                    txtMotivoConsulta.getText(),
                    txtHistoriaEnfermedad.getText()
            );

            // Sección 3: Antecedentes
            // <-- SOLUCIÓN 2: Declaramos las variables AFUERA del DTO
            String menarquia = txtMenarquia != null ? txtMenarquia.getText() : "";
            String gpca = txtGPCA != null ? txtGPCA.getText() : "";
            java.time.LocalDate fum = dpFUM != null ? dpFUM.getValue() : null;

            AntecedentesDTO antecedentes = new AntecedentesDTO(
                    txtAntecedentesFam.getText(), txtAntecedentesPers.getText(),
                    txtTransfusiones.getText(), txtCirugias.getText(), txtAlergias.getText(),
                    menarquia, fum, gpca // <-- Y aquí simplemente las pasamos como parámetros
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
                    parsearDoble(txtSpO2.getText()),
                    txtCabeza.getText(), txtCuello.getText(), txtTorax.getText(), txtCorazon.getText(),
                    txtPulmones.getText(), txtAbdomen.getText(), txtGenitales.getText(), txtMiembrosSup.getText(),
                    txtMiembrosInf.getText(), txtPiel.getText(), txtHallazgosGen.getText(), txtMamas.getText(), txtEspeculoscopia.getText(), txtTactoVaginal.getText(),
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
            // Borramos todo lo que NO sea un número, un punto o una coma.
            // Y si el doctor usó una coma (37,5), la cambiamos por un punto (37.5)
            String limpio = valor.replaceAll("[^0-9.,]", "").replace(",", ".");
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            log.warn("El valor no es numérico: {}. Se asignará 0.0", valor);
            return 0.0;
        }
    }



    /*
     * Este método es llamado por el Dashboard del Médico cuando el paciente
     * viene de la Sala de Espera (Recepción). Pre-llena los campos básicos.
     */
    /**
     * Este método es llamado por el Dashboard del Médico cuando el paciente
     * viene de la Sala de Espera (Recepción). Pre-llena los campos básicos.
     */
    // AÑADIMOS EL SEGUNDO PARÁMETRO AQUÍ ABAJO 👇
    public void cargarDatosPreliminares(com.proyectomedico.appmedicacenfasies.model.Paciente paciente, String especialidadTurno) {
        if (paciente != null) {
            txtCedula.setText(paciente.getCedula());
            txtNombre.setText(paciente.getNombreApellidos());

            if (paciente.getTelefonos() != null) {
                txtTelefonos.setText(paciente.getTelefonos());
            }

            if (paciente.getSeguro() != null) {
                cmbSeguro.getEditor().setText(paciente.getSeguro());
            }

            // --- NUEVOS CAMPOS DESDE RECEPCIÓN ---
            if (paciente.getFechaNacimiento() != null) {
                dpFechaNacimiento.setValue(paciente.getFechaNacimiento());
            }

            if (paciente.getSexo() != null) {
                cmbSexo.setValue(paciente.getSexo());
            }

            txtCedula.setEditable(false);
            txtNombre.setEditable(false);
            if(paciente.getSexo() != null) cmbSexo.setDisable(true);
        }

        // ¡Ahora sí funciona porque especialidadTurno viene en los paréntesis!
        if (especialidadTurno != null && especialidadTurno.toUpperCase().contains("GINECO")) {
            activarModoGinecologia();
        }
    }
    public void activarModoGinecologia() {
        // 1. Forzamos Sexo Femenino
        cmbSexo.setValue("Femenino");
        cmbSexo.setDisable(true); // Bloqueado porque en esta especialidad es obligatorio

        // 2. Mostramos los apartados ginecológicos de antecedentes (el que ya tenías)
        vboxGinecologia.setVisible(true);
        vboxGinecologia.setManaged(true);

        // 3. Mostramos los nuevos apartados físicos
        vboxMamas.setVisible(true);
        vboxMamas.setManaged(true);
        vboxExploracionGineco.setVisible(true);
        vboxExploracionGineco.setManaged(true);
    }
}
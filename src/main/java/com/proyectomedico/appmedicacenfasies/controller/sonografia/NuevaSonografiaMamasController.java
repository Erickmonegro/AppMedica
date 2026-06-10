package com.proyectomedico.appmedicacenfasies.controller.sonografia;

import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.dto.sonografias.SonografiaMamasDTO;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.Turno;
import com.proyectomedico.appmedicacenfasies.repository.PacienteRepository;
import com.proyectomedico.appmedicacenfasies.repository.TurnoRepository;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import com.proyectomedico.appmedicacenfasies.service.SonografiaService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NuevaSonografiaMamasController {

    private final SonografiaService sonografiaService;
    private final PacienteService pacienteService;
    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;
    private final SesionGlobal sesionGlobal;

    private UUID pacienteIdActual;

    @FXML private TextField txtNombrePaciente;
    @FXML private TextField txtCedulaPaciente;
    @FXML private TextField txtEdadPaciente;

    @FXML private TextField txtSuperficie;
    @FXML private TextField txtPezonAreola;
    @FXML private TextArea txtMamaDerecha;
    @FXML private TextArea txtMamaIzquierda;
    @FXML private TextArea txtAxilas;
    @FXML private TextArea txtBiRads;
    @FXML private TextArea txtDiagnostico;

    public void inicializarParaPaciente(UUID pacienteId) {
        this.pacienteIdActual = pacienteId;
        com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.aplicarFormatoCedula(txtCedulaPaciente, null);

        if (pacienteId == null) {
            // MODO AUTÓNOMO (Directo desde el Dashboard)
            txtNombrePaciente.clear();
            txtCedulaPaciente.clear();
            txtEdadPaciente.clear();

            txtNombrePaciente.setEditable(true);
            txtCedulaPaciente.setEditable(true);
            txtEdadPaciente.setEditable(true);
        } else {
            // MODO ENRUTADO (Viene desde un expediente/turno)
            try {
                var pacienteBD = pacienteService.obtenerExpedienteCompleto(pacienteId);
                if (pacienteBD != null) {
                    txtNombrePaciente.setText(pacienteBD.datosPersonales().nombreApellidos());
                    String cedula = pacienteBD.datosPersonales().cedula();
                    txtCedulaPaciente.setText(cedula != null ? cedula : "N/A");
                    txtEdadPaciente.setText(
                            com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.calcularEdadDesdeFecha(pacienteBD.datosPersonales().fechaNacimiento())
                    );
                }
            } catch (Exception e) {
                log.error("No se pudo autocompletar el paciente", e);
            }
            txtNombrePaciente.setEditable(false);
            txtCedulaPaciente.setEditable(false);
            txtEdadPaciente.setEditable(false);
        }
    }

    @FXML
    public void guardarSonografia(ActionEvent event) {
        try {
            // 1. GESTIÓN DEL PACIENTE
            if (pacienteIdActual == null) {
                String nombre = txtNombrePaciente.getText();
                String cedulaRaw = txtCedulaPaciente.getText();

                if (nombre == null || nombre.trim().isEmpty()) {
                    mostrarAlerta("Campos Incompletos", "Debe escribir el nombre de la paciente.");
                    return;
                }
                String cedulaSegura = (cedulaRaw != null && !cedulaRaw.trim().isEmpty()) ? cedulaRaw.trim() : null;
                java.time.LocalDate fechaNacimientoAprox = com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.calcularFechaDesdeEdad(txtEdadPaciente.getText());


                var nuevoPaciente = pacienteService.obtenerOCrearPacienteBasico(cedulaSegura, nombre, null, null, fechaNacimientoAprox, "Femenino");
                this.pacienteIdActual = nuevoPaciente.getId();
            }

            // 2. IDENTIFICAR MÉDICO
            String medicoRealizador = "Dr./Dra. No Identificado";
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico doctor) {
                medicoRealizador = "Dra. " + doctor.getNombreCompleto(); // Ajusta según el género si es necesario
            }

            // 3. EMPAQUETAR DTO (En base al SonografiaMamasDTO que me pasaste)
            SonografiaMamasDTO dto = new SonografiaMamasDTO(
                    medicoRealizador,
                    txtDiagnostico.getText(),
                    txtSuperficie.getText(),
                    txtPezonAreola.getText(),
                    txtMamaDerecha.getText(),
                    txtMamaIzquierda.getText(),
                    txtAxilas.getText(), // Regiones Axilares
                    txtBiRads.getText()
            );

            // 4. GUARDAR EN LA BD
            sonografiaService.guardarSonografiaMamas(pacienteIdActual, dto);

            // 5. AUTO-TURNO SILENCIOSO (Para que no desaparezca del Dashboard del Médico)
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico doctor) {
                boolean necesitaTurno = true;
                var ultimoTurno = turnoRepository.findFirstByPacienteIdOrderByFechaEntradaDesc(pacienteIdActual);

                if (ultimoTurno.isPresent() && ultimoTurno.get().getMedicoAsignado().getId().equals(doctor.getId())) {
                    if (ultimoTurno.get().getFechaEntrada().toLocalDate().isEqual(LocalDate.now())) {
                        necesitaTurno = false;
                    }
                }

                if (necesitaTurno) {
                    com.proyectomedico.appmedicacenfasies.model.Paciente pacienteEntity = pacienteRepository.findById(pacienteIdActual).orElse(null);
                    if (pacienteEntity != null) {
                        Turno autoTurno = new Turno();
                        autoTurno.setPaciente(pacienteEntity);
                        autoTurno.setMedicoAsignado(doctor);
                        autoTurno.setAreaDestino("SONOGRAFÍA DIRECTA");
                        autoTurno.setTipoEstudio("MAMAS");
                        autoTurno.setEstado("ATENDIDO");
                        autoTurno.setFechaEntrada(java.time.LocalDateTime.now());
                        turnoRepository.save(autoTurno);
                        log.info("Auto-Turno generado para Sonografía de Mamas");
                    }
                }
            }

            // 6. CERRAR MODAL
            cerrarVentana(event);

        } catch (Exception e) {
            log.error("Error al guardar la sonografía de mamas", e);
            mostrarAlerta("Error", "No se pudo guardar la sonografía: " + e.getMessage());
        }
    }

    @FXML
    public void cerrarVentana(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
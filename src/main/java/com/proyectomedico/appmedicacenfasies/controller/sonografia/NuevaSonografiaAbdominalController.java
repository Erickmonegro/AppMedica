package com.proyectomedico.appmedicacenfasies.controller.sonografia;

import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.dto.sonografias.SonografiaAbdominalDTO;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.Turno;
import com.proyectomedico.appmedicacenfasies.repository.PacienteRepository;
import com.proyectomedico.appmedicacenfasies.repository.TurnoRepository;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import com.proyectomedico.appmedicacenfasies.service.SonografiaService;
import com.proyectomedico.appmedicacenfasies.service.SonografiaPdfService; // Lo usaremos pronto
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NuevaSonografiaAbdominalController {

    private final SonografiaService sonografiaService;
    private final SonografiaPdfService sonografiaPdfService;
    private final SesionGlobal sesionGlobal;
    private final PacienteService pacienteService;
    private final TurnoRepository turnoRepository; // Inyéctalo en el constructor
    private final PacienteRepository pacienteRepository; // También lo necesitaremos para buscar la entidad

    private UUID pacienteId;

    @FXML
    private TextArea txtHigado, txtVesicula, txtPancreas, txtRinonDerecho, txtRinonIzquierdo, txtBazo, txtIntestinos, txtDiagnostico;
    @FXML
    private TextField txtLhd, txtColedocoPorta;

    @FXML
    private TextField txtNombrePaciente, txtEdadPaciente, txtCedulaPaciente;

    /**
     * Se llama al abrir el modal para saber a quién le estamos haciendo la sonografía.
     */
    public void inicializarParaPaciente(UUID pacienteId) {
        this.pacienteId = pacienteId;
        com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.aplicarFormatoCedula(txtCedulaPaciente, null);
        if (pacienteId == null) {
            // MODO CREACIÓN DIRECTA: Es un paciente de la calle
            txtNombrePaciente.clear();
            txtCedulaPaciente.clear();
            txtEdadPaciente.clear();

            txtNombrePaciente.setEditable(true);
            txtCedulaPaciente.setEditable(true);
            txtEdadPaciente.setEditable(true);
            log.info("Plantilla de Sonografía iniciada en Modo Autónomo (Paciente Nuevo)");
        } else {
            // MODO VISOR: Viene desde un expediente existente
            try {
                // Buscamos los datos reales del paciente en la BD
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
                log.error("No se pudo auto-completar los datos del paciente", e);
            }

            // Bloqueamos los campos para proteger la integridad de la BD
            txtNombrePaciente.setEditable(false);
            txtCedulaPaciente.setEditable(false);
            txtEdadPaciente.setEditable(false);
            log.info("Plantilla de Sonografía iniciada en Modo Enrutado (Paciente Existente)");
        }
    }

    @FXML
    public void guardarSonografia(ActionEvent event) {
        try {
            log.info("Empaquetando datos de la sonografía abdominal...");

            // =======================================================
            // 1. MANEJO DE PACIENTE AUTÓNOMO (Si es paciente nuevo de la calle)
            // =======================================================
            if (pacienteId == null) {
                String nombre = txtNombrePaciente.getText();
                String cedulaRaw = txtCedulaPaciente.getText(); // Capturamos la nueva caja de cédula

                if (nombre == null || nombre.trim().isEmpty()) {
                    mostrarAlerta("Campos Incompletos", "Debe escribir el nombre del paciente para continuar.");
                    return; // Detiene el guardado si no hay nombre
                }

                // Magia: Si la cédula está vacía, se vuelve null automáticamente para la BD
                String cedulaSegura = (cedulaRaw != null && !cedulaRaw.trim().isEmpty()) ? cedulaRaw.trim() : null;
                java.time.LocalDate fechaNacimientoAprox = com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.calcularFechaDesdeEdad(txtEdadPaciente.getText());


                // Usamos el servicio para auto-crear al paciente básico usando la cédula y el nombre
                com.proyectomedico.appmedicacenfasies.model.Paciente nuevoPaciente =
                        pacienteService.obtenerOCrearPacienteBasico(cedulaSegura, nombre, null, null, fechaNacimientoAprox, null);

                // Asignamos el ID recién creado a esta variable para que la sonografía se enlace a él
                this.pacienteId = nuevoPaciente.getId();
            }

            // =======================================================
            // 2. IDENTIFICAR AL MÉDICO REALIZADOR
            // =======================================================
            String medicoRealizador = "Dr./Dra. No Identificado";
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico doctor) {
                medicoRealizador = "Dra. " + doctor.getNombreCompleto();
            }

            // =======================================================
            // 3. ARMAR EL DTO
            // =======================================================
            SonografiaAbdominalDTO dto = new SonografiaAbdominalDTO(
                    medicoRealizador,
                    txtDiagnostico.getText(),
                    txtHigado.getText(),
                    txtLhd.getText(),
                    txtColedocoPorta.getText(),
                    txtVesicula.getText(),
                    txtPancreas.getText(),
                    txtBazo.getText(),
                    txtRinonDerecho.getText(),
                    txtRinonIzquierdo.getText(),
                    txtIntestinos.getText()
            );

            // =======================================================
            // 4. GUARDAR EN BD Y CERRAR
            // =======================================================
            sonografiaService.guardarSonografiaAbdominal(pacienteId, dto);
            // --- AUTO-TURNO PARA SONOGRAFÍA DIRECTA ---
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico doctor) {
                boolean necesitaTurno = true;
                var ultimoTurno = turnoRepository.findFirstByPacienteIdOrderByFechaEntradaDesc(pacienteId);

                if (ultimoTurno.isPresent() && ultimoTurno.get().getMedicoAsignado().getId().equals(doctor.getId())) {
                    if (ultimoTurno.get().getFechaEntrada().toLocalDate().isEqual(java.time.LocalDate.now())) {
                        necesitaTurno = false;
                    }
                }

                if (necesitaTurno) {
                    com.proyectomedico.appmedicacenfasies.model.Paciente pacienteEntity = pacienteRepository.findById(pacienteId).orElse(null);
                    if (pacienteEntity != null) {
                        Turno autoTurno = new Turno();
                        autoTurno.setPaciente(pacienteEntity);
                        autoTurno.setMedicoAsignado(doctor);
                        autoTurno.setAreaDestino("SONOGRAFÍA DIRECTA");
                        autoTurno.setTipoEstudio("ABDOMINAL");
                        autoTurno.setEstado("ATENDIDO");
                        autoTurno.setFechaEntrada(java.time.LocalDateTime.now());
                        turnoRepository.save(autoTurno);
                    }
                }
            }
            // -------------------------------------------

            // (Opcional por ahora) Generación física del PDF
            // String rutaPdf = sonografiaPdfService.generarPdfAbdominal(pacienteId, dto);

            log.info("Sonografía Abdominal registrada correctamente para el paciente: {}", pacienteId);

            // 5. Cerrar Modal
            cerrarVentana(event);

        } catch (Exception e) {
            log.error("Error crítico al guardar la sonografía abdominal", e);
            mostrarAlerta("Error de Sistema", "No se pudo guardar la sonografía: " + e.getMessage());
        }
    }

    @FXML
    public void cerrarVentana(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            log.error("Error al cerrar ventana", e);
        }
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.dto.HojaEvolucionDTO;
import com.proyectomedico.appmedicacenfasies.dto.ResultadosEvolucionDTO;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
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
public class NuevaEvolucionController {

    private final PacienteService pacienteService;
    private final SesionGlobal sesionGlobal;

    // Almacenamos el ID del paciente al que le vamos a agregar la nota
    private UUID pacienteId;

    @FXML private TextArea txtMotivo;
    @FXML private TextArea txtHistoria;
    @FXML private TextArea txtDiagnostico;
    @FXML private TextArea txtTratamiento;
    @FXML private TextArea txtPlan;
    // --- RESULTADOS DE LABORATORIO ---
    @FXML private TextField txtHb, txtHtco, txtPlaq, txtGlic, txtH1ac;
    @FXML private TextField txtColest, txtHdl, txtLdl, txtTrig, txtSonografias;
    @FXML private TextArea txtOtrosResultados;

    /**
     * El VisorExpediente llamará a este método justo antes de mostrar la ventana.
     */
    public void inicializarParaPaciente(UUID pacienteId) {
        this.pacienteId = pacienteId;
        log.info("Modal de Evolución listo para el paciente ID: {}", pacienteId);
    }

    @FXML
    public void guardarEvolucion(ActionEvent event) {
        try {
            log.info("Empaquetando datos de la evolución...");

            String medicoAuditoria = "Médico no identificado";
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof com.proyectomedico.appmedicacenfasies.model.Medico) {
                com.proyectomedico.appmedicacenfasies.model.Medico doctor = (com.proyectomedico.appmedicacenfasies.model.Medico) sesionGlobal.getUsuarioLogueado();
                medicoAuditoria = "Dr. " + doctor.getNombreCompleto();
            }

            // 1. Armamos el sub-cajón de laboratorios
            ResultadosEvolucionDTO resultadosLab = new ResultadosEvolucionDTO(
                    txtHb.getText(), txtHtco.getText(), txtPlaq.getText(),
                    txtGlic.getText(), txtH1ac.getText(), txtColest.getText(),
                    txtHdl.getText(), txtLdl.getText(), txtTrig.getText(),
                    txtSonografias.getText(), txtOtrosResultados.getText()
            );

            // 1. Armar el DTO de transporte (La fecha y el ID de la hoja se ignoran aquí, el Service los crea)
            HojaEvolucionDTO nuevaEvolucion = new HojaEvolucionDTO(
                    null, // ID nulo porque es nueva
                    null, // Fecha nula, el backend le pondrá LocalDate.now()
                    txtMotivo.getText(),
                    txtHistoria.getText(),
                    txtDiagnostico.getText(),
                    txtTratamiento.getText(),
                    txtPlan.getText(),
                    null,
                    resultadosLab,
                    medicoAuditoria


            );

            // 2. Enviar a guardar
            pacienteService.agregarEvolucion(pacienteId, nuevaEvolucion);

            log.info("Evolución guardada con éxito en BD.");

            // 3. Cerrar la ventana modal
            cerrarVentana(event);

        } catch (Exception e) {
            log.error("Error al guardar la hoja de evolución", e);
            // TODO: Agregar una alerta visual (Alert) de JavaFX si falla
        }
    }

    @FXML
    public void cerrarVentana(ActionEvent event) {
        try{
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();

    } catch (Exception e) {
        log.error("Error al cerrar ventana", e);
    }
    }
}
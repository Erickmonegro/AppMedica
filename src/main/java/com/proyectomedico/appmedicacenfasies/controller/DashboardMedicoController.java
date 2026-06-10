package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.dto.PacienteResumenDTO;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.Turno;
import com.proyectomedico.appmedicacenfasies.repository.TurnoRepository;
import com.proyectomedico.appmedicacenfasies.service.PacienteService;
import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardMedicoController {

    // --- DEPENDENCIAS DEL BACKEND ---
    private final PacienteService pacienteService;
    private final TurnoRepository turnoRepository;
    private final SesionGlobal sesionGlobal;
    private final ApplicationContext applicationContext;

    // --- ELEMENTOS DE LA INTERFAZ ---
    @FXML
    private TextField txtBuscar;
    @FXML
    private ListView<PacienteResumenDTO> listaPacientes; // Recuperamos tu lista original
    @FXML
    private VBox vboxCitasDashboard;
    @FXML
    private Label lblBienvenidaDoctor;
    @FXML
    private SplitMenuButton btnNuevoPaciente;
    @FXML
    private Button btnNuevoPacienteNormal;

    private javafx.animation.Timeline relojSincronizador;

    @FXML
    public void initialize() {
        log.info("Inicializando Panel Médico...");


        if (relojSincronizador != null){
            relojSincronizador.stop();
        }

        // 1. Identidad del Doctor y Sala de Espera
        if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico) {
            Medico doctor = (Medico) sesionGlobal.getUsuarioLogueado();
            lblBienvenidaDoctor.setText("Dr. " + doctor.getNombreCompleto());
            cargarSalaDeEspera(doctor);

            // ==========================================
            // ¡NUEVO!: LÓGICA DEL BOTÓN DESPLEGABLE
            // ==========================================
            // ==========================================
            // LÓGICA BLINDADA DEL BOTÓN DESPLEGABLE
            // ==========================================
            // ==========================================
            // LÓGICA DE INTERCAMBIO DE BOTONES (UX)
            // ==========================================
            if (btnNuevoPaciente != null && btnNuevoPacienteNormal != null) {
                // Buscamos si ALGUNA de sus especialidades contiene la palabra "SONOGRAF"
                boolean esSonografista = doctor.getEspecialidades().stream()
                        .anyMatch(esp -> esp.name().toUpperCase().contains("SONOGRAF"));

                if (esSonografista) {
                    // MODO SONOGRAFISTA: Mostramos el desplegable, ocultamos el normal
                    btnNuevoPaciente.setVisible(true);
                    btnNuevoPaciente.setManaged(true);

                    btnNuevoPacienteNormal.setVisible(false);
                    btnNuevoPacienteNormal.setManaged(false);
                } else {
                    // MODO MÉDICO NORMAL: Mostramos el botón sencillo, ocultamos el desplegable
                    btnNuevoPacienteNormal.setVisible(true);
                    btnNuevoPacienteNormal.setManaged(true);

                    btnNuevoPaciente.setVisible(false);
                    btnNuevoPaciente.setManaged(false);
                }
            }
        }

        // 2. Panel Izquierdo: Configurar lista y cargar pacientes
        configurarDisenoDeLaLista();
        cargarPacientes("");

        // 3. EVENTO: Búsqueda en tiempo real
        txtBuscar.textProperty().addListener((observable, textoViejo, textoNuevo) -> {
            cargarPacientes(textoNuevo);
        });

        // 4. EVENTO: Doble Clic en la lista izquierda
        listaPacientes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && listaPacientes.getSelectionModel().getSelectedItem() != null) {
                PacienteResumenDTO pacienteSeleccionado = listaPacientes.getSelectionModel().getSelectedItem();
                abrirVisorExpediente(pacienteSeleccionado.id());
            }
        });
        // =========================================================================
        // MOTOR DE SINCRONIZACIÓN ASÍNCRONA EN TIEMPO REAL (Sala de Espera y Lista)
        // =========================================================================
        if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico doctor) {
            relojSincronizador = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(6), event -> {

                        // Salvaguarda UX: Capturamos si el médico está escribiendo en el buscador
                        String filtroActual = txtBuscar.getText() != null ? txtBuscar.getText().trim() : "";

                        // Hilo Secundario: Descarga los datos de la red sin congelar la pantalla
                        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                            List<Turno> espera = turnoRepository.findByMedicoAsignadoIdAndEstadoOrderByFechaEntradaAsc(doctor.getId(), "EN_ESPERA");

                            // Solo consultamos pacientes globales si el buscador está vacío para ahorrar memoria
                            List<PacienteResumenDTO> globales = filtroActual.isEmpty() ?
                                    pacienteService.buscarPacientesDelMedico("", doctor.getId()) : null;

                            return new Object[]{espera, globales};
                        }).thenAcceptAsync(datos -> {

                            // Hilo de la UI: Pintamos los resultados instantáneamente
                            Platform.runLater(() -> {
                                List<Turno> pacientesEsperando = (List<Turno>) datos[0];
                                List<PacienteResumenDTO> pacientesGlobales = (List<PacienteResumenDTO>) datos[1];

                                // A. Refrescar Sala de Espera dinámica (Tarjetas)
                                vboxCitasDashboard.getChildren().clear();
                                if (pacientesEsperando.isEmpty()) {
                                    Label lblVacio = new Label("No hay pacientes en sala de espera.");
                                    lblVacio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                                    vboxCitasDashboard.getChildren().add(lblVacio);
                                } else {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                                    for (Turno turno : pacientesEsperando) {
                                        vboxCitasDashboard.getChildren().add(crearTarjetaPaciente(turno, formatter));
                                    }
                                }

                                // B. Refrescar Lista Izquierda (SOLO si el médico NO está escribiendo una búsqueda)
                                if (filtroActual.isEmpty() && pacientesGlobales != null) {
                                    listaPacientes.getItems().setAll(pacientesGlobales);
                                }
                            });
                        }).exceptionally(ex -> {
                            log.error("Error en la sincronización en segundo plano del médico", ex);
                            return null;
                        });
                    })
            );
            relojSincronizador.setCycleCount(javafx.animation.Timeline.INDEFINITE);
            relojSincronizador.play();
        }
    }

    // ==========================================================
    // --- MAGIA NUEVA: SALA DE ESPERA CON BOTÓN INTEGRADO ---
    // ==========================================================

    private void cargarSalaDeEspera(Medico doctor) {
        Platform.runLater(() -> {
            vboxCitasDashboard.getChildren().clear();

            List<Turno> pacientesEsperando = turnoRepository
                    .findByMedicoAsignadoIdAndEstadoOrderByFechaEntradaAsc(doctor.getId(), "EN_ESPERA");

            if (pacientesEsperando.isEmpty()) {
                Label lblVacio = new Label("No hay pacientes en sala de espera.");
                lblVacio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                vboxCitasDashboard.getChildren().add(lblVacio);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

            for (Turno turno : pacientesEsperando) {
                HBox tarjeta = crearTarjetaPaciente(turno, formatter);
                vboxCitasDashboard.getChildren().add(tarjeta);
            }
        });
    }

    private HBox crearTarjetaPaciente(Turno turno, DateTimeFormatter formatter) {
        HBox tarjeta = new HBox(15);
        tarjeta.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        tarjeta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Indicador visual
        Circle indicador = new Circle(6, Color.web("#28a745"));

        // Textos del paciente
        VBox datos = new VBox(5);
        Label lblNombre = new Label("👤 " + turno.getPaciente().getNombreApellidos());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1e293b;");

        // MAGIA VISUAL: Ahora el motivo es detallado. Aumentamos un poco la fuente y le permitimos bajar de línea si es muy largo.
        Label lblDetalles = new Label("Llegó: " + turno.getFechaEntrada().format(formatter) + " | Motivo: " + turno.getAreaDestino());
        lblDetalles.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        lblDetalles.setWrapText(true);

        datos.getChildren().addAll(lblNombre, lblDetalles);

        // "Resorte" invisible para empujar el botón a la derecha
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // EL BOTÓN "ATENDER" INTEGRADO EN LA TARJETA
        Button btnAtender = new Button("Atender");
        btnAtender.setStyle("-fx-background-color: #0b4a9e; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");

        btnAtender.setOnAction(event -> {
            log.info("Atendiendo paciente: {}", turno.getPaciente().getNombreApellidos());
            decidirRutaAtencion(turno);
        });

        tarjeta.getChildren().addAll(indicador, datos, spacer, btnAtender);

        // Efecto Hover suave (opcional)
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8;"));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;"));

        return tarjeta;
    }

    // ==========================================================
    // --- MÉTODOS HEREDADOS (EL MERGE PERFECTO) ---
    // ==========================================================

    @FXML
    public void abrirNuevoPaciente() {
        try {
            log.info("Cargando vista de Nuevo Paciente...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nuevo_paciente.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();

            // 1. PRIMERO creamos el Stage (La ventana) y sus configuraciones básicas
            Stage stage = new Stage();
            stage.setTitle("Registro de Historia Clínica");
            stage.initModality(Modality.APPLICATION_MODAL);

            // 2. SEGUNDO aplicamos la magia responsiva
            // (Esta línea calcula el tamaño del monitor, crea la Scene y se la inyecta al Stage)
            com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.configurarVentanaResponsiva(stage, root, 1000, 800);

            // 3. TERCERO le ponemos los estilos (Ahora funciona porque el Stage ya tiene una Scene gracias al paso 2)
            stage.getScene().getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());

            // 4. FINALMENTE mostramos la ventana en pantalla
            stage.showAndWait();

            // Refrescamos la lista al cerrar
            cargarPacientes("");

        } catch (Exception e) {
            log.error("Error crítico al intentar abrir la ventana de Nuevo Paciente", e);
        }
    }

    private void abrirVisorExpediente(UUID pacienteId) {
        log.info("Abriendo expediente del paciente con ID: {}", pacienteId);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/visor_expediente.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            VisorExpedienteController visorController = loader.getController();
            visorController.cargarDatosPaciente(pacienteId);

            Stage stage = new Stage();
            stage.setTitle("Expediente Médico - CENFASIES");
            stage.setScene(new Scene(root, 1000, 700));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error crítico al abrir la ventana del expediente", e);
        }
    }

    // Reemplaza tu antiguo cargarPacientes con este:
    private void cargarPacientes(String filtro) {
        Platform.runLater(() -> {
            // Verificamos que haya un médico logueado
            if (sesionGlobal.haySesionActiva() && sesionGlobal.getUsuarioLogueado() instanceof Medico) {
                Medico doctor = (Medico) sesionGlobal.getUsuarioLogueado();

                // Usamos el nuevo método seguro
                List<PacienteResumenDTO> resultados = pacienteService.buscarPacientesDelMedico(filtro, doctor.getId());
                listaPacientes.getItems().setAll(resultados);
            }
        });
    }

    private void configurarDisenoDeLaLista() {
        listaPacientes.setCellFactory(param -> new ListCell<>() {
            // Creamos los componentes visuales una sola vez por celda
            private final VBox container = new VBox();
            private final Label lblNombre = new Label();
            private final Label lblCedula = new Label();

            {
                // Estilizamos cada línea de forma independiente (Estilo Web)
                lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
                lblCedula.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");

                container.getChildren().addAll(lblNombre, lblCedula);
                container.setSpacing(3);
                container.setStyle("-fx-padding: 12px; -fx-border-color: transparent transparent #e2e8f0 transparent; -fx-border-width: 1px;");
            }

            @Override
            protected void updateItem(PacienteResumenDTO paciente, boolean empty) {
                super.updateItem(paciente, empty);

                if (empty || paciente == null) {
                    setGraphic(null); // Ocultamos el contenedor si no hay datos
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Llenamos los datos
                    lblNombre.setText(paciente.nombreApellidos());
                    lblCedula.setText("Cédula: " + (paciente.cedula() != null ? paciente.cedula() : "N/A"));

                    // ¡LA MAGIA!: En lugar de setText, usamos setGraphic para inyectar nuestro diseño
                    setGraphic(container);
                    setText(null);

                    // Dejamos el fondo transparente para que el efecto "hover" y "selected" del CSS funcione
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    @FXML
    public void cerrarSesion(ActionEvent event) {
        if (relojSincronizador != null) {
            relojSincronizador.stop();
        }
        sesionGlobal.cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // MAGIA SENIOR: En lugar de usar el 'event' (que es un MenuItem),
            // usamos el 'txtBuscar' que ya está inyectado y SÍ es un Node,
            // para extraer en qué ventana estamos parados.
            Stage stage = (Stage) txtBuscar.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setTitle("Login - CENFASIES");
        } catch (Exception e) {
            log.error("Error al cerrar sesión", e);
        }
    }

    private void decidirRutaAtencion(Turno turno) {
        UUID pacienteId = turno.getPaciente().getId();

        log.info("Iniciando Smart Routing para el turno: {}", turno.getId());

        // 1. EVALUAR EL ADN DEL TURNO (¿Para dónde va?)
        String area = turno.getAreaDestino() != null ? turno.getAreaDestino().toUpperCase() : "GENERAL";
        String estudio = turno.getTipoEstudio(); // Ej: ABDOMINAL

        if (area.contains("SONOGRAF")) {
            // ========================================================
            // RUTA A: Es una Sonografía (Abrir Visor directamente en el modal del reporte)
            // ========================================================
            log.info("Detectada Sonografía [{}]. Enrutando a plantilla específica...", estudio);
            abrirVisorExpedienteYAutoLanzarSonografia(pacienteId, estudio);

        } else {
            // ========================================================
            // RUTA B: Es Medicina General (Tu flujo original)
            // ========================================================
            boolean yaTieneHistoria = pacienteService.tieneHistoriaClinicaCompleta(pacienteId);
            if (yaTieneHistoria) {
                log.info("Paciente recurrente (General). Abriendo Visor de Expediente.");
                abrirVisorExpediente(pacienteId);
            } else {
                log.info("Paciente nuevo (General). Abriendo Formulario Inicial.");
                abrirNuevoPacienteDesdeRecepcion(turno.getPaciente(), turno.getAreaDestino());
            }
        }

        // 3. Finalizar
        turno.setEstado("ATENDIDO");
        turnoRepository.save(turno);

        Medico doctor = (Medico) sesionGlobal.getUsuarioLogueado();
        cargarSalaDeEspera(doctor);
    }

    // 2. EL MÉTODO PARA ABRIR FORMULARIO PRE-LLENADO
    // 2. EL MÉTODO PARA ABRIR FORMULARIO PRE-LLENADO
    // AÑADIMOS EL SEGUNDO PARÁMETRO EN LA FIRMA DEL MÉTODO 👇
    private void abrirNuevoPacienteDesdeRecepcion(com.proyectomedico.appmedicacenfasies.model.Paciente pacienteBasico, String especialidadTurno) {
        try {
            log.info("Cargando vista de Nuevo Paciente para pre-llenado...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/nuevo_paciente.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();

            // --- MAGIA: Le pasamos los datos básicos Y LA ESPECIALIDAD al formulario ---
            NuevoPacienteController controller = fxmlLoader.getController();
            controller.cargarDatosPreliminares(pacienteBasico, especialidadTurno);

            // 1. PRIMERO: Creamos el Stage (Ventana) y sus propiedades básicas
            Stage stage = new Stage();
            stage.setTitle("Registro de Historia Clínica - " + pacienteBasico.getNombreApellidos());
            stage.initModality(Modality.APPLICATION_MODAL);

            // 2. SEGUNDO: Usamos TU clase de utilidades para crear y ajustar la Scene responsiva
            com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.configurarVentanaResponsiva(stage, root, 1000, 800);

            // 3. TERCERO: Le inyectamos los estilos CSS (ahora sí se puede porque la Scene ya existe)
            stage.getScene().getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());

            // 4. FINALMENTE: Mostramos la ventana
            stage.showAndWait();

            // Refrescamos al cerrar
            cargarPacientes("");

        } catch (Exception e) {
            log.error("Error al abrir la ventana de Nuevo Paciente desde recepción", e);
        }
    }

    @FXML
    public void abrirMenuRapidoSonografia() {
        log.info("Médico inicia creación directa de Sonografía...");

        List<String> estudios = java.util.Arrays.asList("ABDOMINAL", "OBSTETRICA", "MAMAS", "PELVICA_FEMENINA", "PELVICA_MASCULINA");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("ABDOMINAL", estudios);
        dialog.setTitle("Seleccionar Estudio");
        dialog.setHeaderText("Creación Directa de Reporte");
        dialog.setContentText("Estudio a realizar:");

        java.util.Optional<String> result = dialog.showAndWait();

        result.ifPresent(tipoEstudio -> {
            try {
                String rutaFxml = "";
                // 1. Elegimos qué plantilla abrir
                switch (tipoEstudio) {
                    case "ABDOMINAL":
                        rutaFxml = "/fxml/sonografias/nueva_sonografia_abdominal.fxml";
                        break;
                    case "TIROIDES": rutaFxml = "/fxml/nueva_sonografia_tiroides.fxml"; break;
                    case "MAMAS":
                        rutaFxml = "/fxml/sonografias/nueva_sonografia_mamas.fxml";
                        break;
                    case "PELVICA_FEMENINA":
                        rutaFxml = "/fxml/sonografias/nueva_sonografia_pelvica_femenina.fxml";
                        break;
                    case "OBSTETRICA":
                        rutaFxml = "/fxml/sonografias/nueva_sonografia_obstetrica.fxml";
                        break;
                    case "PELVICA_MASCULINA":
                        rutaFxml = "/fxml/sonografias/nueva_sonografia_pelvica_masculina.fxml";
                        break;
                    default:
                        mostrarAlerta("En Desarrollo", "La plantilla directa para " + tipoEstudio + " aún no está conectada.");
                        return;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                // 2. ¡EL TRUCO ARQUITECTÓNICO!
                // Le pasamos 'NULL' al controlador. Esto le dirá a la pantalla:
                // "Oye, este es un paciente nuevo, habilita los campos para que el doctor escriba el nombre".
                if (tipoEstudio.equals("ABDOMINAL")) {
                    com.proyectomedico.appmedicacenfasies.controller.sonografia.NuevaSonografiaAbdominalController controller = loader.getController();
                    controller.inicializarParaPaciente(null);
                } else if (tipoEstudio.equals("MAMAS")) {
                    com.proyectomedico.appmedicacenfasies.controller.sonografia.NuevaSonografiaMamasController controller = loader.getController();
                    controller.inicializarParaPaciente(null);
                } else if (tipoEstudio.equals("PELVICA_FEMENINA")) {
                    com.proyectomedico.appmedicacenfasies.controller.sonografia.NuevaSonografiaPelvicaFemeninaController controller = loader.getController();
                    controller.inicializarParaPaciente(null);
                }  else if (tipoEstudio.equals("OBSTETRICA")) {
                var controller = (com.proyectomedico.appmedicacenfasies.controller.sonografia.NuevaSonografiaObstetricaController) loader.getController();
                    controller.inicializarParaPaciente(null);
            }else if (tipoEstudio.equals("PELVICA_MASCULINA")) {
                    com.proyectomedico.appmedicacenfasies.controller.sonografia.NuevaSonografiaPelvicaMasculinaController controller = loader.getController();
                    controller.inicializarParaPaciente(null);
                } else if (tipoEstudio.equals("TIROIDES")){
                    com.proyectomedico.appmedicacenfasies.controller.sonografia.NuevaSonografiaTiroidesController controller = loader.getController();
                    controller.inicializarParaPaciente(null);
                }


                Stage stage = new Stage();
                stage.setTitle("Reporte Sonográfico - " + tipoEstudio);
                stage.initModality(Modality.APPLICATION_MODAL);

                // Usamos la utilidad para evitar desbordes
                com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.configurarVentanaResponsiva(stage, root, 1000, 800);

                stage.showAndWait();

            } catch(Exception e){
                log.error("Error al abrir formulario directo de sonografía", e);
            }
    });
}

    private void abrirVisorExpedienteYAutoLanzarSonografia(UUID pacienteId, String tipoEstudio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/visor_expediente.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            VisorExpedienteController visorController = loader.getController();
            visorController.cargarDatosPaciente(pacienteId);

            // ¡EL COMANDO MÁGICO!
            visorController.ejecutarAutoAperturaSonografia(tipoEstudio);

            // ==========================================
            // NUEVA APERTURA RESPONSIVA
            // ==========================================
            Stage stage = new Stage();
            stage.setTitle("Expediente Médico - Sonografía");

            // Reemplazamos la escena estática por nuestra utilidad
            com.proyectomedico.appmedicacenfasies.util.FormatoClinicoUtil.configurarVentanaResponsiva(stage, root, 1000, 700);

            // IMPORTANTE: Quitamos el setResizable(false) para que Windows permita moverla libremente
            stage.setResizable(true);
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error crítico al enrutar hacia el Visor de Expediente para Sonografía", e);
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
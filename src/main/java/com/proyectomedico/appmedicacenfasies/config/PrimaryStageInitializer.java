package com.proyectomedico.appmedicacenfasies.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final ApplicationContext applicationContext;
    private final String applicationTitle;
    private final Resource fxmlResource;

    // Inyectamos el contexto de Spring y preparamos la primera vista (Ej. dashboard.fxml)
    public PrimaryStageInitializer(
            ApplicationContext applicationContext,
            @Value("${spring.application.name}") String applicationTitle,
            @Value("classpath:/fxml/dashboard.fxml") Resource fxmlResource) {

        this.applicationContext = applicationContext;
        this.applicationTitle = applicationTitle;
        this.fxmlResource = fxmlResource;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Stage stage = event.getStage();
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlResource.getURL());

            // Le decimos a JavaFX: "Usa Spring para crear los controladores"
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1024, 768); // Resolución inicial

            // --- NUEVO CÓDIGO SENIOR AQUÍ ---
            // Cargamos el CSS de forma absoluta y segura
            String cssPath = getClass().getResource("/css/estilos.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            // ---------------------------------

            stage.setScene(scene);
            stage.setTitle(applicationTitle);
            stage.show();

        } catch (Exception e) {
            // Cambié IOException por Exception para atrapar cualquier fallo de ruta
            throw new RuntimeException("Error crítico al cargar la interfaz gráfica o los estilos", e);
        }
    }
}
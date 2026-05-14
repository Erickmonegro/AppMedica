package com.proyectomedico.appmedicacenfasies.controller;

import com.proyectomedico.appmedicacenfasies.config.SesionGlobal;
import com.proyectomedico.appmedicacenfasies.model.Rol;
import com.proyectomedico.appmedicacenfasies.model.Usuario;
import com.proyectomedico.appmedicacenfasies.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;
    // Inyectamos el cerebro de Spring Boot para pasárselo a JavaFX
    private final ApplicationContext applicationContext;
    private final SesionGlobal sesionGlobal;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleLogin() {
        try {
            lblError.setText(""); // Limpiar errores previos
            Usuario user = authService.autenticar(txtUsername.getText(), txtPassword.getText());

            sesionGlobal.iniciarSesion(user);

            // SEGMENTACIÓN POR ROL
            if (user.getRol() == Rol.SECRETARIO) {
                cargarPantalla("/fxml/dashboard_secretaria.fxml", "Panel de Recepción - CENFASIES");
            } else if (user.getRol() == Rol.MEDICO) {
                cargarPantalla("/fxml/dashboard_medico.fxml", "Panel Médico - CENFASIES");
            }

        } catch (Exception e) {
            e.printStackTrace(); // <--- ESTA ES LA MAGIA: Nos dirá la verdad en la consola
            lblError.setText("Error: Revisa la consola de IntelliJ");
        }
    }

    private void cargarPantalla(String fxmlPath, String titulo) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));

            // MAGIA SENIOR: Le decimos a JavaFX que use Spring para crear los controladores de la nueva pantalla
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            Parent root = fxmlLoader.load();

            // Obtenemos la ventana (Stage) actual usando uno de los campos de texto
            Stage stage = (Stage) txtUsername.getScene().getWindow();

            // Cambiamos la escena
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            lblError.setText("Error al cargar la pantalla del sistema.");
            e.printStackTrace();
        }
    }
}
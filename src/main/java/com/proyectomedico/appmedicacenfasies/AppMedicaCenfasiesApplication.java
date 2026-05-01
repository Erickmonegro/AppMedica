package com.proyectomedico.appmedicacenfasies;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppMedicaCenfasiesApplication {

    public static void main(String[] args) {
        // En lugar de arrancar Spring directamente, arrancamos la app de JavaFX.
        // La clase JavaFxApplication se encargará de levantar Spring en su método init().
        Application.launch(JavaFxApplication.class, args);
    }
}
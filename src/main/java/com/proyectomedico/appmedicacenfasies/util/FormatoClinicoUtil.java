package com.proyectomedico.appmedicacenfasies.util;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class FormatoClinicoUtil {

    /**
     * Calcula y formatea el IMC automáticamente escuchando los campos de peso y talla.
     */
    public static void configurarCalculoIMC(TextField txtPeso, TextField txtTalla, TextField txtIMC) {
        ChangeListener<String> listener = (obs, oldVal, newVal) -> {
            try {
                double peso = Double.parseDouble(txtPeso.getText().replace(",", "."));
                double talla = Double.parseDouble(txtTalla.getText().replace(",", "."));

                if (talla > 3.0) talla = talla / 100.0; // Autocorrección si escriben cm

                if (talla > 0) {
                    double imc = peso / (talla * talla);
                    txtIMC.setText(String.format("%.1f", imc));

                    // Colores según alerta médica
                    if (imc < 18.5) txtIMC.setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
                    else if (imc < 24.9) txtIMC.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    else if (imc < 29.9) txtIMC.setStyle("-fx-text-fill: #f97316; -fx-font-weight: bold;");
                    else txtIMC.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                } else {
                    txtIMC.clear();
                }
            } catch (Exception e) {
                txtIMC.clear();
            }
        };

        txtPeso.textProperty().addListener(listener);
        txtTalla.textProperty().addListener(listener);
    }

    /**
     * Auto-formatea la Tensión Arterial (Ej: 12080 -> 120/80)
     */
    public static void configurarFormatoTA(TextField txtTA) {
        txtTA.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            String clean = newValue.replaceAll("[^\\d]", "");
            if (clean.length() > 6) clean = clean.substring(0, 6);

            String result = clean;
            if (clean.length() >= 2) {
                char firstChar = clean.charAt(0);
                int systoleLen = (firstChar == '1' || firstChar == '2' || firstChar == '3') ? 3 : 2;

                if (clean.length() > systoleLen) {
                    result = clean.substring(0, systoleLen) + "/" + clean.substring(systoleLen);
                } else if (clean.length() == systoleLen) {
                    result = clean + "/";
                }
            }

            if (!newValue.equals(result)) {
                txtTA.setText(result);
            }
        });
    }

    /**
     * MAGIA SENIOR: Ruta Estricta de Tabulación.
     * Obliga a la tecla TAB a seguir exactamente el orden que pasemos por parámetro.
     */
    public static void configurarTabulacionRapida(Node... nodos) {
        for (int i = 0; i < nodos.length - 1; i++) {
            Node actual = nodos[i];
            Node siguiente = nodos[i + 1];

            actual.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                // Si presiona TAB y NO tiene pisado Shift (Shift+TAB es para ir hacia atrás)
                if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
                    event.consume(); // Mata el salto por defecto
                    siguiente.requestFocus(); // Forzamos ir al componente exacto que sigue
                }
            });
        }
    }
}
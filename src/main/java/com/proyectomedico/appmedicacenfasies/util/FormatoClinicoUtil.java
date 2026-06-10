package com.proyectomedico.appmedicacenfasies.util;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
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
                    if (imc < 18.5) txtIMC.setStyle("-fx-text-fill: #d5a81f; -fx-font-weight: bold;");
                    else if (imc < 24.9) txtIMC.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    else if (imc < 29.9) txtIMC.setStyle("-fx-text-fill: #d56111; -fx-font-weight: bold;");
                    else txtIMC.setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold;");
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

    /**
     * Auto-formatea la cédula a XXX-XXXXXXX-X.
     * @param txtCedula El campo de texto de la cédula.
     * @param chkExtranjero (Opcional) Si es null, siempre asume dominicano. Si no es null y está marcado, permite texto libre.
     */
    public static void aplicarFormatoCedula(TextField txtCedula, CheckBox chkExtranjero) {
        if (txtCedula == null) return;

        txtCedula.textProperty().addListener((observable, oldValue, newValue) -> {
            // Si el checkbox de extranjero existe y está marcado, lo dejamos escribir libremente
            if (chkExtranjero != null && chkExtranjero.isSelected()) return;
            if (newValue == null) return;

            // 1. Extraemos solo los números
            String numeros = newValue.replaceAll("[^\\d]", "");

            // 2. Limitamos a 11 dígitos máximo
            if (numeros.length() > 11) numeros = numeros.substring(0, 11);

            // 3. Ensamblamos con los guiones
            StringBuilder formateado = new StringBuilder();
            for (int i = 0; i < numeros.length(); i++) {
                if (i == 3 || i == 10) formateado.append("-");
                formateado.append(numeros.charAt(i));
            }

            // 4. Actualizamos el campo de texto (evitando loops infinitos)
            if (!newValue.equals(formateado.toString())) {
                txtCedula.setText(formateado.toString());
            }
        });
    }

    /**
     * Auto-formatea el teléfono a XXX-XXX-XXXX
     */
    public static void aplicarFormatoTelefono(TextField txtTelefono) {
        if (txtTelefono == null) return;

        txtTelefono.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            String numeros = newValue.replaceAll("[^\\d]", "");
            if (numeros.length() > 10) numeros = numeros.substring(0, 10);

            StringBuilder formateado = new StringBuilder();
            for (int i = 0; i < numeros.length(); i++) {
                if (i == 3 || i == 6) formateado.append("-");
                formateado.append(numeros.charAt(i));
            }

            if (!newValue.equals(formateado.toString())) {
                txtTelefono.setText(formateado.toString());
            }
        });
    }

    /**
     * Convierte un texto de edad (Ej: "28" o "28 años") en una Fecha de Nacimiento aproximada.
     */
    public static java.time.LocalDate calcularFechaDesdeEdad(String edadTexto) {
        if (edadTexto == null || edadTexto.trim().isEmpty()) {
            return null;
        }
        try {
            // Extrae solo los números y resta los años al día de hoy
            int edad = Integer.parseInt(edadTexto.replaceAll("[^0-9]", ""));
            return java.time.LocalDate.now().minusYears(edad);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Calcula la edad actual en base a una Fecha de Nacimiento.
     */
    public static String calcularEdadDesdeFecha(java.time.LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return "";
        }
        int edad = java.time.Period.between(fechaNacimiento, java.time.LocalDate.now()).getYears();
        return String.valueOf(edad);
    }

    /**
     * Adapta dinámicamente cualquier ventana al tamaño del monitor de la PC que lo esté ejecutando.
     * Evita que los botones de cerrar/minimizar queden fuera de la pantalla.
     */
    public static void configurarVentanaResponsiva(javafx.stage.Stage stage, javafx.scene.Parent root, double anchoDeseado, double altoDeseado) {
        // 1. Obtenemos las dimensiones reales del monitor actual (sin contar la barra de tareas de Windows)
        javafx.geometry.Rectangle2D limitesMonitor = javafx.stage.Screen.getPrimary().getVisualBounds();

        // 2. Elegimos el valor más pequeño: O el tamaño que pedimos, o el 95% de la pantalla disponible
        double anchoFinal = Math.min(anchoDeseado, limitesMonitor.getWidth() * 0.95);
        double altoFinal = Math.min(altoDeseado, limitesMonitor.getHeight() * 0.95);

        // 3. Creamos la escena con las medidas perfectas y se la asignamos a la ventana
        javafx.scene.Scene scene = new javafx.scene.Scene(root, anchoFinal, altoFinal);
        stage.setScene(scene);

        // 4. Candado de seguridad: Nunca podrá ser más grande que el monitor
        stage.setMaxWidth(limitesMonitor.getWidth());
        stage.setMaxHeight(limitesMonitor.getHeight());

        // 5. La centramos perfectamente en la pantalla
        stage.centerOnScreen();
    }

}
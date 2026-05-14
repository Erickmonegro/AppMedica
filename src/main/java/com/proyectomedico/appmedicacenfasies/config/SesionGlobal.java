package com.proyectomedico.appmedicacenfasies.config;

import com.proyectomedico.appmedicacenfasies.model.Usuario;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class SesionGlobal {

    // Getter para obtener el usuario desde cualquier controlador
    @Getter
    private Usuario usuarioLogueado;

    /**
     * Guarda el usuario que acaba de hacer login.
     */
    public void iniciarSesion(Usuario usuario) {
        this.usuarioLogueado = usuario;
    }

    /**
     * Limpia la sesión (útil para el botón "Cerrar Sesión").
     */
    public void cerrarSesion() {
        this.usuarioLogueado = null;
    }

    /**
     * Verifica si hay alguien usando el sistema.
     */
    public boolean haySesionActiva() {
        return usuarioLogueado != null;
    }
}
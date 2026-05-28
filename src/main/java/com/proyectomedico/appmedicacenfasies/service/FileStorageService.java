package com.proyectomedico.appmedicacenfasies.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileStorageService {

    // Spring Boot inyecta los valores de tu application.properties aquí
    @Value("${almacenamiento.servidor.ruta}")
    private String rutaServidor;

    @Value("${almacenamiento.local.ruta}")
    private String rutaLocal;

    /**
     * Crea o verifica la existencia de la carpeta del paciente y devuelve la ruta.
     */
    public Path obtenerOCrearCarpetaPaciente(String cedula, String nombreApellidos) {

        // 1. Manejo seguro de la Cédula (Si es null, usamos un texto por defecto)
        String cedulaSegura = (cedula != null && !cedula.trim().isEmpty()) ? cedula.trim() : "SIN_CEDULA";

        // 2. Manejo seguro del Nombre (Reemplaza caracteres ilegales y recorta espacios finales con .trim())
        String nombreSeguro = nombreApellidos != null
                ? nombreApellidos.replaceAll("[\\\\/:*?\"<>|]", "_").trim()
                : "Paciente_Desconocido";

        // 3. Construcción segura de la carpeta (Usamos guion bajo para evitar conflictos de espacios en Windows)
        String nombreCarpeta = cedulaSegura + "_" + nombreSeguro;

        Path rutaDestino;

        try {
            // INTENTO 1: Guardar en el Servidor
            // MAGIA: Aplicamos .trim() a rutaServidor para limpiar cualquier espacio invisible que venga del application.properties
            rutaDestino = Paths.get(rutaServidor.trim(), nombreCarpeta).normalize();

            if (!Files.exists(rutaDestino)) {
                Files.createDirectories(rutaDestino);
                log.info("Carpeta creada en el Servidor: {}", rutaDestino.toString());
            }
            return rutaDestino;

        } catch (Exception e) {
            log.error("¡ERROR CRÍTICO! No se pudo acceder al Servidor. Activando protocolo de respaldo local.", e);

            // INTENTO 2: Guardar en la PC Local de emergencia
            try {
                // MAGIA: Aplicamos .trim() a rutaLocal también por seguridad
                rutaDestino = Paths.get(rutaLocal.trim(), nombreCarpeta).normalize();

                if (!Files.exists(rutaDestino)) {
                    Files.createDirectories(rutaDestino);
                    log.info("Carpeta creada en el Respaldo Local: {}", rutaDestino.toString());
                }
                return rutaDestino;
            } catch (Exception ex) {
                log.error("Fallo total de almacenamiento. No se pudo guardar ni en servidor ni en local.", ex);
                throw new RuntimeException("Error en sistema de archivos al intentar crear carpeta del paciente.");
            }
        }
    }
}
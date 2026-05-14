package com.proyectomedico.appmedicacenfasies.runner;

import com.proyectomedico.appmedicacenfasies.model.Especialidad;
import com.proyectomedico.appmedicacenfasies.model.Medico;
import com.proyectomedico.appmedicacenfasies.model.Rol;
import com.proyectomedico.appmedicacenfasies.model.Usuario;
import com.proyectomedico.appmedicacenfasies.repository.MedicoRepository;
import com.proyectomedico.appmedicacenfasies.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Verificando usuarios de prueba en la base de datos...");

        // 1. Crear Secretaria si no existe
        if (usuarioRepository.findByUsername("recepcion").isEmpty()) {
            Usuario secretaria = new Usuario();
            secretaria.setUsername("recepcion");
            secretaria.setPassword("1234"); // Nota: En producción esto irá encriptado
            secretaria.setNombreCompleto("Ana Pérez (Secretaria)");
            secretaria.setRol(Rol.SECRETARIO);

            usuarioRepository.save(secretaria);
            log.info("✅ Usuario de Recepción creado exitosamente (User: recepcion | Pass: 1234)");
        }

        // 2. Crear Médico si no existe
        if (usuarioRepository.findByUsername("drMorillo").isEmpty()) {
            Medico medico = new Medico();
            medico.setUsername("drMorillo");
            medico.setPassword("1234");
            medico.setNombreCompleto("Waldo Morillo");
            medico.setRol(Rol.MEDICO);
            medico.setEspecialidad(Especialidad.MEDICOFAMILIAR); // Asegúrate de usar una especialidad de tu Enum
            medico.setExequatur("12346-X");

            medicoRepository.save(medico);
            log.info("✅ Usuario Médico creado exitosamente (User: drprueba | Pass: 1234)");
        }
    }
}
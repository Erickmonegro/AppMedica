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

import java.util.Set;

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
        if (usuarioRepository.findByUsername("draLluberes").isEmpty()) {
            Medico medico1 = new Medico();
            medico1.setUsername("draLluberes");
            medico1.setPassword("1234");
            medico1.setNombreCompleto("Rissy Lluberes");
            medico1.setRol(Rol.MEDICO);
            medico1.setExequatur("12349-X");

            // =========================================================================
            // ¡NUEVO!: ASIGNACIÓN DE MÚLTIPLES ESPECIALIDADES (EL SET)
            // =========================================================================
            // Asegúrate de usar los nombres exactos que tienes en tu Enum 'Especialidad'
            medico1.setEspecialidades(Set.of(Especialidad.SONOGRAFISTA, Especialidad.MEDICOGENERAL));

            medicoRepository.save(medico1);
            log.info("✅ Usuario Médico1 creado exitosamente.");
        }
        if (usuarioRepository.findByUsername("draRodriguez").isEmpty()) {
            Medico medico2 = new Medico();
            medico2.setUsername("draRodriguez");
            medico2.setPassword("1234");
            medico2.setNombreCompleto("Yesenia Rodriguez");
            medico2.setRol(Rol.MEDICO);
            medico2.setExequatur("123410-X");

            // =========================================================================
            // ¡NUEVO!: ASIGNACIÓN DE MÚLTIPLES ESPECIALIDADES (EL SET)
            // =========================================================================
            // Asegúrate de usar los nombres exactos que tienes en tu Enum 'Especialidad'
            medico2.setEspecialidades(Set.of(Especialidad.MEDICOFAMILIAR));

            medicoRepository.save(medico2);
            log.info("✅ Usuario Médico2 creado exitosamente.");
        }
        if (usuarioRepository.findByUsername("draGonzalez").isEmpty()) {
            Medico medico1 = new Medico();
            medico1.setUsername("draGonzalez");
            medico1.setPassword("1234");
            medico1.setNombreCompleto("Yuli Gonzalez");
            medico1.setRol(Rol.MEDICO);
            medico1.setExequatur("123411-X");

            // =========================================================================
            // ¡NUEVO!: ASIGNACIÓN DE MÚLTIPLES ESPECIALIDADES (EL SET)
            // =========================================================================
            // Asegúrate de usar los nombres exactos que tienes en tu Enum 'Especialidad'
            medico1.setEspecialidades(Set.of(Especialidad.GINECOLOGIA));

            medicoRepository.save(medico1);
            log.info("✅ Usuario Médico3 creado exitosamente.");
        }
    }
}
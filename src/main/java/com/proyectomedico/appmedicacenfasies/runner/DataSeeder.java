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
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;

    @Override
    @Transactional // IMPORTANTE: Garantiza que la inyección sea segura
    public void run(String... args) throws Exception {
        log.info("Verificando usuarios y médicos en la base de datos...");

        // 1. Crear Secretaria (Vital para probar el ingreso de pacientes)
        if (usuarioRepository.findByUsername("recepcion").isEmpty()) {
            Usuario secretaria = new Usuario();
            secretaria.setUsername("recepcion");
            secretaria.setPassword("1234"); // En producción irá encriptado
            secretaria.setNombreCompleto("Erick Monegro");
            secretaria.setRol(Rol.SECRETARIO);
            secretaria.setSexo("Masculino"); // Opcional para la secretaria

            usuarioRepository.save(secretaria);
            log.info("✅ Usuario de Recepción creado exitosamente (User: recepcion | Pass: 1234)");
        }

        // 2. Inyección de todos los Médicos del Centro (Añadido el parámetro SEXO)
        registrarMedico("draRodriguez", "1234", "Yesenia Rodriguez", "10001-X", Set.of(Especialidad.MEDICOFAMILIAR), "Femenino");
        registrarMedico("drMorillo", "1234", "Waldo Morillo", "10002-X", Set.of(Especialidad.MEDICOFAMILIAR), "Masculino");
        registrarMedico("draGonzalez", "1234", "Yuli Gonzalez", "10003-X", Set.of(Especialidad.GINECOLOGIA, Especialidad.MEDICOGENERAL), "Femenino");
        registrarMedico("draSantiago", "1234", "Johanna Santiago", "10004-X", Set.of(Especialidad.SONOGRAFISTA, Especialidad.MEDICOGENERAL), "Femenino");
        registrarMedico("draLluberes", "1234", "Rissy Lluberes", "10005-X", Set.of(Especialidad.SONOGRAFISTA, Especialidad.MEDICOGENERAL), "Femenino");
        registrarMedico("drQuezada", "1234", "Calin Quezada", "10006-X", Set.of(Especialidad.SONOGRAFISTA, Especialidad.MEDICOGENERAL), "Masculino");
        registrarMedico("drDaniel", "1234", "Jorge Daniel", "10007-X", Set.of(Especialidad.MEDICOGENERAL), "Masculino");

        log.info("🏁 Inyección de datos finalizada. El sistema está listo para operar.");
    }

    /**
     * Método Auxiliar para inyectar médicos sin repetir código (Principio DRY: Don't Repeat Yourself)
     */
    private void registrarMedico(String username, String password, String nombre, String exequatur, Set<Especialidad> especialidades, String sexo) {
        if (usuarioRepository.findByUsername(username).isEmpty()) {
            Medico medico = new Medico();
            medico.setUsername(username);
            medico.setPassword(password);
            medico.setNombreCompleto(nombre);
            medico.setRol(Rol.MEDICO);
            medico.setExequatur(exequatur);
            medico.setEspecialidades(especialidades);

            // INYECTAMOS EL NUEVO CAMPO AQUÍ 👇
            medico.setSexo(sexo);

            medicoRepository.save(medico);
            log.info("✅ Médico insertado: {} (User: {})", nombre, username);
        }
    }
}
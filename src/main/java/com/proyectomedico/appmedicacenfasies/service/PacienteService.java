package com.proyectomedico.appmedicacenfasies.service;

import com.proyectomedico.appmedicacenfasies.dto.PacienteRegistroDTO;
import com.proyectomedico.appmedicacenfasies.dto.PacienteResumenDTO;
import com.proyectomedico.appmedicacenfasies.model.*;
import com.proyectomedico.appmedicacenfasies.model.HabitosToxicos;
import com.proyectomedico.appmedicacenfasies.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j // Para usar log.info() o log.error()
@Service
@RequiredArgsConstructor // Lombok crea un constructor con los repositorios "final", inyectándolos (Mejor práctica que usar @Autowired)
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ExamenFisicoRepository examenFisicoRepository;
    private final HabitosToxicosRepository habitosToxicosRepository;
    private final AntecedentesPacienteRepository antecedentesRepository;
    private final HojaEvolucionRepository hojaEvolucionRepository;

    /**
     * Registra un paciente nuevo junto con su historial base (Examen, Hábitos, Antecedentes).
     * @Transactional asegura que o se guarda todo (Paciente y sus dependencias), o no se guarda nada.
     */
    @Transactional
    public Paciente registrarNuevoPaciente(Paciente paciente,
                                           ExamenFisico examenFisico,
                                           HabitosToxicos habitos,
                                           AntecedentesPaciente antecedentes) {

        log.info("Iniciando registro para el paciente con cédula: {}", paciente.getCedula());

        // 1. Regla de Negocio: Validar duplicados
        if (pacienteRepository.existsByCedula(paciente.getCedula())) {
            log.error("Fallo el registro. La cédula ya existe en el sistema.");
            throw new RuntimeException("Ya existe un paciente con la cédula: " + paciente.getCedula());
        }

        // 2. Cálculo de Negocio (Ejemplo IMC)
        if (examenFisico != null && examenFisico.getPeso() != null && examenFisico.getTalla() != null) {
            double tallaMetros = examenFisico.getTalla(); // Asumiendo que viene en metros
            double imc = examenFisico.getPeso() / (tallaMetros * tallaMetros);
            examenFisico.setImc(Math.round(imc * 10.0) / 10.0); // Redondeo a 1 decimal
        }

        // 3. Guardar el Paciente "Padre" (Se genera su UUID en la DB)
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        // 4. Conectar y guardar Entidades "Hijas"
        if (examenFisico != null) {
            examenFisico.setPaciente(pacienteGuardado); // Vínculo FK
            examenFisicoRepository.save(examenFisico);
        }

        if (habitos != null) {
            habitos.setPaciente(pacienteGuardado); // Vínculo FK
            habitosToxicosRepository.save(habitos);
        }

        if (antecedentes != null) {
            antecedentes.setPaciente(pacienteGuardado); // Vínculo FK
            antecedentesRepository.save(antecedentes);
        }

        log.info("Paciente y registros asociados guardados exitosamente. ID: {}", pacienteGuardado.getId());

        // 5. ¡AQUÍ ENTRARÁ LA LÓGICA DE GENERAR EL PDF!
        // generarYGuardarPdfHistoriaClinica(pacienteGuardado, examenFisico, habitos, antecedentes);

        return pacienteGuardado;
    }

// ... (tu código anterior de registrarNuevoPaciente) ...

    /**
     * Convierte una entidad Paciente a un DTO ligero.
     * (Método privado de utilidad interna)
     */
    private PacienteResumenDTO mapearAResumen(Paciente paciente) {
        return new PacienteResumenDTO(
                paciente.getId(),
                paciente.getNombreApellidos(),
                paciente.getCedula()
        );
    }

    /**
     * Búsqueda inteligente: Si no hay filtro, trae todos. Si hay, filtra por nombre o cédula.
     */
    public List<PacienteResumenDTO> buscarPacientes(String filtro) {
        List<Paciente> pacientes;

        if (filtro == null || filtro.trim().isEmpty()) {
            // Si el buscador está vacío, traemos todos los pacientes de la base de datos
            pacientes = pacienteRepository.findAll();
        } else {
            // Usamos el método que acabas de corregir en el Repositorio
            pacientes = pacienteRepository.findByNombreApellidosContainingIgnoreCaseOrCedulaContaining(filtro, filtro);
        }

        // Convertimos la lista de "Pacientes" a una lista de "PacienteResumenDTO"
        return pacientes.stream()
                .map(this::mapearAResumen)
                .toList();
    }

    /**
     * Obtiene TODOS los datos de un paciente para llenar la Hoja Clínica.
     */
    public Paciente obtenerDetalleCompleto(UUID pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con el ID: " + pacienteId));
    }

    // Importante: Asegúrate de importar org.springframework.transaction.annotation.Transactional;
    @Transactional
    public void registrarNuevaHistoriaClinica(PacienteRegistroDTO dto) {
        log.info("Iniciando transacción segura para el paciente: {}", dto.datosPersonales().cedula());

        // 1. EXTRAER Y GUARDAR EL PACIENTE PRINCIPAL
        Paciente paciente = new Paciente();
        paciente.setNombreApellidos(dto.datosPersonales().nombreApellidos());
        paciente.setCedula(dto.datosPersonales().cedula());
        paciente.setFechaNacimiento(dto.datosPersonales().fechaNacimiento());
        paciente.setDireccion(dto.datosPersonales().direccion());
        paciente.setTelefonos(dto.datosPersonales().telefonos());
        paciente.setSeguro(dto.datosPersonales().seguro());
        paciente.setOcupacion(dto.datosPersonales().ocupacion());
        // (Agrega sexo, tipo de sangre, etc., cuando los incluyas en la UI)

        // Guardamos el paciente primero para que PostgreSQL le asigne su UUID
        paciente = pacienteRepository.save(paciente);

        // 2. EXTRAER Y GUARDAR ANTECEDENTES ASOCIADOS
        AntecedentesPaciente antecedentes = new AntecedentesPaciente();
        antecedentes.setPaciente(paciente); // ¡Clave para la relación (Foreign Key)!
        antecedentes.setAntecedentesFamiliares(dto.antecedentes().antecedentesFamiliares());
        antecedentes.setAntecedentesPersonales(dto.antecedentes().antecedentesPersonales());
        antecedentes.setAlergias(dto.antecedentes().alergias());
        antecedentes.setCirugias(dto.antecedentes().cirugias());
        antecedentesRepository.save(antecedentes); // Descomenta si tienes este repositorio

        // 3. EXTRAER Y GUARDAR HÁBITOS TÓXICOS
        HabitosToxicos habitos = new HabitosToxicos();
        habitos.setPaciente(paciente);
        habitos.setTabaco(dto.habitos().tabaco());
        habitos.setAlcohol(dto.habitos().alcohol());
        habitos.setCafe(dto.habitos().cafe());
        habitos.setDrogas(dto.habitos().drogas());
        habitos.setHooka(dto.habitos().hooka());
        habitos.setCigarroElectronico(dto.habitos().cigarrilloElectronico());
        habitosToxicosRepository.save(habitos); // Descomenta si tienes este repositorio



        log.info("Transacción completada. Historia clínica guardada con éxito.");
    }


}
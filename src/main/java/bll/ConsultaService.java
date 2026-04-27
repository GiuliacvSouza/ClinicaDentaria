package bll;

import dal.ConsultaRepository;
import dal.DentistaRepository;
import dal.PacienteRepository;
import dal.ProcedimentoRepository;
import dal.AtendimentoProcedimentoRepository;
import bll.AtendimentoService;
import jakarta.transaction.Transactional;
import model.Consulta;
import model.Dentista;
import model.Paciente;
import model.Utilizador;
import model.dto.ConsultaAgendadaDTO;
import model.enums.EstadoConsulta;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultaService {

    private final ConsultaRepository repository;
    private final PacienteRepository pacienteRepository;
    private final DentistaRepository dentistaRepository;
    private final AtendimentoService atendimentoService;
    private final ProcedimentoRepository procedimentoRepository;
    private final AtendimentoProcedimentoRepository atendimentoProcedimentoRepository;

    public ConsultaService(
            ConsultaRepository repository,
            PacienteRepository pacienteRepository,
            DentistaRepository dentistaRepository,
            AtendimentoService atendimentoService,
            ProcedimentoRepository procedimentoRepository,
            AtendimentoProcedimentoRepository atendimentoProcedimentoRepository
    ) {
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;
        this.dentistaRepository = dentistaRepository;
        this.atendimentoService = atendimentoService;
        this.procedimentoRepository = procedimentoRepository;
        this.atendimentoProcedimentoRepository = atendimentoProcedimentoRepository;
    }

    @Transactional
    public Consulta agendarConsulta(Consulta consulta) {
        if (consulta.getIdPaciente() == null || consulta.getIdPaciente().getId() == null) {
            throw new RuntimeException("Consulta deve ter um paciente.");
        }
        if (consulta.getIdDentista() == null || consulta.getIdDentista().getId() == null) {
            throw new RuntimeException("Consulta deve ter um dentista.");
        }
        if (consulta.getDataHoraInicio() == null || consulta.getDataHoraInicio().isBefore(Instant.now())) {
            throw new RuntimeException("Nao e possivel agendar consulta no passado.");
        }

        Paciente paciente = pacienteRepository.findById(consulta.getIdPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Paciente nao encontrado."));
        Dentista dentista = dentistaRepository.findById(consulta.getIdDentista().getId())
                .orElseThrow(() -> new RuntimeException("Dentista nao encontrado."));

        consulta.setIdPaciente(paciente);
        consulta.setIdDentista(dentista);
        validarConflitoHorario(consulta, consulta.getDataHoraInicio());

        Consulta consultaGuardada = repository.saveAndFlush(consulta);
        if (consultaGuardada.getId() == null) {
            throw new RuntimeException("Nao foi possivel guardar a consulta.");
        }

        return consultaGuardada;
    }

    public List<Consulta> listarTodas() {
        return repository.findAllEager();
    }

    public List<ConsultaAgendadaDTO> listarTodasAgendadas() {
        try {
            List<ConsultaAgendadaDTO> consultas = repository.findAllAgendadas();
            if (!consultas.isEmpty() || repository.count() == 0) {
                return consultas;
            }
        } catch (Exception ignored) {
            // Fallback para proteger a agenda quando a query DTO falha silenciosamente.
        }

        return repository.findAllEager().stream()
                .sorted(Comparator.comparing(Consulta::getDataHoraInicio, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toConsultaAgendadaDTO)
                .collect(Collectors.toList());
    }

    public List<Consulta> listarPorStatus(EstadoConsulta status) {
        return repository.findByStatus(status);
    }

    public List<Consulta> listarPorPaciente(Integer pacienteId) {
        if (pacienteId == null) {
            return List.of();
        }
        return repository.findByPacienteIdEager(pacienteId);
    }

    public List<ConsultaAgendadaDTO> listarPorStatusAgendadas(EstadoConsulta status) {
        try {
            List<ConsultaAgendadaDTO> consultas = repository.findByStatusAgendadas(status);
            if (!consultas.isEmpty() || repository.findByStatus(status).isEmpty()) {
                return consultas;
            }
        } catch (Exception ignored) {
            // Fallback para manter a agenda funcional mesmo com problema na query DTO.
        }

        return repository.findByStatus(status).stream()
                .sorted(Comparator.comparing(Consulta::getDataHoraInicio, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toConsultaAgendadaDTO)
                .collect(Collectors.toList());
    }

    public List<Consulta> listarPorDentistaEDia(Integer dentistaId, LocalDate data) {
        if (dentistaId == null || data == null) {
            return List.of();
        }

        Instant inicio = data.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant fim = data.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();
        return repository.findByDentistaEDia(dentistaId, inicio, fim);
    }

    public Consulta buscarPorId(Integer id) {
        return repository.findByIdEager(id)
                .orElseThrow(() -> new RuntimeException("Consulta nao encontrada"));
    }

    public Consulta atualizar(Consulta consulta) {
        buscarPorId(consulta.getId());
        return repository.save(consulta);
    }

    @Transactional
    public Consulta cancelar(Integer id) {
        Consulta consulta = buscarPorId(id);
        validarTransicao(consulta.getStatus(), EstadoConsulta.CANCELADA);
        consulta.setStatus(EstadoConsulta.CANCELADA);
        consulta.setDataCancelamento(LocalDate.now());
        consulta.setMotivoCancelamento("Cancelada pela agenda.");
        return repository.save(consulta);
    }

    @Transactional
    public Consulta reagendar(Integer id, Instant novaDataHoraInicio) {
        Consulta consulta = buscarPorId(id);
        validarTransicao(consulta.getStatus(), EstadoConsulta.AGENDADA);

        if (novaDataHoraInicio == null) {
            throw new RuntimeException("A nova data da consulta e obrigatoria.");
        }
        if (novaDataHoraInicio.isBefore(Instant.now())) {
            throw new RuntimeException("Nao e possivel reagendar consulta para o passado.");
        }

        validarConflitoHorario(consulta, novaDataHoraInicio);

        consulta.setDataHoraInicio(novaDataHoraInicio);
        consulta.setDataMarcacao(LocalDate.now());
        consulta.setDataCancelamento(null);
        consulta.setMotivoCancelamento(null);
        return repository.save(consulta);
    }

    @Transactional
    public Consulta marcarChegada(Integer id) {
        return atualizarStatus(id, EstadoConsulta.EM_ESPERA);
    }

    @Transactional
    public Consulta confirmarConsulta(Integer id) {
        return atualizarStatus(id, EstadoConsulta.CONFIRMADA);
    }

    @Transactional
    public Consulta iniciarConsulta(Integer id) {
        return atualizarStatus(id, EstadoConsulta.EM_CONSULTA);
    }

    @Transactional
    public Consulta finalizarConsulta(Integer id) {
        Consulta consulta = atualizarStatus(id, EstadoConsulta.CONCLUIDA);

        // Se ainda não existir atendimento associado, criar um atendimento básico
        try {
            if (atendimentoService.buscarPorConsulta(consulta) == null) {
                model.Atendimento at = new model.Atendimento();
                at.setIdConsulta(consulta);
                at.setRetorno(false);
                at.setDiagnostico("Atendimento criado automaticamente ao concluir a consulta.");
                model.Atendimento salvo = atendimentoService.salvar(at);

                // Tentar associar um procedimento automaticamente baseado no tipo/observacoes da consulta
                try {
                    String nomeProc = resolverProcedimento(consulta);
                    if (nomeProc != null && !nomeProc.isBlank()) {
                        var procs = procedimentoRepository.findByNomeContainingIgnoreCase(nomeProc);
                        if (procs != null && !procs.isEmpty()) {
                            model.Procedimento proc = procs.get(0);
                            model.AtendimentoProcedimento ap = new model.AtendimentoProcedimento();
                            ap.setIdAtendimento(salvo);
                            ap.setIdProcedimento(proc);
                            ap.setQuantidade(1);
                            ap.setDesconto(java.math.BigDecimal.ZERO);
                            atendimentoProcedimentoRepository.save(ap);
                        }
                    }
                } catch (RuntimeException ignoredInner) {
                    // Não bloquear fluxo principal se não for possível mapear procedimento
                }
            }
        } catch (RuntimeException ignored) {
            // Não impedir a finalização da consulta se a criação do atendimento falhar
        }

        return consulta;
    }

    @Transactional
    public Consulta faturarConsulta(Integer id) {
        return atualizarStatus(id, EstadoConsulta.FATURADA);
    }

    @Transactional
    public Consulta atualizarStatus(Integer id, EstadoConsulta novoStatus) {
        Consulta consulta = buscarPorId(id);
        validarTransicao(consulta.getStatus(), novoStatus);
        consulta.setStatus(novoStatus);

        if (novoStatus != EstadoConsulta.CANCELADA) {
            consulta.setDataCancelamento(null);
            consulta.setMotivoCancelamento(null);
        }

        return repository.save(consulta);
    }

    private ConsultaAgendadaDTO toConsultaAgendadaDTO(Consulta consulta) {
        return new ConsultaAgendadaDTO(
                consulta.getId(),
                getNomePaciente(consulta.getIdPaciente()),
                getNomeDentista(consulta.getIdDentista()),
                resolverProcedimento(consulta),
                consulta.getDataHoraInicio(),
                consulta.getStatus()
        );
    }

    private String getNomePaciente(Paciente paciente) {
        return formatarNome(paciente != null ? paciente.getUtilizador() : null);
    }

    private String getNomeDentista(Dentista dentista) {
        return formatarNome(dentista != null ? dentista.getUtilizador() : null);
    }

    private String formatarNome(Utilizador utilizador) {
        if (utilizador == null) {
            return null;
        }

        String primeiroNome = utilizador.getPrimeiroNome() != null ? utilizador.getPrimeiroNome().trim() : "";
        String ultimoNome = utilizador.getUltimoNome() != null ? utilizador.getUltimoNome().trim() : "";
        String nomeCompleto = (primeiroNome + " " + ultimoNome).trim();
        return nomeCompleto.isEmpty() ? null : nomeCompleto;
    }

    private String resolverProcedimento(Consulta consulta) {
        if (consulta == null) {
            return null;
        }

        String procedimentoNasObservacoes = extrairLinhaPrefixada(consulta.getObservacoes(), "Procedimento:");
        if (procedimentoNasObservacoes != null && !procedimentoNasObservacoes.isBlank()) {
            return procedimentoNasObservacoes;
        }

        return consulta.getTipo();
    }

    private String extrairLinhaPrefixada(String texto, String prefixo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        for (String linha : texto.split("\\R")) {
            if (linha != null && linha.startsWith(prefixo)) {
                return linha.substring(prefixo.length()).trim();
            }
        }
        return null;
    }

    private void validarTransicao(EstadoConsulta statusAtual, EstadoConsulta novoStatus) {
        if (statusAtual == null || novoStatus == null) {
            throw new RuntimeException("Status da consulta invalido.");
        }

        if (statusAtual == novoStatus) {
            return;
        }

        boolean transicaoValida = switch (statusAtual) {
            case AGENDADA -> novoStatus == EstadoConsulta.AGENDADA
                    || novoStatus == EstadoConsulta.CONFIRMADA
                    || novoStatus == EstadoConsulta.CANCELADA;
            case CONFIRMADA -> novoStatus == EstadoConsulta.EM_ESPERA;
            case EM_ESPERA -> novoStatus == EstadoConsulta.EM_CONSULTA;
            case EM_CONSULTA -> novoStatus == EstadoConsulta.CONCLUIDA;
            case CONCLUIDA -> novoStatus == EstadoConsulta.FATURADA;
            case FATURADA, CANCELADA, FALTA, PENDENTE, EM_ATENDIMENTO -> false;
        };

        if (!transicaoValida) {
            throw new RuntimeException("Transicao de estado invalida: " + statusAtual + " -> " + novoStatus);
        }
    }

    private void validarConflitoHorario(Consulta consulta, Instant novaDataHoraInicio) {
        if (consulta.getIdDentista() == null || consulta.getIdDentista().getId() == null) {
            throw new RuntimeException("Consulta deve ter um dentista.");
        }

        try {
            boolean horarioOcupado = repository.findConflitoHorario(consulta.getIdDentista().getId(), novaDataHoraInicio).stream()
                    .anyMatch(conflito -> !conflito.getId().equals(consulta.getId()));

            if (horarioOcupado) {
                throw new RuntimeException("O dentista ja possui consulta marcada para esse horario.");
            }
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Nao foi possivel validar o novo horario da consulta.", ex);
        }
    }
}

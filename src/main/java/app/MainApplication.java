package app;

import bll.*;
import dal.*;
import model.*;
import model.enums.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@EntityScan(basePackages = "model")
@SpringBootApplication
@ComponentScan(basePackages = {"app", "bll", "dal", "model", "controller"})
@EnableJpaRepositories(basePackages = "dal")

public class MainApplication implements CommandLineRunner {

    // Services
    private final UtilizadorService utilizadorService;
    private final PacienteService pacienteService;
    private final DentistaService dentistaService;
    private final AssistenteService assistenteService;
    private final RecepcionistaService recepcionistaService;
    private final SeguroService seguroService;
    private final PacientexSeguroService pacientexSeguroService;
    private final ContatoEmergenciaService contatoEmergenciaService;
    private final ProntuarioService prontuarioService;
    private final ConsultaService consultaService;
    private final AtendimentoService atendimentoService;
    private final FaturaService faturaService;
    private final PagamentoService pagamentoService;

    // Repositories
    private final AtendimentoRepository atendimentoRepository;
    private final AssistenteRepository assistenteRepository;
    private final AtendimentoProcedimentoRepository atendimentoProcedimentoRepository;
    private final CodigoPostalRepository codigoPostalRepository;
    private final ConsultaRepository consultaRepository;
    private final ContatoEmergenciaRepository contatoEmergenciaRepository;
    private final DentistaRepository dentistaRepository;
    private final DoencaRepository doencaRepository;
    private final EspecialidadeDentistaRepository especialidadeDentistaRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final EspecialidadexAssistenteRepository especialidadexAssistenteRepository;
    private final EspecialidadexDentistaRepository especialidadexDentistaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final MaterialRepository materialRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final PacienteRepository pacienteRepository;
    private final PacientexSeguroRepository pacientexSeguroRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PedidoCompraRepository pedidoCompraRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final ProntuarioRepository prontuarioRepository;
    private final RecepcionistaRepository recepcionistaRepository;
    private final SeguroRepository seguroRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final AlergiaRepository alergiaRepository;
    private final AnamneseAlergiaRepository anamneseAlergiaRepository;
    private final AnamneseDoencaRepository anamneseDoencaRepository;
    private final AnamneseMedicamentoRepository anamneseMedicamentoRepository;
    private final AnamneseRepository anamneseRepository;
    private final FaturaRepository faturaRepository;

    // Construtor com todas as dependências
    public MainApplication(
            UtilizadorService utilizadorService,
            PacienteService pacienteService,
            DentistaService dentistaService,
            AssistenteService assistenteService,
            RecepcionistaService recepcionistaService,
            SeguroService seguroService,
            PacientexSeguroService pacientexSeguroService,
            ContatoEmergenciaService contatoEmergenciaService,
            ProntuarioService prontuarioService,
            ConsultaService consultaService,
            AtendimentoService atendimentoService,
            FaturaService faturaService,
            PagamentoService pagamentoService,
            AtendimentoRepository atendimentoRepository,
            AssistenteRepository assistenteRepository,
            AtendimentoProcedimentoRepository atendimentoProcedimentoRepository,
            CodigoPostalRepository codigoPostalRepository,
            ConsultaRepository consultaRepository,
            ContatoEmergenciaRepository contatoEmergenciaRepository,
            DentistaRepository dentistaRepository,
            DoencaRepository doencaRepository,
            EspecialidadeDentistaRepository especialidadeDentistaRepository,
            EspecialidadeRepository especialidadeRepository,
            EspecialidadexAssistenteRepository especialidadexAssistenteRepository,
            EspecialidadexDentistaRepository especialidadexDentistaRepository,
            FornecedorRepository fornecedorRepository,
            ItemPedidoRepository itemPedidoRepository,
            MaterialRepository materialRepository,
            MedicamentoRepository medicamentoRepository,
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            PacienteRepository pacienteRepository,
            PacientexSeguroRepository pacientexSeguroRepository,
            PagamentoRepository pagamentoRepository,
            PedidoCompraRepository pedidoCompraRepository,
            ProcedimentoRepository procedimentoRepository,
            ProntuarioRepository prontuarioRepository,
            RecepcionistaRepository recepcionistaRepository,
            SeguroRepository seguroRepository,
            UtilizadorRepository utilizadorRepository,
            AlergiaRepository alergiaRepository,
            AnamneseAlergiaRepository anamneseAlergiaRepository,
            AnamneseDoencaRepository anamneseDoencaRepository,
            AnamneseMedicamentoRepository anamneseMedicamentoRepository,
            AnamneseRepository anamneseRepository,
            FaturaRepository faturaRepository
    ) {
        // Services
        this.utilizadorService = utilizadorService;
        this.pacienteService = pacienteService;
        this.dentistaService = dentistaService;
        this.assistenteService = assistenteService;
        this.recepcionistaService = recepcionistaService;
        this.seguroService = seguroService;
        this.pacientexSeguroService = pacientexSeguroService;
        this.contatoEmergenciaService = contatoEmergenciaService;
        this.prontuarioService = prontuarioService;
        this.consultaService = consultaService;
        this.atendimentoService = atendimentoService;
        this.faturaService = faturaService;
        this.pagamentoService = pagamentoService;

        // Repositories
        this.atendimentoRepository = atendimentoRepository;
        this.assistenteRepository = assistenteRepository;
        this.atendimentoProcedimentoRepository = atendimentoProcedimentoRepository;
        this.codigoPostalRepository = codigoPostalRepository;
        this.consultaRepository = consultaRepository;
        this.contatoEmergenciaRepository = contatoEmergenciaRepository;
        this.dentistaRepository = dentistaRepository;
        this.doencaRepository = doencaRepository;
        this.especialidadeDentistaRepository = especialidadeDentistaRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.especialidadexAssistenteRepository = especialidadexAssistenteRepository;
        this.especialidadexDentistaRepository = especialidadexDentistaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.materialRepository = materialRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.pacienteRepository = pacienteRepository;
        this.pacientexSeguroRepository = pacientexSeguroRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoCompraRepository = pedidoCompraRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.prontuarioRepository = prontuarioRepository;
        this.recepcionistaRepository = recepcionistaRepository;
        this.seguroRepository = seguroRepository;
        this.utilizadorRepository = utilizadorRepository;
        this.alergiaRepository = alergiaRepository;
        this.anamneseAlergiaRepository = anamneseAlergiaRepository;
        this.anamneseDoencaRepository = anamneseDoencaRepository;
        this.anamneseMedicamentoRepository = anamneseMedicamentoRepository;
        this.anamneseRepository = anamneseRepository;
        this.faturaRepository = faturaRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void secao(String titulo) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf("║  %-36s║%n", titulo);
        System.out.println("╚══════════════════════════════════════╝");
    }

    private static void ok(String msg) {
        System.out.println("  ✔ " + msg);
    }

    private static void erro(String msg) {
        System.out.println(" ERRO: " + msg);
    }

    private Utilizador criarUtilizador(String nome, String apelido, String tipo) {
        Utilizador u = new Utilizador();
        u.setPrimeiroNome(nome);
        u.setUltimoNome(apelido);
        u.setEmail(nome.toLowerCase() + "." + apelido.toLowerCase()
                + "." + System.nanoTime() + "@clinica.pt");
        u.setTipoUtilizador(tipo);
        u.setSenha("Clinica2025!");
        u.setStatus("ATIVO");
        return utilizadorService.salvar(u);
    }

    private AtendimentoProcedimento criarAtendimentoProcedimento(
            Atendimento atendimento,
            Procedimento procedimento,
            int quantidade,
            BigDecimal desconto
    ) {
        AtendimentoProcedimento atendimentoProcedimento = new AtendimentoProcedimento();
        AtendimentoProcedimentoId id = new AtendimentoProcedimentoId();
        id.setIdAtendimento(atendimento.getId());
        id.setIdProcedimento(procedimento.getId());

        atendimentoProcedimento.setId(id);
        atendimentoProcedimento.setIdAtendimento(atendimento);
        atendimentoProcedimento.setIdProcedimento(procedimento);
        atendimentoProcedimento.setQuantidade(quantidade);
        atendimentoProcedimento.setDesconto(desconto != null ? desconto : BigDecimal.ZERO);

        return atendimentoProcedimento;
    }

    // ── Runner ────────────────────────────────────────────────────────────────

    @Override
    public void run(String... args) {

        Utilizador uPaciente = null;
        Utilizador uDentista = null;
        Utilizador uAssistente = null;
        Utilizador uRecepcionista = null;
        Paciente paciente = null;
        Dentista dentista = null;
        Seguro seguro = null;
        Consulta consulta = null;
        Atendimento atendimento = null;
        Fatura fatura = null;

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 1 - UTILIZADORES
        // ══════════════════════════════════════════════════════════════════════
        secao("1. UTILIZADORES");

        try {
            uPaciente = criarUtilizador("Ana", "Costa", "PACIENTE");
            uDentista = criarUtilizador("Carlos", "Ferreira", "DENTISTA");
            uAssistente = criarUtilizador("Sofia", "Lopes", "ASSISTENTE");
            uRecepcionista = criarUtilizador("Rui", "Santos", "RECEPCIONISTA");
            ok("4 utilizadores criados (IDs: "
                    + uPaciente.getId() + ", "
                    + uDentista.getId() + ", "
                    + uAssistente.getId() + ", "
                    + uRecepcionista.getId() + ")");
        } catch (Exception e) {
            erro(e.getMessage());
        }

        // Email duplicado deve ser rejeitado
        try {
            Utilizador dup = new Utilizador();
            dup.setPrimeiroNome("Dup");
            dup.setUltimoNome("Teste");
            dup.setEmail(uPaciente != null ? uPaciente.getEmail() : "dup@clinica.pt");
            dup.setTipoUtilizador("PACIENTE");
            dup.setSenha("Clinica2025!");
            dup.setStatus("ATIVO");
            utilizadorService.salvar(dup);
            erro("Deveria ter rejeitado email duplicado!");
        } catch (Exception e) {
            ok("Email duplicado rejeitado: " + e.getMessage());
        }

        // Nome obrigatorio
        try {
            Utilizador sem = new Utilizador();
            sem.setEmail("semNome@clinica.pt");
            sem.setSenha("Clinica2025!");
            sem.setStatus("ATIVO");
            utilizadorService.salvar(sem);
            erro("Deveria ter rejeitado utilizador sem nome!");
        } catch (Exception e) {
            ok("Nome obrigatorio validado: " + e.getMessage());
        }

        // Senha obrigatoria
        try {
            Utilizador semSenha = new Utilizador();
            semSenha.setPrimeiroNome("Sem");
            semSenha.setUltimoNome("Senha");
            semSenha.setEmail("semsenha." + System.nanoTime() + "@clinica.pt");
            semSenha.setTipoUtilizador("PACIENTE");
            semSenha.setStatus("ATIVO");
            utilizadorService.salvar(semSenha);
            erro("Deveria ter rejeitado utilizador sem senha!");
        } catch (Exception e) {
            ok("Senha obrigatoria validada: " + e.getMessage());
        }

        try {
            ok("Total de utilizadores na BD: " + utilizadorService.listarTodos().size());
        } catch (Exception e) {
            erro(e.getMessage());
        }

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 2 - PACIENTE
        // ══════════════════════════════════════════════════════════════════════
        secao("2. PACIENTE");

        try {
            if (uPaciente == null) throw new Exception("Utilizador paciente nao disponivel");
            Utilizador managed = utilizadorService.buscarPorId(uPaciente.getId());
            Paciente p = new Paciente();
            p.setUtilizador(managed);
            p.setStatus("ATIVO");
            p.setDataRegisto(LocalDate.now());
            paciente = pacienteService.salvar(p);
            ok("Paciente criado com ID: " + paciente.getId());
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            if (uPaciente == null) throw new Exception("Utilizador paciente nao disponivel");
            Utilizador managed = utilizadorService.buscarPorId(uPaciente.getId());
            Paciente pFut = new Paciente();
            pFut.setUtilizador(managed);
            pFut.setStatus("ATIVO");
            pFut.setDataRegisto(LocalDate.now().plusDays(1));
            pacienteService.salvar(pFut);
            erro("Deveria ter rejeitado data futura!");
        } catch (Exception e) {
            ok("Data de registo futura rejeitada: " + e.getMessage());
        }

        try {
            if (uPaciente == null) throw new Exception("Utilizador paciente nao disponivel");
            Utilizador managed = utilizadorService.buscarPorId(uPaciente.getId());
            Paciente pSem = new Paciente();
            pSem.setUtilizador(managed);
            pacienteService.salvar(pSem);
            erro("Deveria ter rejeitado status vazio!");
        } catch (Exception e) {
            ok("Status obrigatorio validado: " + e.getMessage());
        }

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 3 - DENTISTA
        // ══════════════════════════════════════════════════════════════════════
        secao("3. DENTISTA");

        try {
            if (uDentista == null) throw new Exception("Utilizador dentista nao disponivel");
            Utilizador managed = utilizadorService.buscarPorId(uDentista.getId());
            Dentista d = new Dentista();
            d.setUtilizador(managed);
            d.setNumeroOmd("OMD12345");
            d.setDataAdmissao(LocalDate.now().minusYears(2));
            d.setHorarioEntrada(LocalTime.of(9, 0));
            d.setHorarioSaida(LocalTime.of(18, 0));
            d.setAtivo(true);
            dentista = dentistaService.salvar(d);
            ok("Dentista criado com ID: " + dentista.getId());
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            if (uDentista == null) throw new Exception("Utilizador dentista nao disponivel");
            Utilizador managed = utilizadorService.buscarPorId(uDentista.getId());
            Dentista dInv = new Dentista();
            dInv.setUtilizador(managed);
            dInv.setNumeroOmd("OMD99999");
            dInv.setHorarioEntrada(LocalTime.of(18, 0));
            dInv.setHorarioSaida(LocalTime.of(9, 0));
            dentistaService.salvar(dInv);
            erro("Deveria ter rejeitado horario invalido!");
        } catch (Exception e) {
            ok("Horario invalido rejeitado: " + e.getMessage());
        }

        try {
            if (uDentista == null) throw new Exception("Utilizador dentista nao disponivel");
            Utilizador managed = utilizadorService.buscarPorId(uDentista.getId());
            Dentista dSem = new Dentista();
            dSem.setUtilizador(managed);
            dentistaService.salvar(dSem);
            erro("Deveria ter rejeitado dentista sem OMD!");
        } catch (Exception e) {
            ok("OMD obrigatorio validado: " + e.getMessage());
        }

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 4 - ASSISTENTE & RECEPCIONISTA
        // ══════════════════════════════════════════════════════════════════════
        secao("4. ASSISTENTE & RECEPCIONISTA");

        try {
            if (uAssistente == null) throw new Exception("Utilizador assistente nao disponivel");
            Utilizador managed = utilizadorService.buscarPorId(uAssistente.getId());
            Assistente a = new Assistente();
            a.setUtilizador(managed);
            a.setNivelFormacao(NivelFormacao.SENIOR);
            a.setDataAdmissao(LocalDate.now().minusMonths(6));
            a.setAtivo(true);
            assistenteService.salvar(a);
            ok("Assistente criado");
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            if (uRecepcionista == null) throw new Exception("Utilizador recepcionista nao disponivel");
            Utilizador managed = utilizadorService.buscarPorId(uRecepcionista.getId());
            Recepcionista r = new Recepcionista();
            r.setUtilizador(managed);
            r.setDataAdmissao(LocalDate.now().minusMonths(3));
            r.setTurno(Turno.MANHA);
            recepcionistaService.salvar(r);
            ok("Recepcionista criado");
        } catch (Exception e) {
            erro(e.getMessage());
        }

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 5 - SEGURO & PACIENTE x SEGURO
        // ══════════════════════════════════════════════════════════════════════
        secao("5. SEGURO & PACIENTE×SEGURO");

        try {
            Seguro s = new Seguro();
            s.setNomeSeguro("Medicare Plus");
            s.setCodigoPlano("MED-2025");
            s.setTipoPlano("COMPLETO");
            s.setValidoAte(LocalDate.now().plusYears(1));
            seguro = seguroService.salvar(s);
            ok("Seguro criado com ID: " + seguro.getId());
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            if (paciente == null || seguro == null)
                throw new Exception("Paciente ou seguro nao disponivel");
            PacientexSeguroId psId = new PacientexSeguroId();
            psId.setIdUtilizador(paciente.getId());
            psId.setIdSeguro(seguro.getId());
            PacientexSeguro ps = new PacientexSeguro();
            ps.setId(psId);
            ps.setIdUtilizador(pacienteService.buscarPorId(paciente.getId()));
            ps.setIdSeguro(seguroService.buscarPorId(seguro.getId()));
            ps.setNumeroApolice("APOLICE-001");
            ps.setDataInicioCobertura(LocalDate.now());
            ps.setDataFimCobertura(LocalDate.now().plusYears(1));
            pacientexSeguroService.salvar(ps);
            ok("Paciente associado ao seguro");
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            if (paciente == null || seguro == null)
                throw new Exception("Paciente ou seguro nao disponivel");
            PacientexSeguroId psId2 = new PacientexSeguroId();
            psId2.setIdUtilizador(paciente.getId());
            psId2.setIdSeguro(seguro.getId());
            PacientexSeguro psInv = new PacientexSeguro();
            psInv.setId(psId2);
            psInv.setIdUtilizador(pacienteService.buscarPorId(paciente.getId()));
            psInv.setIdSeguro(seguroService.buscarPorId(seguro.getId()));
            psInv.setDataInicioCobertura(LocalDate.now().plusYears(1));
            psInv.setDataFimCobertura(LocalDate.now());
            pacientexSeguroService.salvar(psInv);
            erro("Deveria ter rejeitado datas invertidas!");
        } catch (Exception e) {
            ok("Datas de cobertura invertidas rejeitadas: " + e.getMessage());
        }

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 6 - CONTATO DE EMERGENCIA
        // ══════════════════════════════════════════════════════════════════════
        secao("6. CONTATO DE EMERGÊNCIA");

        try {
            if (paciente == null) throw new Exception("Paciente nao disponivel");
            ContatoEmergencia ce = new ContatoEmergencia();
            ce.setPaciente(pacienteService.buscarPorId(paciente.getId()));
            ce.setPrimeiroNome("Maria");
            ce.setUltimoNome("Costa");
            contatoEmergenciaService.salvar(ce);
            ok("Contato de emergencia criado");
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            if (paciente == null) throw new Exception("Paciente nao disponivel");
            ContatoEmergencia ceSem = new ContatoEmergencia();
            ceSem.setPaciente(pacienteService.buscarPorId(paciente.getId()));
            contatoEmergenciaService.salvar(ceSem);
            erro("Deveria ter rejeitado contato sem nome!");
        } catch (Exception e) {
            ok("Nome de contato obrigatorio validado: " + e.getMessage());
        }

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 7 - PRONTUARIO
        // ══════════════════════════════════════════════════════════════════════
        secao("7. PRONTUÁRIO");

        try {
            if (paciente == null) throw new Exception("Paciente nao disponivel");
            Prontuario pr = new Prontuario();
            pr.setPaciente(pacienteService.buscarPorId(paciente.getId()));
            pr.setGrupoSanguineo("A+");
            pr.setObservacoes("Paciente sem alergias conhecidas.");
            prontuarioService.criarProntuario(pr);
            ok("Prontuario criado");
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            if (paciente == null) throw new Exception("Paciente nao disponivel");
            Prontuario prDup = new Prontuario();
            prDup.setPaciente(pacienteService.buscarPorId(paciente.getId()));
            prDup.setGrupoSanguineo("B+");
            prontuarioService.criarProntuario(prDup);
            erro("Deveria ter rejeitado prontuario duplicado!");
        } catch (Exception e) {
            ok("Prontuario duplicado rejeitado: " + e.getMessage());
        }

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 8 - CONSULTA
        // ══════════════════════════════════════════════════════════════════════
        secao("8. CONSULTA");

        try {
            if (paciente == null || dentista == null)
                throw new Exception("Paciente ou dentista nao disponivel");
            Consulta c = new Consulta();
            c.setIdPaciente(pacienteService.buscarPorId(paciente.getId()));
            c.setIdDentista(dentistaService.buscarPorId(dentista.getId()));
            c.setDataHoraInicio(Instant.now().plusSeconds(86400));
            c.setDuracao(30);
            c.setTipo("CONSULTA_GERAL");
            c.setStatus(EstadoConsulta.AGENDADA);
            c.setDataMarcacao(LocalDate.now());
            consulta = consultaService.agendarConsulta(c);
            ok("Consulta agendada com ID: " + consulta.getId());
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            if (paciente == null || dentista == null)
                throw new Exception("Paciente ou dentista nao disponivel");
            Consulta cPass = new Consulta();
            cPass.setIdPaciente(pacienteService.buscarPorId(paciente.getId()));
            cPass.setIdDentista(dentistaService.buscarPorId(dentista.getId()));
            cPass.setDataHoraInicio(Instant.now().minusSeconds(3600));
            cPass.setStatus(EstadoConsulta.AGENDADA);
            consultaService.agendarConsulta(cPass);
            erro("Deveria ter rejeitado consulta no passado!");
        } catch (Exception e) {
            ok("Consulta no passado rejeitada: " + e.getMessage());
        }

        try {
            Consulta cSem = new Consulta();
            cSem.setDataHoraInicio(Instant.now().plusSeconds(86400));
            cSem.setStatus(EstadoConsulta.AGENDADA);
            consultaService.agendarConsulta(cSem);
            erro("Deveria ter rejeitado consulta sem paciente!");
        } catch (Exception e) {
            ok("Consulta sem paciente rejeitada: " + e.getMessage());
        }

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 9 - ATENDIMENTO
        // ══════════════════════════════════════════════════════════════════════
        secao("9. ATENDIMENTO");

        try {
            if (consulta == null) throw new Exception("Consulta nao disponivel");
            Atendimento at = new Atendimento();
            at.setIdConsulta(consultaService.buscarPorId(consulta.getId()));
            at.setDiagnostico("Carie grau 2 no dente 36.");
            at.setRetorno(true);
            at.setPeriodoRetorno(30);
            at.setObservacoes("Aplicar selante apos tratamento.");
            atendimento = atendimentoService.salvar(at);
            ok("Atendimento criado com ID: " + atendimento.getId());
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            if (consulta == null) throw new Exception("Consulta nao disponivel");
            Atendimento atInv = new Atendimento();
            atInv.setIdConsulta(consultaService.buscarPorId(consulta.getId()));
            atInv.setRetorno(true);
            atendimentoService.salvar(atInv);
            erro("Deveria ter rejeitado retorno sem periodo!");
        } catch (Exception e) {
            ok("Retorno sem periodo rejeitado: " + e.getMessage());
        }

        int idAtendimentoCriado = atendimento.getId();

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 10 - FATURA & PAGAMENTO (COM PROCEDIMENTOS)
        // ══════════════════════════════════════════════════════════════════════
        secao("10. FATURA & PAGAMENTO");

        try {
            // -------------------------------------------------------------
            // 1. Garantir que existem procedimentos de exemplo na base de dados
            // -------------------------------------------------------------
            Procedimento procConsulta = null;
            Procedimento procProtese = null;
            Procedimento procEstetica = null;

            // Tenta buscar procedimentos pelos nomes (ou cria se não existirem)
            List<Procedimento> todosProcedimentos = procedimentoRepository.findAll();
            for (Procedimento p : todosProcedimentos) {
                if ("Consulta Geral".equals(p.getNome())) procConsulta = p;
                if ("Implante Dentário".equals(p.getNome())) procProtese = p;
                if ("Branqueamento".equals(p.getNome())) procEstetica = p;
            }

            if (procConsulta == null) {
                procConsulta = new Procedimento();
                procConsulta.setNome("Consulta Geral");
                procConsulta.setValor(new BigDecimal("150.00"));
                procConsulta.setTaxaIva(BigDecimal.ZERO);   // 0%
                procConsulta.setStatus("ATIVO");
                procConsulta.setTipo("terapeutico");
                procConsulta = procedimentoRepository.save(procConsulta);
                ok("Procedimento 'Consulta Geral' criado (IVA 0%)");
            }

            if (procProtese == null) {
                procProtese = new Procedimento();
                procProtese.setNome("Implante Dentário");
                procProtese.setValor(new BigDecimal("800.00"));
                procProtese.setTaxaIva(new BigDecimal("6")); // 6%
                procProtese.setStatus("ATIVO");
                procProtese.setTipo("protese");
                procProtese = procedimentoRepository.save(procProtese);
                ok("Procedimento 'Implante Dentário' criado (IVA 6%)");
            }

            if (procEstetica == null) {
                procEstetica = new Procedimento();
                procEstetica.setNome("Branqueamento");
                procEstetica.setValor(new BigDecimal("200.00"));
                procEstetica.setTaxaIva(new BigDecimal("23")); // 23%
                procEstetica.setStatus("ATIVO");
                procEstetica.setTipo("estetico");
                procEstetica = procedimentoRepository.save(procEstetica);
                ok("Procedimento 'Branqueamento' criado (IVA 23%)");
            }

            // -------------------------------------------------------------
            // 2. Criar três atendimentos diferentes, cada um com um procedimento
            // -------------------------------------------------------------
            // Atendimento 1 – apenas consulta (IVA 0%)
            Consulta consulta1 = consultaService.buscarPorId(consulta.getId()); // reutiliza a consulta criada no bloco 8
            Atendimento at1 = new Atendimento();
            at1.setIdConsulta(consulta1);
            at1.setDiagnostico("Consulta de rotina");
            at1.setRetorno(false);
            at1 = atendimentoService.salvar(at1);
            // Associar procedimento de consulta
            AtendimentoProcedimento ap1 = criarAtendimentoProcedimento(at1, procConsulta, 1, BigDecimal.ZERO);
            atendimentoProcedimentoRepository.save(ap1);
            ok("Atendimento 1 (consulta) criado com ID " + at1.getId());

            // Atendimento 2 – apenas prótese (IVA 6%)
            Consulta consulta2 = new Consulta();
            consulta2.setIdPaciente(pacienteService.buscarPorId(paciente.getId()));
            consulta2.setIdDentista(dentistaService.buscarPorId(dentista.getId()));
            consulta2.setDataHoraInicio(Instant.now().plusSeconds(172800)); // +2 dias
            consulta2.setDuracao(45);
            consulta2.setTipo("PROTESE");
            consulta2.setStatus(EstadoConsulta.AGENDADA);
            consulta2.setDataMarcacao(LocalDate.now());
            consulta2 = consultaService.agendarConsulta(consulta2);
            Atendimento at2 = new Atendimento();
            at2.setIdConsulta(consulta2);
            at2.setDiagnostico("Colocação de implante");
            at2.setRetorno(true);
            at2.setPeriodoRetorno(30);
            at2 = atendimentoService.salvar(at2);
            AtendimentoProcedimento ap2 = criarAtendimentoProcedimento(at2, procProtese, 1, BigDecimal.ZERO);
            atendimentoProcedimentoRepository.save(ap2);
            ok("Atendimento 2 (prótese) criado com ID " + at2.getId());

            // Atendimento 3 – apenas estética (IVA 23%)
            Consulta consulta3 = new Consulta();
            consulta3.setIdPaciente(pacienteService.buscarPorId(paciente.getId()));
            consulta3.setIdDentista(dentistaService.buscarPorId(dentista.getId()));
            consulta3.setDataHoraInicio(Instant.now().plusSeconds(259200)); // +3 dias
            consulta3.setDuracao(30);
            consulta3.setTipo("ESTETICA");
            consulta3.setStatus(EstadoConsulta.AGENDADA);
            consulta3.setDataMarcacao(LocalDate.now());
            consulta3 = consultaService.agendarConsulta(consulta3);
            Atendimento at3 = new Atendimento();
            at3.setIdConsulta(consulta3);
            at3.setDiagnostico("Branqueamento");
            at3.setRetorno(false);
            at3 = atendimentoService.salvar(at3);
            AtendimentoProcedimento ap3 = criarAtendimentoProcedimento(at3, procEstetica, 1, BigDecimal.ZERO);
            atendimentoProcedimentoRepository.save(ap3);
            ok("Atendimento 3 (estética) criado com ID " + at3.getId());

            // -------------------------------------------------------------
            // 3. Emitir as faturas (uma para cada atendimento)
            // -------------------------------------------------------------
            Fatura fatura1 = faturaService.emitirFaturaPorAtendimento(at1);
            ok("Fatura 1 (consulta, IVA 0%) emitida com ID: " + fatura1.getId() + " | Valor Final: " + fatura1.getValorFinal() + "€");

            Fatura fatura2 = faturaService.emitirFaturaPorAtendimento(at2);
            ok("Fatura 2 (prótese, IVA 6%) emitida com ID: " + fatura2.getId() + " | Valor Final: " + fatura2.getValorFinal() + "€");

            Fatura fatura3 = faturaService.emitirFaturaPorAtendimento(at3);
            ok("Fatura 3 (estética, IVA 23%) emitida com ID: " + fatura3.getId() + " | Valor Final: " + fatura3.getValorFinal() + "€");

        } catch (Exception e) {
            erro(e.getMessage());
        }

        // ══════════════════════════════════════════════════════════════════════
        // BLOCO 11 - LISTAGENS FINAIS
        // ══════════════════════════════════════════════════════════════════════
        secao("11. LISTAGENS FINAIS");

        try {
            System.out.println("  Utilizadores (" + utilizadorService.listarTodos().size() + "):");
            utilizadorService.listarTodos().forEach(u ->
                    System.out.println("    * [" + u.getTipoUtilizador() + "] "
                            + u.getPrimeiroNome() + " " + (u.getUltimoNome() != null ? u.getUltimoNome() : "")
                            + " -- " + u.getEmail()));
        } catch (Exception e) {
            erro(e.getMessage());
        }

        try {
            System.out.println("  Pacientes    : " + pacienteService.listarTodos().size());
            System.out.println("  Dentistas    : " + dentistaService.listarTodos().size());
            System.out.println("  Consultas    : " + consultaService.listarTodas().size());
            System.out.println("  Atendimentos : " + atendimentoService.listarTodos().size());
            System.out.println("  Faturas      : " + faturaService.listarTodos().size());
            System.out.println("  Pagamentos   : " + pagamentoService.listarTodos().size());
        } catch (Exception e) {
            erro(e.getMessage());
        }

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║      TODOS OS TESTES CONCLUÍDOS      ║");
        System.out.println("╚══════════════════════════════════════╝\n");
    }
}

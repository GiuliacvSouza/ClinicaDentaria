package app;

import bll.*;
import dal.*;
import model.*;
import model.enums.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@ConditionalOnProperty(name = "demo.data.generate", havingValue = "true")
public class ClinicaDemoDataGenerator implements CommandLineRunner {

    private final UtilizadorService utilizadorService;
    private final PacienteService pacienteService;
    private final DentistaService dentistaService;
    private final AssistenteService assistenteService;
    private final RecepcionistaService recepcionistaService;
    private final EspecialidadeService especialidadeService;
    private final SeguroService seguroService;
    private final PacientexSeguroService pacientexSeguroService;
    private final FornecedorService fornecedorService;
    private final MaterialService materialService;
    private final ProcedimentoService procedimentoService;
    private final ConsultaService consultaService;
    private final AtendimentoService atendimentoService;
    private final AtendimentoProcedimentoService atendimentoProcedimentoService;
    private final MaterialUtilizadoService materialUtilizadoService;
    private final FaturaService faturaService;
    private final PagamentoService pagamentoService;
    private final PedidoCompraService pedidoCompraService;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;
    private final ProntuarioService prontuarioService;
    private final AnamneseService anamneseService;
    private final PrescricaoService prescricaoService;
    private final PlanoTratamentoService planoTratamentoService;
    private final AuditoriaService auditoriaService;
    private final CodigoPostalService codigoPostalService;
    private final DoencaService doencaService;
    private final AlergiaService alergiaService;
    private final MedicamentoService medicamentoService;
    private final ContatoEmergenciaService contatoEmergenciaService;
    private final AnamneseAlergiaService anamneseAlergiaService;
    private final AnamneseDoencaService anamneseDoencaService;
    private final AnamneseMedicamentoService anamneseMedicamentoService;
    private final EspecialidadeDentistaService especialidadeDentistaService;
    private final EspecialidadexAssistenteService especialidadexAssistenteService;
    private final AtendimentoProcedimentoRepository atendimentoProcedimentoRepository;

    private final Random random = new Random();
    private final ZoneId ZONE = ZoneId.systemDefault();
    private int emailCounter = 0;

    // ── Dados realistas portugueses ──
    private static final String[] FIRST_NAMES_MASC = {
        "João", "Pedro", "Tiago", "Miguel", "Ricardo", "Hugo", "Rui", "Diogo", "José", "Francisco",
        "Gonçalo", "Rafael", "Luís", "André", "Nuno", "Filipe", "Bruno", "Daniel", "Carlos", "Manuel",
        "Paulo", "Vítor", "António", "Mário", "Jorge", "Sérgio", "David", "Ruben", "Marco", "Simão"
    };
    private static final String[] FIRST_NAMES_FEM = {
        "Maria", "Ana", "Marta", "Sofia", "Carla", "Joana", "Catarina", "Beatriz", "Inês", "Mariana",
        "Matilde", "Leonor", "Carolina", "Rita", "Madalena", "Patrícia", "Tânia", "Vânia", "Sandra",
        "Lúcia", "Helena", "Isabel", "Cláudia", "Sara", "Eva", "Diana", "Lara", "Vera", "Alice", "Margarida"
    };
    private static final String[] LAST_NAMES = {
        "Silva", "Santos", "Ferreira", "Pereira", "Oliveira", "Costa", "Rodrigues", "Martins",
        "Jesus", "Sousa", "Fernandes", "Gomes", "Marques", "Almeida", "Ribeiro", "Pinto",
        "Carvalho", "Teixeira", "Moreira", "Correia", "Mendes", "Nunes", "Soares", "Dias",
        "Monteiro", "Neves", "Coelho", "Cunha", "Reis", "Araújo"
    };
    private static final String[] LOCALIDADES = {
        "Lisboa", "Porto", "Coimbra", "Braga", "Faro", "Aveiro", "Setúbal", "Évora",
        "Viseu", "Guimarães", "Leiria", "Santarém", "Vila Nova de Gaia", "Cascais", "Sintra"
    };
    private static final String[] DIAGNOSTICOS = {
        "Cárie dentária – dente 16", "Cárie dentária – dente 26", "Cárie dentária – dente 36",
        "Cárie dentária – dente 46", "Periodontite crónica generalizada", "Gengivite associada a placa bacteriana",
        "Pulpite irreversível – dente 14", "Pulpite irreversível – dente 24", "Necrose pulpar – dente 44",
        "Abscesso periapical – dente 34", "Fratura coronária – dente 11", "Fratura coronária – dente 21",
        "Desgaste dentário por bruxismo", "Erosão dentária", "Má oclusão Classe I",
        "Má oclusão Classe II", "Má oclusão Classe III", "Apinhamento dentário",
        "Diastema entre dentes anteriores", "Sinusite odontogénica", "Estomatite", "Aftose oral recorrente",
        "Lesão de cárie proximal – dente 15", "Lesão de cárie proximal – dente 25",
        "Recidiva de cárie – dente 17", "Sensibilidade dentinária generalizada",
        "Bruxismo do sono", "Síndrome da disfunção temporomandibular",
        "Halitose associada a doença periodontal", "Agenesia do dente 12"
    };
    private static final String[] MEDICAMENTOS_PRESC = {
        "Amoxicilina 500mg", "Amoxicilina + Ácido Clavulânico 875mg", "Azitromicina 500mg",
        "Clindamicina 300mg", "Metronidazol 250mg", "Ibuprofeno 600mg", "Paracetamol 500mg",
        "Diclofenac 50mg", "Naproxeno 500mg", "Nimesulida 100mg", "Cetoprofeno 100mg",
        "Benzocaína spray 10%", "Lidocaína gel 2%", "Clorexidina colutório 0,12%",
        "Fluoreto de Sódio gel 2%", "Fluoreto de Sódio verniz", "Ácido Mefenâmico 500mg",
        "Dexametasona 4mg", "Prednisolona 20mg", "Omeprazol 20mg"
    };
    private static final String[] POSOLOGIAS = {
        "Tomar 1 comprimido de 8/8h durante 7 dias",
        "Tomar 1 comprimido de 12/12h durante 5 dias",
        "Tomar 1 comprimido ao dia durante 3 dias",
        "Aplicar topicamente 3x/dia durante 7 dias",
        "Tomar 2 comprimidos de 8/8h durante 5 dias",
        "Tomar 1 comprimido de 6/6h se dor, máximo 3 dias",
        "Bochechar 10ml 2x/dia durante 14 dias",
        "Aplicar gel 2x/dia após escovagem",
        "Tomar 1 comprimido ao dia durante 10 dias",
        "Tomar 1 comprimido de 12/12h durante 8 dias",
        "Aplicar spray 3x/dia em caso de dor",
        "Tomar 1 comprimido de 24/24h durante 6 dias",
        "Bochechar 15ml antes de dormir durante 30 dias",
        "Aplicar verniz nos dentes afetados, 1x/semana durante 4 semanas",
        "Tomar 1 comprimido de 8/8h durante 10 dias + colutório 15ml 2x/dia"
    };
    private static final String[] OBJETIVOS_TRATAMENTO = {
        "Restauração da função mastigatória do quadrante inferior direito",
        "Correção da má oclusão com aparelho ortodôntico fixo",
        "Recuperação do dente 16 com tratamento endodôntico e coroa",
        "Tratamento periodontal com raspagem e alisamento radicular",
        "Substituição de restaurações insatisfatórias nos dentes posteriores",
        "Implante dentário para substituição do dente 36",
        "Clareamento dentário interno e externo",
        "Reabilitação oral completa com prótese fixa",
        "Tratamento de lesões de cárie múltiplas em dentes decíduos",
        "Cirurgia de inclusão do siso 48",
        "Aplicação de selantes de fissuras nos molares permanentes",
        "Tratamento conservador das lesões cervicais não cariosas",
        "Reconstrução dos dentes anteriores fraturados",
        "Controlo da doença periodontal com manutenção regular",
        "Tratamento ortodôntico com alinhadores removíveis",
        "Prótese parcial removível para reabilitação do maxilar superior",
        "Tratamento de sensibilidade com aplicação de dessensibilizante",
        "Correção estética com facetas de resina composta",
        "Avaliação e tratamento da disfunção temporomandibular",
        "Manutenção e controlo periodontais com follow-up trimestral"
    };
    private static final String[] OPERACOES_AUDITORIA = {
        "LOGIN", "LOGOUT", "CRIACAO_CONSULTA", "CANCELAMENTO_CONSULTA",
        "EMISSAO_PRESCRICAO", "ALTERACAO_PLANO_TRATAMENTO",
        "CRIACAO_PEDIDO_COMPRA", "MOVIMENTACAO_ESTOQUE",
        "EMISSAO_FATURA", "REGISTO_PAGAMENTO", "CADASTRO_PACIENTE",
        "ATUALIZACAO_PRONTUARIO", "REALIZACAO_ATENDIMENTO",
        "CRIACAO_PLANO_TRATAMENTO", "ALTERACAO_ESTADO_CONSULTA"
    };

    @Autowired
    public ClinicaDemoDataGenerator(
            UtilizadorService utilizadorService, PacienteService pacienteService,
            DentistaService dentistaService, AssistenteService assistenteService,
            RecepcionistaService recepcionistaService, EspecialidadeService especialidadeService,
            SeguroService seguroService, PacientexSeguroService pacientexSeguroService,
            FornecedorService fornecedorService, MaterialService materialService,
            ProcedimentoService procedimentoService, ConsultaService consultaService,
            AtendimentoService atendimentoService,
            AtendimentoProcedimentoService atendimentoProcedimentoService,
            MaterialUtilizadoService materialUtilizadoService, FaturaService faturaService,
            PagamentoService pagamentoService, PedidoCompraService pedidoCompraService,
            MovimentacaoEstoqueService movimentacaoEstoqueService, ProntuarioService prontuarioService,
            AnamneseService anamneseService, PrescricaoService prescricaoService,
            PlanoTratamentoService planoTratamentoService, AuditoriaService auditoriaService,
            CodigoPostalService codigoPostalService, DoencaService doencaService,
            AlergiaService alergiaService, MedicamentoService medicamentoService,
            ContatoEmergenciaService contatoEmergenciaService,
            AnamneseAlergiaService anamneseAlergiaService,
            AnamneseDoencaService anamneseDoencaService,
            AnamneseMedicamentoService anamneseMedicamentoService,
            EspecialidadeDentistaService especialidadeDentistaService,
            EspecialidadexAssistenteService especialidadexAssistenteService,
            AtendimentoProcedimentoRepository atendimentoProcedimentoRepository) {
        this.utilizadorService = utilizadorService;
        this.pacienteService = pacienteService;
        this.dentistaService = dentistaService;
        this.assistenteService = assistenteService;
        this.recepcionistaService = recepcionistaService;
        this.especialidadeService = especialidadeService;
        this.seguroService = seguroService;
        this.pacientexSeguroService = pacientexSeguroService;
        this.fornecedorService = fornecedorService;
        this.materialService = materialService;
        this.procedimentoService = procedimentoService;
        this.consultaService = consultaService;
        this.atendimentoService = atendimentoService;
        this.atendimentoProcedimentoService = atendimentoProcedimentoService;
        this.materialUtilizadoService = materialUtilizadoService;
        this.faturaService = faturaService;
        this.pagamentoService = pagamentoService;
        this.pedidoCompraService = pedidoCompraService;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
        this.prontuarioService = prontuarioService;
        this.anamneseService = anamneseService;
        this.prescricaoService = prescricaoService;
        this.planoTratamentoService = planoTratamentoService;
        this.auditoriaService = auditoriaService;
        this.codigoPostalService = codigoPostalService;
        this.doencaService = doencaService;
        this.alergiaService = alergiaService;
        this.medicamentoService = medicamentoService;
        this.contatoEmergenciaService = contatoEmergenciaService;
        this.anamneseAlergiaService = anamneseAlergiaService;
        this.anamneseDoencaService = anamneseDoencaService;
        this.anamneseMedicamentoService = anamneseMedicamentoService;
        this.especialidadeDentistaService = especialidadeDentistaService;
        this.especialidadexAssistenteService = especialidadexAssistenteService;
        this.atendimentoProcedimentoRepository = atendimentoProcedimentoRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("======================================================");
        System.out.println("  CLÍNICA DENTÁRIA – GERADOR DE DADOS DE DEMONSTRAÇÃO");
        System.out.println("======================================================");

        long totalUtilizadores = utilizadorService.listarTodos().size();
        if (totalUtilizadores > 80) {
            System.out.println("A base já contém " + totalUtilizadores + " utilizadores. Gerador abortado.");
            return;
        }

        List<CodigoPostal> codigosPostais = new ArrayList<>();
        List<Especialidade> especialidades = new ArrayList<>();
        List<Doenca> doencas = new ArrayList<>();
        List<Alergia> alergias = new ArrayList<>();
        List<Medicamento> medicamentos = new ArrayList<>();
        List<Utilizador> admins = new ArrayList<>();
        List<Dentista> dentistas = new ArrayList<>();
        List<Assistente> assistentes = new ArrayList<>();
        List<Recepcionista> recepcionistas = new ArrayList<>();
        List<Paciente> pacientes = new ArrayList<>();
        List<Seguro> seguros = new ArrayList<>();
        List<Fornecedor> fornecedores = new ArrayList<>();
        List<Procedimento> procedimentos = new ArrayList<>();
        List<Material> materiais = new ArrayList<>();
        List<Consulta> consultas = new ArrayList<>();
        List<Atendimento> atendimentos = new ArrayList<>();

        // ═══ FASE 1 – DADOS BASE ═══
        System.out.println("\n── FASE 1: Dados Base ──");
        for (int i = 0; i < 15; i++) {
            try {
                CodigoPostal cp = new CodigoPostal();
                cp.setCodigoPostal(String.format("%04d-%03d", 1000 + i * 50, 100 + random.nextInt(899)));
                cp.setLocalidade(LOCALIDADES[i % LOCALIDADES.length]);
                codigoPostalService.salvar(cp);
                codigosPostais.add(cp);
            } catch (Exception ignored) {}
        }
        String[] nomesEsp = {"Odontopediatria","Ortodontia","Endodontia","Periodontia","Implantologia","Cirurgia Oral","Prostodontia"};
        for (int i = 0; i < nomesEsp.length; i++) {
            try {
                Especialidade e = new Especialidade();
                e.setNome(nomesEsp[i]);
                especialidadeService.salvar(e);
                especialidades.add(e);
            } catch (Exception ignored) {}
        }
        String[][] doencasData = {
            {"Hipertensão Arterial","Cardiovascular"},{"Diabetes Tipo 1","Endócrino"},
            {"Diabetes Tipo 2","Endócrino"},{"Asma Brônquica","Respiratório"},
            {"Hepatite B","Infeciosa"},{"Epilepsia","Neurológico"},
            {"Doença de Crohn","Gastrointestinal"},{"Artrite Reumatoide","Autoimune"},
            {"Osteoporose","Ósseo"},{"Anemia Ferropénica","Hematológico"}
        };
        for (String[] d : doencasData) {
            try {
                Doenca doenca = new Doenca();
                doenca.setNome(d[0]);
                doenca.setCategoria(d[1]);
                doenca.setAtiva(true);
                doencaService.salvar(doenca);
                doencas.add(doenca);
            } catch (Exception ignored) {}
        }
        String[][] alergiasData = {
            {"Penicilina","MEDICAMENTO"},{"Aspirina","MEDICAMENTO"},{"Ibuprofeno","MEDICAMENTO"},
            {"Látex","MATERIAL"},{"Iodo","QUIMICA"},{"Sulfa","MEDICAMENTO"},
            {"Anestésico Local","MEDICAMENTO"},{"Amoxicilina","MEDICAMENTO"},
            {"Pólen","AMBIENTAL"},{"Marisco","ALIMENTAR"},{"Paracetamol","MEDICAMENTO"},
            {"Níquel","MATERIAL"}
        };
        for (String[] a : alergiasData) {
            try {
                Alergia alergia = new Alergia();
                alergia.setSubstancia(a[0]);
                alergia.setTipo(TipoAlergia.valueOf(a[1]));
                alergiaService.criar(alergia);
                alergias.add(alergia);
            } catch (Exception ignored) {}
        }
        String[] medNomes = {"Amoxicilina","Ibuprofeno","Paracetamol","Azitromicina","Clindamicina",
                             "Diclofenac","Nimesulida","Metronidazol","Cetoprofeno","Dexametasona","Omeprazol","Fluoxetina"};
        for (String mn : medNomes) {
            try {
                Medicamento m = new Medicamento();
                m.setNome(mn);
                m.setFabricante("LabMed");
                medicamentoService.salvar(m);
                medicamentos.add(m);
            } catch (Exception ignored) {}
        }
        System.out.println("  Cod.Postais: " + codigosPostais.size() + " | Especialidades: " + especialidades.size()
                + " | Doenças: " + doencas.size() + " | Alergias: " + alergias.size() + " | Medicamentos: " + medicamentos.size());

        // ═══ FASE 2 – UTILIZADORES ═══
        System.out.println("\n── FASE 2: Utilizadores ──");

        // Admins (5)
        for (int i = 1; i <= 5; i++) {
            try {
                Utilizador u = criarUtilizadorBase("ADMINISTRADOR");
                u.setEmail(i == 1 ? "admin@clinica.pt" : "admin" + i + "@clinica.pt");
                u.setSenha("Admin@123");
                admins.add(utilizadorService.salvar(u));
            } catch (Exception ignored) {}
        }
        System.out.println("  Administradores: " + admins.size());

        // Dentistas (10+)
        String[][] dentistaData = {
            {"Mariana","Pereira","927451320","OMD-21001","08:30","17:30"},
            {"Tiago","Almeida","927451321","OMD-21002","09:00","18:00"},
            {"Inês","Carvalho","927451322","OMD-21003","10:00","19:00"},
            {"Rita","Martins","927451323","OMD-21004","08:00","17:00"},
            {"Pedro","Santos","927451324","OMD-21005","09:30","18:30"},
            {"Sofia","Lopes","927451325","OMD-21006","08:30","17:30"},
            {"Miguel","Ferreira","927451326","OMD-21007","09:00","18:00"},
            {"Ana","Costa","927451327","OMD-21008","10:00","19:00"},
            {"João","Rodrigues","927451328","OMD-21009","08:00","17:00"},
            {"Catarina","Silva","927451329","OMD-21010","09:30","18:30"}
        };
        for (String[] dd : dentistaData) {
            try {
                Utilizador u = new Utilizador();
                u.setPrimeiroNome(dd[0]);
                u.setUltimoNome(dd[1]);
                u.setEmail(("dentista." + dd[2] + "@clinica.pt").toLowerCase());
                u.setTipoUtilizador("DENTISTA");
                u.setNif(dd[2]);
                u.setTelemovel(dd[2]);
                u.setTelefone(dd[2]);
                u.setSenha("Clinica2026!");
                u.setStatus("ATIVO");
                u.setDataNascimento(LocalDate.of(1975 + random.nextInt(25), 1 + random.nextInt(12), 1 + random.nextInt(28)));
                u = utilizadorService.salvar(u);

                Dentista d = new Dentista();
                d.setUtilizador(u);
                d.setNumeroOmd(dd[3]);
                d.setDataAdmissao(LocalDate.now().minusMonths(6 + random.nextInt(36)));
                d.setHorarioEntrada(LocalTime.parse(dd[4]));
                d.setHorarioSaida(LocalTime.parse(dd[5]));
                d.setAtivo(true);
                dentistaService.salvar(d);
                dentistas.add(d);

                // Associar especialidade
                if (!especialidades.isEmpty()) {
                    Especialidade esp = especialidades.get(random.nextInt(especialidades.size()));
                    try {
                        EspecialidadeDentistaId edId = new EspecialidadeDentistaId();
                        edId.setIdUtilizador(d.getId());
                        edId.setIdEspecialidade(esp.getId());
                        EspecialidadeDentista ed = new EspecialidadeDentista();
                        ed.setId(edId);
                        ed.setIdUtilizador(d);
                        ed.setIdEspecialidade(esp);
                        especialidadeDentistaService.salvar(ed);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }
        System.out.println("  Dentistas: " + dentistas.size());

        // Assistentes (10+)
        for (int i = 1; i <= 10; i++) {
            try {
                Utilizador u = criarUtilizadorBase("ASSISTENTE");
                u.setEmail("assistente" + i + "@clinica.pt");
                u = utilizadorService.salvar(u);
                Assistente a = new Assistente();
                a.setUtilizador(u);
                a.setNivelFormacao(NivelFormacao.values()[random.nextInt(NivelFormacao.values().length)]);
                a.setDataAdmissao(LocalDate.now().minusMonths(1 + random.nextInt(36)));
                a.setAtivo(true);
                assistenteService.salvar(a);
                assistentes.add(a);

                // Associar especialidade
                if (!especialidades.isEmpty()) {
                    Especialidade esp = especialidades.get(random.nextInt(especialidades.size()));
                    try {
                        EspecialidadexAssistenteId eaId = new EspecialidadexAssistenteId();
                        eaId.setIdUtilizador(a.getId());
                        eaId.setIdEspecialidade(esp.getId());
                        EspecialidadexAssistente ea = new EspecialidadexAssistente();
                        ea.setId(eaId);
                        ea.setIdUtilizador(a);
                        ea.setIdEspecialidade(esp);
                        especialidadexAssistenteService.salvar(ea);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }
        System.out.println("  Assistentes: " + assistentes.size());

        // Rececionistas (10+)
        for (int i = 1; i <= 10; i++) {
            try {
                Utilizador u = criarUtilizadorBase("RECEPCIONISTA");
                u.setEmail("rececionista" + i + "@clinica.pt");
                u = utilizadorService.salvar(u);
                Recepcionista r = new Recepcionista();
                r.setUtilizador(u);
                r.setDataAdmissao(LocalDate.now().minusMonths(1 + random.nextInt(24)));
                r.setTurno(Turno.values()[random.nextInt(Turno.values().length)]);
                recepcionistaService.salvar(r);
                recepcionistas.add(r);
            } catch (Exception ignored) {}
        }
        System.out.println("  Rececionistas: " + recepcionistas.size());

        // Pacientes (25)
        for (int i = 1; i <= 25; i++) {
            try {
                Utilizador u = criarUtilizadorBase("PACIENTE");
                u.setEmail("paciente." + System.nanoTime() + "@exemplo.pt");
                u = utilizadorService.salvar(u);
                Paciente p = new Paciente();
                p.setUtilizador(u);
                p.setStatus("ATIVO");
                p.setDataRegisto(LocalDate.now().minusDays(random.nextInt(730)));
                pacienteService.salvar(p);
                pacientes.add(p);
            } catch (Exception ignored) {}
        }
        System.out.println("  Pacientes: " + pacientes.size());

        // ═══ FASE 3 – SEGUROS, FORNECEDORES, ASSOCIAÇÕES ═══
        System.out.println("\n── FASE 3: Seguros, Fornecedores e Associações ──");

        String[] segurosNomes = {"Médis","Multicare","Allianz","Fidelidade","AdvanceCare","SAMS","ADSE","Tranquilidade","Generali","Ageas","Real Vida","Lusitânia"};
        for (String sn : segurosNomes) {
            try {
                Seguro s = new Seguro();
                s.setNomeSeguro(sn + " Dental");
                s.setTipoPlano("Plano " + (random.nextBoolean() ? "Premium" : "Base"));
                s.setCodigoPlano(sn.substring(0, 3).toUpperCase() + "-" + String.format("%04d", random.nextInt(9999)));
                s.setContactoSeguradora("contacto@" + sn.toLowerCase() + ".pt");
                s.setValidoAte(LocalDate.now().plusMonths(6 + random.nextInt(30)));
                seguroService.salvar(s);
                seguros.add(s);
            } catch (Exception ignored) {}
        }
        System.out.println("  Seguros: " + seguros.size());

        String[] fornNomes = {"DentalMed","OralTech","DentiMax","EuroDental","MediDent","SorrisoPerfeito","DentalPro","OralCare","DentiLus","SaúdeDental"};
        for (String fn : fornNomes) {
            try {
                Fornecedor f = new Fornecedor();
                f.setNome(fn);
                f.setUltimoNome("Fornecedor");
                f.setEmail("contato@" + fn.toLowerCase() + ".pt");
                f.setTelefone("21" + (1000000 + random.nextInt(9000000)));
                fornecedorService.salvar(f);
                fornecedores.add(f);
            } catch (Exception ignored) {}
        }
        System.out.println("  Fornecedores: " + fornecedores.size());

        int assocSeguros = 0;
        for (Paciente p : pacientes) {
            if (random.nextDouble() < 0.6 && !seguros.isEmpty()) {
                Seguro s = seguros.get(random.nextInt(seguros.size()));
                try {
                    PacientexSeguroId psId = new PacientexSeguroId();
                    psId.setIdUtilizador(p.getId());
                    psId.setIdSeguro(s.getId());
                    PacientexSeguro ps = new PacientexSeguro();
                    ps.setId(psId);
                    ps.setIdUtilizador(p);
                    ps.setIdSeguro(s);
                    ps.setNumeroApolice("AP-" + String.format("%06d", random.nextInt(999999)));
                    ps.setDataInicioCobertura(LocalDate.now().minusMonths(1 + random.nextInt(24)));
                    ps.setDataFimCobertura(ps.getDataInicioCobertura().plusYears(1));
                    pacientexSeguroService.salvar(ps);
                    assocSeguros++;
                } catch (Exception ignored) {}
            }
        }
        System.out.println("  Associações Paciente-Seguro: " + assocSeguros);

        int contatos = 0;
        for (Paciente p : pacientes) {
            if (random.nextDouble() < 0.7) {
                try {
                    ContatoEmergencia ce = new ContatoEmergencia();
                    ce.setPaciente(p);
                    ce.setPrimeiroNome(FIRST_NAMES_FEM[random.nextInt(FIRST_NAMES_FEM.length)]);
                    ce.setUltimoNome(LAST_NAMES[random.nextInt(LAST_NAMES.length)]);
                    contatoEmergenciaService.salvar(ce);
                    contatos++;
                } catch (Exception ignored) {}
            }
        }
        System.out.println("  Contatos de Emergência: " + contatos);

        // ═══ FASE 4 – PROCEDIMENTOS, MATERIAIS ═══
        System.out.println("\n── FASE 4: Procedimentos e Materiais ──");

        String[][] procData = {
            {"Consulta Geral","terapeutico","50.00","6","30"},{"Limpeza Oral","terapeutico","70.00","6","45"},
            {"Restauração em Resina","terapeutico","85.00","23","40"},{"Tratamento de Canal","terapeutico","250.00","23","90"},
            {"Extração Simples","terapeutico","80.00","23","30"},{"Extração de Siso","terapeutico","200.00","23","60"},
            {"Branqueamento","estetico","300.00","23","90"},{"Aplicação de Selante","terapeutico","35.00","6","20"},
            {"Implante Dentário","protese","1200.00","23","120"},{"Coroa Dentária","protese","600.00","23","90"},
            {"Faceta de Resina","estetico","250.00","23","60"},{"Aparelho Ortodôntico","protese","1500.00","23","0"},
            {"Destartarização","terapeutico","60.00","6","45"},{"Fluoretação","terapeutico","25.00","6","15"},
            {"Raio-X Panorâmico","terapeutico","40.00","6","15"}
        };
        for (String[] pd : procData) {
            try {
                Procedimento p = new Procedimento();
                p.setNome(pd[0]);
                p.setDescricao(pd[0] + " – procedimento odontológico");
                p.setTipo(pd[1]);
                p.setValor(new BigDecimal(pd[2]));
                p.setTaxaIva(new BigDecimal(pd[3]));
                p.setDuracaoEstimada(Integer.parseInt(pd[4]));
                p.setStatus("ATIVO");
                procedimentoService.salvar(p);
                procedimentos.add(p);
            } catch (Exception ignored) {}
        }
        System.out.println("  Procedimentos: " + procedimentos.size());

        String[][] matData = {
            {"LUV-NIT-M","Luvas de nitrilo sem pó M","un","12.50","50","20"},{"LUV-NIT-G","Luvas de nitrilo sem pó G","un","13.00","50","20"},
            {"MAS-CIR","Máscaras cirúrgicas tripla","un","8.00","200","100"},{"RES-A2","Resina composta A2 4g","un","35.00","15","5"},
            {"RES-A3","Resina composta A3 4g","un","35.00","15","5"},{"BRO-1012","Broca diamantada FG 1012","un","12.00","30","10"},
            {"BRO-3017","Broca diamantada FG 3017","un","14.00","25","10"},{"ANE-ART","Anestésico Articaina 4%","cx","18.00","40","15"},
            {"ANE-LID","Anestésico Lidocaína 2%","cx","15.00","35","15"},{"SEL-OP","Selante fissuras opaco","un","22.00","20","8"},
            {"FIO-SUT3","Fio sutura seda 3-0","un","8.50","50","20"},{"ALG-ROL","Algodão em rolo 500g","un","6.00","30","10"},
            {"GAZ-EST","Gaze esterilizada 10x10","pc","4.50","100","40"},{"BAB-DES","Babetes descartáveis","pc","12.00","150","60"},
            {"CIM-ION","Cimento ionómero vidro","un","45.00","10","4"},{"MAT-ACO","Matriz aço restaurações","un","9.00","40","15"},
            {"ISO-BOR","Isolamento borracha 5x5","pc","18.00","25","10"},{"GRA-ISO","Grampos isolamento vários","un","7.00","30","12"},
            {"SER-ANE","Seringa anestesia descartável","un","3.50","200","80"},{"LAM-BIS","Lâmina bisturi n.º15","cx","6.00","100","40"},
            {"CIM-PROV","Cimento provisório","un","28.00","15","5"},{"FIO-RET","Fio retrator vários","un","11.00","20","8"},
            {"DIS-SUG","Sugador cirúrgico descartável","un","4.00","100","50"},{"GOR-MAT","Gorra touca descartável","pc","5.00","200","80"},
            {"BRO-TUNG","Broca tungsténio cirurgia","un","22.00","15","5"}
        };
        for (String[] md : matData) {
            try {
                Material m = new Material();
                m.setCodigoInterno(md[0]);
                m.setNome(md[1]);
                m.setUnidadeMedida(md[2]);
                m.setValorUnitario(new BigDecimal(md[3]));
                m.setQuantidadeAtual(Integer.parseInt(md[4]));
                m.setQuantidadeMinima(Integer.parseInt(md[5]));
                m.setDescricao(md[1] + " – material odontológico");
                m.setAtivo(true);
                if (!fornecedores.isEmpty()) {
                    m.setIdFornecedor(fornecedores.get(random.nextInt(fornecedores.size())));
                }
                materialService.salvar(m);
                materiais.add(m);
            } catch (Exception ignored) {}
        }
        System.out.println("  Materiais: " + materiais.size());

        // ═══ FASE 5 – PRONTUÁRIOS ═══
        System.out.println("\n── FASE 5: Prontuários ──");
        String[] grupos = {"A+","A-","B+","B-","AB+","AB-","O+","O-"};
        int prontCriados = 0;
        for (Paciente p : pacientes) {
            try {
                Prontuario existente = prontuarioService.buscarPorPaciente(p.getId());
                if (existente == null) {
                    Prontuario pr = new Prontuario();
                    pr.setPaciente(p);
                    pr.setGrupoSanguineo(grupos[random.nextInt(grupos.length)]);
                    pr.setHistoricoMedico(random.nextBoolean() ? "Paciente com " + doencas.get(random.nextInt(doencas.size())).getNome() + ". Controlo regular." : "Sem doenças sistémicas relevantes.");
                    pr.setAlergias(random.nextDouble() < 0.4 ? alergias.get(random.nextInt(alergias.size())).getSubstancia() + " – reação alérgica conhecida." : "Sem alergias conhecidas.");
                    pr.setMedicamentosUso(random.nextDouble() < 0.5 ? medicamentos.get(random.nextInt(medicamentos.size())).getNome() + " – uso contínuo." : "Não toma medicação regular.");
                    pr.setObservacoesClinicas("Higiene oral " + (random.nextBoolean() ? "boa" : "regular") + ". " + (random.nextBoolean() ? "Necessita de motivação para higiene." : ""));
                    pr.setHistoricoOdontologico("Já realizou " + (random.nextBoolean() ? "tratamento de canal" : "restaurações") + " anteriormente.");
                    pr.setObservacoes("Registo criado em " + LocalDate.now().minusDays(random.nextInt(730)));
                    prontuarioService.criarProntuario(pr);
                    prontCriados++;
                }
            } catch (Exception ignored) {}
        }
        System.out.println("  Prontuários: " + prontCriados);

        // ═══ FASE 6 – CONSULTAS ═══
        System.out.println("\n── FASE 6: Consultas ──");
        EstadoConsulta[] estadosConsulta = {
            EstadoConsulta.AGENDADA, EstadoConsulta.CONFIRMADA, EstadoConsulta.EM_ESPERA,
            EstadoConsulta.EM_CONSULTA, EstadoConsulta.CONCLUIDA, EstadoConsulta.CANCELADA, EstadoConsulta.FATURADA
        };
        int consultasCriadas = 0;
        for (int i = 0; i < 90; i++) {
            try {
                if (pacientes.isEmpty() || dentistas.isEmpty() || procedimentos.isEmpty()) break;
                Paciente p = pacientes.get(random.nextInt(pacientes.size()));
                Dentista d = dentistas.get(random.nextInt(dentistas.size()));
                Procedimento proc = procedimentos.get(random.nextInt(procedimentos.size()));

                EstadoConsulta estado = estadosConsulta[random.nextInt(estadosConsulta.length)];
                Instant dataInicio;
                if (estado == EstadoConsulta.AGENDADA || estado == EstadoConsulta.CONFIRMADA) {
                    dataInicio = Instant.now().plus(1 + random.nextInt(30), ChronoUnit.DAYS)
                            .plus(8 + random.nextInt(9), ChronoUnit.HOURS);
                } else {
                    dataInicio = Instant.now().minus(1 + random.nextInt(180), ChronoUnit.DAYS)
                            .plus(8 + random.nextInt(9), ChronoUnit.HOURS);
                }

                Consulta c = new Consulta();
                c.setIdPaciente(p);
                c.setIdDentista(d);
                c.setDataHoraInicio(dataInicio);
                c.setDuracao(30 + random.nextInt(60));
                c.setTipo(proc.getTipo() != null ? proc.getTipo() : "terapeutico");
                c.setStatus(EstadoConsulta.AGENDADA);
                c.setObservacoes("Procedimento: " + proc.getNome());
                c.setDataMarcacao(LocalDate.ofInstant(dataInicio.minus(1 + random.nextInt(15), ChronoUnit.DAYS), ZONE));

                Consulta consultaSalva = consultaService.agendarConsulta(c);

                if (estado == EstadoConsulta.CANCELADA) {
                    consultaService.cancelar(consultaSalva.getId());
                } else if (estado == EstadoConsulta.CONFIRMADA) {
                    consultaService.confirmarConsulta(consultaSalva.getId());
                } else if (estado == EstadoConsulta.EM_ESPERA) {
                    consultaService.confirmarConsulta(consultaSalva.getId());
                    consultaService.marcarChegada(consultaSalva.getId());
                } else if (estado == EstadoConsulta.CONCLUIDA || estado == EstadoConsulta.FATURADA) {
                    if (dataInicio.isBefore(Instant.now())) {
                        consultaService.confirmarConsulta(consultaSalva.getId());
                        consultaService.marcarChegada(consultaSalva.getId());
                        consultaService.iniciarConsulta(consultaSalva.getId());
                        consultaService.finalizarConsulta(consultaSalva.getId());
                        if (estado == EstadoConsulta.FATURADA) {
                            consultaService.faturarConsulta(consultaSalva.getId());
                        }
                    }
                }
                consultasCriadas++;
                consultas.add(consultaService.buscarPorId(consultaSalva.getId()));
            } catch (Exception ignored) {}
        }
        System.out.println("  Consultas: " + consultasCriadas);

        // ── Consultas específicas para hoje ──
        int consultasHojeCriadas = 0;
        LocalDate hoje = LocalDate.now();

        // Gerar horários futuros a partir de agora + 1h arredondado à meia hora
        LocalTime agora = LocalTime.now();
        LocalTime inicio = agora.plusHours(1);
        if (inicio.getMinute() < 30) {
            inicio = inicio.withMinute(30);
        } else {
            inicio = inicio.plusHours(1).withMinute(0);
        }
        inicio = inicio.truncatedTo(ChronoUnit.MINUTES);

        List<LocalTime> horariosDisponiveis = new ArrayList<>();
        LocalTime horario = inicio;
        LocalTime fimExpediente = LocalTime.of(18, 30);
        while (!horario.isAfter(fimExpediente) && horariosDisponiveis.size() < 14) {
            horariosDisponiveis.add(horario);
            horario = horario.plusMinutes(30);
        }

        EstadoConsulta[] estadosHoje = {
            EstadoConsulta.AGENDADA,
            EstadoConsulta.CONFIRMADA,
            EstadoConsulta.EM_ESPERA,
            EstadoConsulta.EM_CONSULTA,
            EstadoConsulta.CONCLUIDA
        };

        for (int i = 0; i < horariosDisponiveis.size(); i++) {
            try {
                if (pacientes.isEmpty() || dentistas.isEmpty() || procedimentos.isEmpty()) break;

                LocalTime hora = horariosDisponiveis.get(i);
                LocalDateTime dataHoraLocal = LocalDateTime.of(hoje, hora);
                Instant dataHoraInicio = dataHoraLocal.atZone(ZONE).toInstant();

                Paciente p = pacientes.get(random.nextInt(pacientes.size()));
                Dentista d = dentistas.get(random.nextInt(dentistas.size()));
                Procedimento proc = procedimentos.get(random.nextInt(procedimentos.size()));

                int duracao = proc.getDuracaoEstimada() != null ? proc.getDuracaoEstimada() : 30;

                Consulta c = new Consulta();
                c.setIdPaciente(p);
                c.setIdDentista(d);
                c.setDataHoraInicio(dataHoraInicio);
                c.setDuracao(duracao);
                c.setTipo(proc.getTipo() != null ? proc.getTipo() : "terapeutico");
                c.setStatus(EstadoConsulta.AGENDADA);
                c.setObservacoes("Procedimento: " + proc.getNome());
                c.setDataMarcacao(hoje);

                Consulta consultaSalva = consultaService.agendarConsulta(c);

                EstadoConsulta estado = estadosHoje[i % estadosHoje.length];

                if (estado == EstadoConsulta.CONFIRMADA) {
                    consultaService.confirmarConsulta(consultaSalva.getId());
                } else if (estado == EstadoConsulta.EM_ESPERA) {
                    consultaService.confirmarConsulta(consultaSalva.getId());
                    consultaService.marcarChegada(consultaSalva.getId());
                } else if (estado == EstadoConsulta.EM_CONSULTA) {
                    consultaService.confirmarConsulta(consultaSalva.getId());
                    consultaService.marcarChegada(consultaSalva.getId());
                    consultaService.iniciarConsulta(consultaSalva.getId());
                } else if (estado == EstadoConsulta.CONCLUIDA) {
                    consultaService.confirmarConsulta(consultaSalva.getId());
                    consultaService.marcarChegada(consultaSalva.getId());
                    consultaService.iniciarConsulta(consultaSalva.getId());
                    consultaService.finalizarConsulta(consultaSalva.getId());
                }

                consultasHojeCriadas++;
                consultas.add(consultaService.buscarPorId(consultaSalva.getId()));
            } catch (Exception ignored) {}
        }
        System.out.println("  Consultas criadas para hoje: " + consultasHojeCriadas);

        // ═══ FASE 7 – ATENDIMENTOS ═══
        System.out.println("\n── FASE 7: Atendimentos ──");
        int atendimentosCriados = 0;
        int anamnesesCriadas = 0;
        for (Consulta c : consultas) {
            if (c.getStatus() == EstadoConsulta.CONCLUIDA || c.getStatus() == EstadoConsulta.FATURADA || c.getStatus() == EstadoConsulta.EM_CONSULTA) {
                try {
                    Atendimento at = atendimentoService.obterOuCriarPorConsulta(c);
                    at.setDiagnostico(DIAGNOSTICOS[random.nextInt(DIAGNOSTICOS.length)]);
                    at.setDataAtendimento(LocalDate.ofInstant(c.getDataHoraInicio(), ZONE));
                    at.setRetorno(random.nextDouble() < 0.3);
                    if (Boolean.TRUE.equals(at.getRetorno())) {
                        at.setPeriodoRetorno(3 + random.nextInt(9));
                    }
                    at.setProcedimentosRealizados("Procedimento conforme planeado.");
                    at.setObservacoes("Paciente " + (random.nextBoolean() ? "tolerou bem o procedimento." : "apresentou alguma sensibilidade."));

                    Atendimento atSalvo = atendimentoService.salvar(at);

                    // Associar procedimento ao atendimento
                    if (!procedimentos.isEmpty()) {
                        Procedimento proc = procedimentos.get(random.nextInt(procedimentos.size()));
                        try {
                            AtendimentoProcedimento ap = new AtendimentoProcedimento();
                            ap.setIdAtendimento(atSalvo);
                            ap.setIdProcedimento(proc);
                            ap.setQuantidade(1);
                            ap.setDesconto(random.nextDouble() < 0.2 ? BigDecimal.valueOf(random.nextInt(15)) : BigDecimal.ZERO);
                            atendimentoProcedimentoService.salvar(ap);
                        } catch (Exception ignored) {}
                    }

                    // Associar materiais utilizados
                    if (!materiais.isEmpty() && random.nextDouble() < 0.5) {
                        Material mat = materiais.get(random.nextInt(materiais.size()));
                        try {
                            materialUtilizadoService.registar(atSalvo.getId(), mat.getId(), 1 + random.nextInt(3));
                        } catch (Exception ignored) {}
                    }

                    // Criar anamnese
                    if (random.nextDouble() < 0.6) {
                        try {
                            Anamnese a = new Anamnese();
                            a.setIdAtendimento(atSalvo);
                            a.setData(LocalDate.ofInstant(c.getDataHoraInicio(), ZONE));
                            a.setMotivo(random.nextBoolean() ? "Rotina" : "Queixa específica");
                            a.setQueixaPrincipal(random.nextBoolean() ? "Dor no dente " + (10 + random.nextInt(40)) : "Revisão periódica");
                            a.setDiabetes(random.nextDouble() < 0.1);
                            a.setHipertensao(random.nextDouble() < 0.15);
                            a.setDoencagrave(random.nextDouble() < 0.05);
                            a.setHepatite(random.nextDouble() < 0.02);
                            a.setOutrasDoencas(random.nextBoolean() ? "Nenhuma" : "Asma ligeira");
                            a.setUsaMedicamento(random.nextDouble() < 0.4);
                            a.setTemAlergia(random.nextDouble() < 0.3);
                            a.seteFumante(random.nextDouble() < 0.2);
                            a.setHabitosRelevantes(random.nextBoolean() ? "Café 3x/dia" : "Higiene oral diária");
                            a.setCirurgiasAnteriores(random.nextDouble() < 0.3 ? "Extração de siso há 2 anos" : "Nenhuma");
                            a.setObservacoes(random.nextBoolean() ? "Higiene oral adequada" : "Recomendar melhoria da higiene");
                            anamneseService.salvar(a);
                            anamnesesCriadas++;

                            // Associar alergias na anamnese
                            if (!alergias.isEmpty() && random.nextDouble() < 0.4) {
                                try {
                                    AnamneseAlergia aa = new AnamneseAlergia();
                                    AnamneseAlergiaId aaId = new AnamneseAlergiaId();
                                    aaId.setIdAnamnese(a.getId());
                                    aaId.setIdAlergia(alergias.get(random.nextInt(alergias.size())).getId());
                                    aa.setId(aaId);
                                    aa.setIdAnamnese(a);
                                    aa.setIdAlergia(alergias.get(random.nextInt(alergias.size())));
                                    aa.setGravidade(Gravidade.values()[random.nextInt(Gravidade.values().length)]);
                                    anamneseAlergiaService.adicionar(aa);
                                } catch (Exception ignored) {}
                            }

                            // Associar doenças na anamnese
                            if (!doencas.isEmpty() && random.nextDouble() < 0.3) {
                                try {
                                    AnamneseDoenca ad = new AnamneseDoenca();
                                    AnamneseDoencaId adId = new AnamneseDoencaId();
                                    adId.setIdAnamnese(a.getId());
                                    adId.setIdDoenca(doencas.get(random.nextInt(doencas.size())).getId());
                                    ad.setId(adId);
                                    ad.setIdAnamnese(a);
                                    ad.setIdDoenca(doencas.get(random.nextInt(doencas.size())));
                                    ad.setDescricaoPaciente("Diagnosticado há " + (1 + random.nextInt(10)) + " anos.");
                                    anamneseDoencaService.adicionar(ad);
                                } catch (Exception ignored) {}
                            }
                        } catch (Exception ignored) {}
                    }

                    atendimentosCriados++;
                    atendimentos.add(atSalvo);
                } catch (Exception ignored) {}
            }
        }
        System.out.println("  Atendimentos: " + atendimentosCriados + " | Anamneses: " + anamnesesCriadas);

        // ═══ FASE 8 – PRESCRIÇÕES ═══
        System.out.println("\n── FASE 8: Prescrições ──");
        int prescCriadas = 0;
        for (int i = 0; i < 30; i++) {
            try {
                if (pacientes.isEmpty() || dentistas.isEmpty() || consultas.isEmpty() || atendimentos.isEmpty()) break;
                Paciente p = pacientes.get(random.nextInt(pacientes.size()));
                Dentista d = dentistas.get(random.nextInt(dentistas.size()));
                Consulta c = consultas.get(random.nextInt(consultas.size()));
                Atendimento a = atendimentos.get(random.nextInt(atendimentos.size()));

                Prescricao presc = new Prescricao();
                presc.setPaciente(p);
                presc.setDentista(d);
                presc.setConsulta(c);
                presc.setData(LocalDate.now().minusDays(random.nextInt(90)));
                presc.setMedicamento(MEDICAMENTOS_PRESC[random.nextInt(MEDICAMENTOS_PRESC.length)]);
                presc.setPosologia(POSOLOGIAS[random.nextInt(POSOLOGIAS.length)]);
                presc.setTempoTratamento((5 + random.nextInt(15)) + " dias");
                presc.setObservacoes(random.nextBoolean() ? "Tomar após as refeições." : "Evitar exposição solar durante o tratamento.");
                prescricaoService.salvar(presc);
                prescCriadas++;
            } catch (Exception ignored) {}
        }
        System.out.println("  Prescrições: " + prescCriadas);

        // ═══ FASE 9 – PLANOS DE TRATAMENTO ═══
        System.out.println("\n── FASE 9: Planos de Tratamento ──");
        EstadoPlanoTratamento[] estadosPlano = EstadoPlanoTratamento.values();
        int planosCriados = 0;
        for (int i = 0; i < 20; i++) {
            try {
                if (pacientes.isEmpty() || dentistas.isEmpty()) break;
                Paciente p = pacientes.get(random.nextInt(pacientes.size()));
                Dentista d = dentistas.get(random.nextInt(dentistas.size()));

                PlanoTratamento pt = new PlanoTratamento();
                pt.setPaciente(p);
                pt.setDentista(d);
                pt.setObjetivo(OBJETIVOS_TRATAMENTO[random.nextInt(OBJETIVOS_TRATAMENTO.length)]);
                pt.setEtapas("1. Avaliação inicial\n2. Plano de tratamento\n3. Execução\n4. Follow-up");
                pt.setProcedimentosPrevistos("Procedimentos conforme plano");
                pt.setValorEstimado(BigDecimal.valueOf(100 + random.nextInt(5000)));
                pt.setDataPrevistaInicio(LocalDate.now().minusMonths(random.nextInt(6)));
                pt.setDataPrevistaFim(pt.getDataPrevistaInicio().plusMonths(1 + random.nextInt(12)));
                EstadoPlanoTratamento estado = estadosPlano[random.nextInt(estadosPlano.length)];
                pt.setEstado(estado);
                pt.setProgresso(estado == EstadoPlanoTratamento.CONCLUIDO ? "100%" :
                    estado == EstadoPlanoTratamento.EM_ANDAMENTO ? (20 + random.nextInt(60)) + "%" : "0%");
                planoTratamentoService.salvar(pt);
                planosCriados++;
            } catch (Exception ignored) {}
        }
        System.out.println("  Planos de Tratamento: " + planosCriados);

        // ═══ FASE 10 – FATURAS E PAGAMENTOS ═══
        System.out.println("\n── FASE 10: Faturas e Pagamentos ──");
        int faturasCriadas = 0;
        int pagamentosCriados = 0;

        // Primeiro, associar procedimentos aos atendimentos que não têm
        for (Atendimento a : atendimentos) {
            try {
                if (a.getProcedimentos() == null || a.getProcedimentos().isEmpty()) {
                    if (!procedimentos.isEmpty()) {
                        Procedimento proc = procedimentos.get(random.nextInt(procedimentos.size()));
                        AtendimentoProcedimento ap = new AtendimentoProcedimento();
                        ap.setIdAtendimento(a);
                        ap.setIdProcedimento(proc);
                        ap.setQuantidade(1 + random.nextInt(3));
                        ap.setDesconto(BigDecimal.ZERO);
                        atendimentoProcedimentoService.salvar(ap);
                    }
                }
            } catch (Exception ignored) {}
        }

        for (int i = 0; i < 30 && i < atendimentos.size(); i++) {
            try {
                Atendimento a = atendimentos.get(i);
                Fatura fatura = faturaService.emitirFaturaPorAtendimento(a);

                // Pagar algumas faturas
                if (random.nextDouble() < 0.7) {
                    try {
                        Pagamento pag = new Pagamento();
                        pag.setIdFatura(fatura);
                        pag.setValorPago(fatura.getValorFinal() != null ? fatura.getValorFinal() : BigDecimal.valueOf(100));
                        pag.setDataPagamento(fatura.getDataEmissao() != null ? fatura.getDataEmissao() : LocalDate.now().minusDays(random.nextInt(30)));
                        MetodoPagamento[] metodos = MetodoPagamento.values();
                        pag.setMetodo(metodos[random.nextInt(metodos.length)]);
                        if (!admins.isEmpty()) {
                            pag.setIdUtilizador(admins.get(0));
                        }
                        pagamentoService.registrarPagamento(pag);
                        pagamentosCriados++;
                    } catch (Exception ignored) {}
                }

                faturasCriadas++;
            } catch (Exception ignored) {}
        }
        System.out.println("  Faturas: " + faturasCriadas + " | Pagamentos: " + pagamentosCriados);

        // ═══ FASE 11 – ESTOQUE E PEDIDOS ═══
        System.out.println("\n── FASE 11: Estoque e Pedidos ──");
        int movCriadas = 0;
        for (int i = 0; i < 30 && !materiais.isEmpty(); i++) {
            try {
                Material m = materiais.get(random.nextInt(materiais.size()));
                Assistente assistente = assistentes.isEmpty() ? null : assistentes.get(random.nextInt(assistentes.size()));
                boolean entrada = random.nextBoolean();
                TipoMovimentacao tipo = entrada ? TipoMovimentacao.ENTRADA : TipoMovimentacao.SAIDA;
                int qtd = 1 + random.nextInt(20);
                try {
                    movimentacaoEstoqueService.registarMovimentacao(
                        m.getId(),
                        assistente != null ? assistente.getId() : null,
                        tipo,
                        qtd,
                        entrada ? "Reposição de stock" : "Utilização em procedimento",
                        "Movimentação automática – demo"
                    );
                    movCriadas++;
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        }
        System.out.println("  Movimentações: " + movCriadas);

        int pedidosCriados = 0;
        for (int i = 0; i < 15 && !materiais.isEmpty() && !fornecedores.isEmpty() && !assistentes.isEmpty(); i++) {
            try {
                Material m = materiais.get(random.nextInt(materiais.size()));
                Fornecedor f = fornecedores.get(random.nextInt(fornecedores.size()));
                Assistente a = assistentes.get(random.nextInt(assistentes.size()));

                PedidoCompra p = new PedidoCompra();
                p.setIdFornecedor(f);
                p.setIdAssistente(a);
                p.setDataPedido(LocalDate.now().minusDays(random.nextInt(30)));

                ItemPedido item = new ItemPedido();
                ItemPedidoId itemId = new ItemPedidoId();
                item.setId(itemId);
                item.setIdMaterial(m);
                item.setQuantidade(m.getQuantidadeMinima() * 2);
                item.setValor(m.getValorUnitario() != null ? m.getValorUnitario() : BigDecimal.ONE);
                item.setIdPedido(p);

                List<ItemPedido> itens = new ArrayList<>();
                itens.add(item);

                // Adicionar mais alguns itens ocasionalmente
                if (random.nextDouble() < 0.3 && materiais.size() > 1) {
                    Material m2 = materiais.get(random.nextInt(materiais.size()));
                    if (!m2.getId().equals(m.getId())) {
                        ItemPedido item2 = new ItemPedido();
                        item2.setId(new ItemPedidoId());
                        item2.setIdMaterial(m2);
                        item2.setQuantidade(m2.getQuantidadeMinima());
                        item2.setValor(m2.getValorUnitario() != null ? m2.getValorUnitario() : BigDecimal.ONE);
                        item2.setIdPedido(p);
                        itens.add(item2);
                    }
                }

                PedidoCompra pedido = pedidoCompraService.criarPedido(p, itens);

                // Marcar alguns como recebidos
                if (random.nextDouble() < 0.4) {
                    try {
                        pedidoCompraService.marcarComoRecebido(pedido.getId());
                    } catch (Exception ignored) {}
                }

                pedidosCriados++;
            } catch (Exception ignored) {}
        }
        System.out.println("  Pedidos de Compra: " + pedidosCriados);

        // ═══ FASE 12 – AUDITORIA ═══
        System.out.println("\n── FASE 12: Auditoria ──");
        List<Utilizador> todosUtilizadores = new ArrayList<>();
        todosUtilizadores.addAll(admins);
        for (Dentista d : dentistas) {
            try { todosUtilizadores.add(d.getUtilizador()); } catch (Exception ignored) {}
        }
        for (Assistente a : assistentes) {
            try { todosUtilizadores.add(a.getUtilizador()); } catch (Exception ignored) {}
        }
        for (Recepcionista r : recepcionistas) {
            try { todosUtilizadores.add(r.getUtilizador()); } catch (Exception ignored) {}
        }

        int auditoriasCriadas = 0;
        for (int i = 0; i < 55 && !todosUtilizadores.isEmpty(); i++) {
            try {
                Utilizador u = todosUtilizadores.get(random.nextInt(todosUtilizadores.size()));
                String operacao = OPERACOES_AUDITORIA[random.nextInt(OPERACOES_AUDITORIA.length)];
                String descricao = switch (operacao) {
                    case "LOGIN" -> "Utilizador " + u.getPrimeiroNome() + " " + u.getUltimoNome() + " autenticou-se no sistema.";
                    case "LOGOUT" -> "Utilizador " + u.getPrimeiroNome() + " " + u.getUltimoNome() + " terminou sessão.";
                    case "CRIACAO_CONSULTA" -> "Nova consulta agendada para " + LocalDate.now().minusDays(random.nextInt(30));
                    case "CANCELAMENTO_CONSULTA" -> "Consulta cancelada – motivo: " + (random.nextBoolean() ? "indisponibilidade do paciente" : "reagendamento");
                    case "EMISSAO_PRESCRICAO" -> "Prescrição emitida para paciente da consulta #" + (1000 + random.nextInt(9999));
                    case "ALTERACAO_PLANO_TRATAMENTO" -> "Plano de tratamento atualizado – nova etapa adicionada";
                    case "CRIACAO_PEDIDO_COMPRA" -> "Pedido de compra nº " + (1000 + random.nextInt(9999)) + " registado";
                    case "MOVIMENTACAO_ESTOQUE" -> "Movimentação de stock: " + (random.nextBoolean() ? "entrada" : "saída") + " de material";
                    case "EMISSAO_FATURA" -> "Fatura emitida para atendimento #" + (1000 + random.nextInt(9999));
                    case "REGISTO_PAGAMENTO" -> "Pagamento registado para fatura #" + (1000 + random.nextInt(9999));
                    case "CADASTRO_PACIENTE" -> "Novo paciente registado no sistema";
                    case "ATUALIZACAO_PRONTUARIO" -> "Prontuário atualizado com novas informações clínicas";
                    case "REALIZACAO_ATENDIMENTO" -> "Atendimento realizado para consulta #" + (1000 + random.nextInt(9999));
                    case "CRIACAO_PLANO_TRATAMENTO" -> "Novo plano de tratamento criado para paciente";
                    case "ALTERACAO_ESTADO_CONSULTA" -> "Estado da consulta #" + (1000 + random.nextInt(9999)) + " alterado";
                    default -> "Operação registada no sistema.";
                };
                auditoriaService.registar(u, operacao, descricao);
                auditoriasCriadas++;
            } catch (Exception ignored) {}
        }
        System.out.println("  Registos de Auditoria: " + auditoriasCriadas);

        // ═══ RESUMO ═══
        System.out.println("\n======================================================");
        System.out.println("  GERAÇÃO DE DADOS CONCLUÍDA COM SUCESSO!");
        System.out.println("======================================================");
        System.out.println("  Utilizadores : " + utilizadorService.listarTodos().size());
        System.out.println("  Pacientes   : " + pacienteService.listarTodos().size());
        System.out.println("  Dentistas   : " + dentistaService.listarTodos().size());
        System.out.println("  Consultas   : " + consultaService.listarTodas().size());
        System.out.println("  Atendimentos: " + atendimentoService.listarTodos().size());
        System.out.println("  Faturas     : " + faturaService.listarTodos().size());
        System.out.println("======================================================\n");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private String gerarNIF() {
        return "1" + (10000000 + random.nextInt(90000000));
    }

    private String gerarEmail(String primeiroNome, String ultimoNome) {
        emailCounter++;
        return (primeiroNome.toLowerCase() + "." + ultimoNome.toLowerCase() + emailCounter + "@clinica.pt")
                .replaceAll("[çáàâãéèêíóôõú]", "a")
                .replaceAll("[^a-z0-9.@]", "");
    }

    private Utilizador criarUtilizadorBase(String tipo) {
        Utilizador u = new Utilizador();
        boolean masc = random.nextBoolean();
        String primeiroNome = masc ? FIRST_NAMES_MASC[random.nextInt(FIRST_NAMES_MASC.length)]
                                   : FIRST_NAMES_FEM[random.nextInt(FIRST_NAMES_FEM.length)];
        String ultimoNome = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        u.setPrimeiroNome(primeiroNome);
        u.setUltimoNome(ultimoNome);
        u.setSenha("Clinica2026!");
        u.setTipoUtilizador(tipo);
        u.setNif(gerarNIF());
        u.setTelefone("2" + (1000000 + random.nextInt(9000000)));
        u.setTelemovel("9" + (10000000 + random.nextInt(90000000)));
        u.setDataNascimento(LocalDate.of(1965 + random.nextInt(40), 1 + random.nextInt(12), 1 + random.nextInt(28)));
        u.setStatus("ATIVO");
        u.setRua("Rua " + (random.nextInt(100) + 1));
        u.setNumeroPorta(String.valueOf(random.nextInt(200) + 1));
        // Não setar email aqui – cada método define o seu
        return u;
    }
}

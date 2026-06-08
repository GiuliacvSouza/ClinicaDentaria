package controller.dentista;

import app.SessionContext;
import bll.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.*;
import model.enums.EstadoConsulta;
import model.enums.EstadoPlanoTratamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AtendimentoDentistaController extends BaseDentistaController {

    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "PT"));
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "PT"));

    // ─────────── HEADER ───────────
    @FXML private Label lblIniciais;
    @FXML private Label lblNomePaciente;
    @FXML private Label lblInfoConsulta;
    @FXML private Label lblEstadoConsulta;

    // ─────────── LEFT COLUMN ───────────
    @FXML private Label lblResumoNome;
    @FXML private Label lblResumoIdade;
    @FXML private Label lblResumoNif;
    @FXML private Label lblResumoTelefone;
    @FXML private Label lblResumoAlergias;
    @FXML private Label lblResumoMedicamentos;
    @FXML private TableView<HistoricoLinha> tblHistoricoClinico;
    @FXML private TableColumn<HistoricoLinha, String> colHistData;
    @FXML private TableColumn<HistoricoLinha, String> colHistDiag;

    // ─────────── TAB 1: PRONTUÁRIO ───────────
    @FXML private TextField txtGrupoSanguineo;
    @FXML private TextArea txtHistoricoMedico;
    @FXML private TextArea txtAlergiasProntuario;
    @FXML private TextArea txtMedicamentosProntuario;
    @FXML private TextArea txtHistoricoOdontologico;
    @FXML private TextArea txtObservacoesProntuario;
    @FXML private Button btnGuardarProntuario;

    // ─────────── TAB 2: ANAMNESE ───────────
    @FXML private Label lblStatusAnamnese;
    @FXML private CheckBox chkDiabetes;
    @FXML private CheckBox chkHipertensao;
    @FXML private CheckBox chkHepatite;
    @FXML private CheckBox chkDoencaGrave;
    @FXML private CheckBox chkFumante;
    @FXML private CheckBox chkUsaMedicamento;
    @FXML private CheckBox chkTemAlergia;
    @FXML private TextField txtMedicamentosAnamnese;
    @FXML private TextField txtAlergiasAnamnese;
    @FXML private TextField txtCirurgiasAnteriores;
    @FXML private TextField txtOutrasDoencas;
    @FXML private TextField txtHabitosRelevantes;
    @FXML private TextArea txtObservacoesAnamnese;
    @FXML private Button btnGuardarAnamnese;

    // ─────────── TAB 3: DIAGNÓSTICO ───────────
    @FXML private TextArea txtDiagnostico;
    @FXML private ComboBox<Procedimento> cbProcedimento;
    @FXML private Spinner<Integer> spProcQuantidade;
    @FXML private Button btnAdicionarProcedimento;
    @FXML private TableView<AtendimentoProcedimento> tblProcedimentos;
    @FXML private TableColumn<AtendimentoProcedimento, String> colProcNome;
    @FXML private TableColumn<AtendimentoProcedimento, String> colProcValor;
    @FXML private TableColumn<AtendimentoProcedimento, String> colProcQuantidade;
    @FXML private TableColumn<AtendimentoProcedimento, String> colProcTotal;
    @FXML private TextArea txtObservacoes;

    // ─────────── TAB 4: MATERIAIS ───────────
    @FXML private ComboBox<Material> cbMaterial;
    @FXML private Spinner<Integer> spMatQuantidade;
    @FXML private Button btnAdicionarMaterial;
    @FXML private TableView<MaterialUtilizado> tblMateriais;
    @FXML private TableColumn<MaterialUtilizado, String> colMatNome;
    @FXML private TableColumn<MaterialUtilizado, String> colMatQuantidade;
    @FXML private TableColumn<MaterialUtilizado, String> colMatValorUnit;
    @FXML private TableColumn<MaterialUtilizado, String> colMatTotal;

    // ─────────── TAB 5: PRESCRIÇÕES ───────────
    @FXML private TableView<Prescricao> tblPrescricoes;
    @FXML private TableColumn<Prescricao, String> colPrescData;
    @FXML private TableColumn<Prescricao, String> colPrescMedicamento;
    @FXML private TableColumn<Prescricao, String> colPrescPosologia;
    @FXML private TextField txtPrescMedicamento;
    @FXML private TextField txtPrescPosologia;
    @FXML private TextField txtPrescTempo;
    @FXML private TextArea txtPrescObservacoes;

    // ─────────── TAB 6: PLANO DE TRATAMENTO ───────────
    @FXML private TableView<PlanoTratamento> tblPlanosTratamento;
    @FXML private TableColumn<PlanoTratamento, String> colPlanoData;
    @FXML private TableColumn<PlanoTratamento, String> colPlanoObjetivo;
    @FXML private TableColumn<PlanoTratamento, String> colPlanoEstado;
    @FXML private TableColumn<PlanoTratamento, String> colPlanoProgresso;
    @FXML private TextField txtPlanoObjetivo;
    @FXML private TextArea txtPlanoEtapas;
    @FXML private TextArea txtPlanoProcedimentos;
    @FXML private DatePicker dpPlanoDataInicio;
    @FXML private DatePicker dpPlanoDataFim;
    @FXML private TextField txtPlanoProgresso;
    @FXML private Button btnPlanoNovo;
    @FXML private Button btnPlanoSalvar;

    // ─────────── ACTION BUTTONS ───────────
    @FXML private Button btnGuardar;
    @FXML private Button btnFinalizar;

    // ─────────── SERVICES ───────────
    @Autowired private ConsultaService consultaService;
    @Autowired private AtendimentoService atendimentoService;
    @Autowired private ProcedimentoService procedimentoService;
    @Autowired private MaterialService materialService;
    @Autowired private MaterialUtilizadoService materialUtilizadoService;
    @Autowired private AtendimentoProcedimentoService atendimentoProcedimentoService;
    @Autowired private ProntuarioService prontuarioService;
    @Autowired private AnamneseService anamneseService;
    @Autowired private PrescricaoService prescricaoService;
    @Autowired private PlanoTratamentoService planoService;
    @Autowired private PacienteService pacienteService;

    // ─────────── STATE ───────────
    private Consulta consulta;
    private Atendimento atendimento;
    private Paciente paciente;
    private Prontuario prontuario;
    private Anamnese anamnese;

    private ObservableList<AtendimentoProcedimento> procedimentosList = FXCollections.observableArrayList();
    private ObservableList<MaterialUtilizado> materiaisList = FXCollections.observableArrayList();
    private ObservableList<Prescricao> prescricoesList = FXCollections.observableArrayList();
    private ObservableList<PlanoTratamento> planosList = FXCollections.observableArrayList();

    // ─────────── INICIALIZAÇÃO ───────────
    @Override
    protected void inicializarEcra() {
        String query = SessionContext.getCurrentQuery();
        if (query == null || !query.startsWith("consulta=")) {
            mostrarErro("Nenhuma consulta especificada.");
            return;
        }
        try {
            Integer consultaId = Integer.parseInt(query.substring("consulta=".length()));
            carregarConsulta(consultaId);
        } catch (NumberFormatException e) {
            mostrarErro("ID de consulta invalido.");
        }
    }

    private void carregarConsulta(Integer consultaId) {
        try {
            consulta = consultaService.buscarPorId(consultaId);
            paciente = consulta.getIdPaciente();
            atendimento = atendimentoService.obterOuCriarPorConsulta(consulta);
            prontuario = prontuarioService.obterOuCriarPorPaciente(paciente);

            preencherHeader();
            preencherResumoPaciente();
            carregarHistoricoClinico();
            carregarProntuario();
            carregarAnamnese();
            carregarProcedimentos();
            carregarMateriais();
            carregarPrescricoes();
            carregarPlanosTratamento();
            configurarTabelas();
            configurarCombos();

            EstadoConsulta st = consulta.getStatus();
            boolean editavel = (st == EstadoConsulta.EM_CONSULTA);
            habilitarEdicao(editavel);
            if (st == EstadoConsulta.CONCLUIDA || st == EstadoConsulta.FATURADA) {
                btnFinalizar.setDisable(true);
            }
        } catch (Exception e) {
            mostrarErro("Erro ao carregar consulta: " + e.getMessage());
        }
    }

    // ─────────── HEADER ───────────
    private void preencherHeader() {
        Utilizador u = paciente.getUtilizador();
        if (u != null) {
            String nomeCompleto = (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim();
            lblNomePaciente.setText(nomeCompleto);
            String initials = "";
            if (!nvl(u.getPrimeiroNome()).isEmpty())
                initials += u.getPrimeiroNome().substring(0, 1).toUpperCase();
            if (!nvl(u.getUltimoNome()).isEmpty())
                initials += u.getUltimoNome().substring(0, 1).toUpperCase();
            lblIniciais.setText(initials.isEmpty() ? "P" : initials);
        }
        String dataStr = consulta.getDataHoraInicio() != null
                ? LocalDateTime.ofInstant(consulta.getDataHoraInicio(), ZoneId.systemDefault()).format(DATETIME_FMT)
                : "-";
        lblInfoConsulta.setText("Consulta #" + consulta.getId() + " | " + dataStr + " | " + nvl(consulta.getTipo()));
        lblEstadoConsulta.setText(textoEstado(consulta.getStatus()));
    }

    // ─────────── LEFT COLUMN ───────────
    private void preencherResumoPaciente() {
        Utilizador u = paciente.getUtilizador();
        if (u != null) {
            lblResumoNome.setText((nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim());
            lblResumoNif.setText("NIF: " + nvl(u.getNif()));
            lblResumoTelefone.setText("Tel: " + nvl(u.getTelemovel()));

            // Idade aproximada
            if (u.getDataNascimento() != null) {
                int idade = LocalDate.now().getYear() - u.getDataNascimento().getYear();
                lblResumoIdade.setText("Idade: " + idade + " anos");
            } else {
                lblResumoIdade.setText("Idade: -");
            }
        }
        if (prontuario != null) {
            lblResumoAlergias.setText("Alergias: " + nvl(prontuario.getAlergias()));
            lblResumoMedicamentos.setText("Medicamentos: " + nvl(prontuario.getMedicamentosUso()));
        }
    }

    private void carregarHistoricoClinico() {
        try {
            List<Consulta> consultas = consultaService.listarPorPaciente(paciente.getId()).stream()
                    .filter(c -> c.getStatus() == EstadoConsulta.CONCLUIDA || c.getStatus() == EstadoConsulta.FATURADA)
                    .sorted(Comparator.comparing(Consulta::getDataHoraInicio, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(10)
                    .toList();

            ObservableList<HistoricoLinha> linhas = FXCollections.observableArrayList();
            for (Consulta c : consultas) {
                String data = c.getDataHoraInicio() != null
                        ? LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault()).format(DATE_FMT)
                        : "-";
                String diag = "-";
                Atendimento at = atendimentoService.buscarPorConsulta(c);
                if (at != null) {
                    diag = nvl(at.getDiagnostico());
                    if (diag.isEmpty()) diag = nvl(at.getProcedimentosRealizados());
                }
                linhas.add(new HistoricoLinha(data, diag.isEmpty() ? "-" : diag));
            }
            tblHistoricoClinico.setItems(linhas);
        } catch (Exception ignored) {
        }
    }

    private void configurarTabelas() {
        colHistData.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getData()));
        colHistDiag.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescricao()));

        // Procedimentos
        colProcNome.setCellValueFactory(cell -> {
            Procedimento p = cell.getValue().getIdProcedimento();
            return new SimpleStringProperty(p != null ? nvl(p.getNome()) : "-");
        });
        colProcValor.setCellValueFactory(cell -> {
            Procedimento p = cell.getValue().getIdProcedimento();
            return new SimpleStringProperty(p != null && p.getValor() != null ? "EUR" + p.getValor() : "-");
        });
        colProcQuantidade.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getQuantidade() != null ? cell.getValue().getQuantidade().toString() : "1"));
        colProcTotal.setCellValueFactory(cell -> {
            Procedimento p = cell.getValue().getIdProcedimento();
            Integer q = cell.getValue().getQuantidade();
            BigDecimal total = BigDecimal.ZERO;
            if (p != null && p.getValor() != null && q != null) {
                total = p.getValor().multiply(BigDecimal.valueOf(q));
            }
            return new SimpleStringProperty("EUR" + total);
        });
        tblProcedimentos.setItems(procedimentosList);

        // Materiais
        colMatNome.setCellValueFactory(cell -> {
            Material m = cell.getValue().getMaterial();
            return new SimpleStringProperty(m != null ? nvl(m.getNome()) : "-");
        });
        colMatQuantidade.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getQuantidade() != null ? cell.getValue().getQuantidade().toString() : "0"));
        colMatValorUnit.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getValorUnitario() != null ? "EUR" + cell.getValue().getValorUnitario() : "-"));
        colMatTotal.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getValorTotal() != null ? "EUR" + cell.getValue().getValorTotal() : "-"));
        tblMateriais.setItems(materiaisList);

        // Prescrições
        colPrescData.setCellValueFactory(cell -> {
            LocalDate d = cell.getValue().getData();
            return new SimpleStringProperty(d != null ? d.format(DATE_FMT) : "-");
        });
        colPrescMedicamento.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getMedicamento())));
        colPrescPosologia.setCellValueFactory(cell -> new SimpleStringProperty(
                nvl(cell.getValue().getPosologia()) + " (" + nvl(cell.getValue().getTempoTratamento()) + ")"));
        tblPrescricoes.setItems(prescricoesList);

        // Planos
        colPlanoData.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDataPrevistaInicio() != null ? cell.getValue().getDataPrevistaInicio().format(DATE_FMT) : "-"));
        colPlanoObjetivo.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getObjetivo())));
        colPlanoEstado.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getEstado() != null ? cell.getValue().getEstado().getDescricao() : "-"));
        colPlanoProgresso.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getProgresso())));

        tblPlanosTratamento.setItems(planosList);
        tblPlanosTratamento.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) carregarPlanoParaEdicao(sel);
        });
    }

    private void configurarCombos() {
        try {
            List<Procedimento> procs = procedimentoService.listarTodos();
            cbProcedimento.setItems(FXCollections.observableArrayList(procs));
            cbProcedimento.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Procedimento p) {
                    return p == null ? "" : p.getNome() + (p.getValor() != null ? " - EUR" + p.getValor() : "");
                }
                @Override public Procedimento fromString(String s) { return null; }
            });

            List<Material> mats = materialService.listarTodos();
            cbMaterial.setItems(FXCollections.observableArrayList(mats));
            cbMaterial.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Material m) {
                    if (m == null) return "";
                    int stock = m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0;
                    return m.getNome() + " (Stock: " + stock + ")";
                }
                @Override public Material fromString(String s) { return null; }
            });
        } catch (Exception e) {
            mostrarErro("Erro ao carregar dados: " + e.getMessage());
        }

        spProcQuantidade.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        spMatQuantidade.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
    }

    // ─────────── TAB 1: PRONTUÁRIO ───────────
    private void carregarProntuario() {
        if (prontuario == null) return;
        txtGrupoSanguineo.setText(nvl(prontuario.getGrupoSanguineo()));
        txtHistoricoMedico.setText(nvl(prontuario.getHistoricoMedico()));
        txtAlergiasProntuario.setText(nvl(prontuario.getAlergias()));
        txtMedicamentosProntuario.setText(nvl(prontuario.getMedicamentosUso()));
        txtHistoricoOdontologico.setText(nvl(prontuario.getHistoricoOdontologico()));
        txtObservacoesProntuario.setText(nvl(prontuario.getObservacoes()));
    }

    @FXML
    private void guardarProntuario() {
        if (prontuario == null) return;
        prontuario.setGrupoSanguineo(txtGrupoSanguineo.getText());
        prontuario.setHistoricoMedico(txtHistoricoMedico.getText());
        prontuario.setAlergias(txtAlergiasProntuario.getText());
        prontuario.setMedicamentosUso(txtMedicamentosProntuario.getText());
        prontuario.setHistoricoOdontologico(txtHistoricoOdontologico.getText());
        prontuario.setObservacoes(txtObservacoesProntuario.getText());
        try {
            prontuarioService.atualizar(prontuario);
            preencherResumoPaciente();
            mostrarInfo("Prontuário atualizado com sucesso.");
        } catch (Exception e) {
            mostrarErro("Erro ao guardar prontuário: " + e.getMessage());
        }
    }

    // ─────────── TAB 2: ANAMNESE ───────────
    private void carregarAnamnese() {
        try {
            anamnese = anamneseService.buscarPorAtendimento(atendimento.getId()).orElse(null);
        } catch (Exception ignored) {}
        if (anamnese == null) {
            lblStatusAnamnese.setText("Sem anamnese registada");
            return;
        }
        lblStatusAnamnese.setText("Atualizada em " + (anamnese.getData() != null ? anamnese.getData().toString() : "-"));
        chkDiabetes.setSelected(Boolean.TRUE.equals(anamnese.getDiabetes()));
        chkHipertensao.setSelected(Boolean.TRUE.equals(anamnese.getHipertensao()));
        chkHepatite.setSelected(Boolean.TRUE.equals(anamnese.getHepatite()));
        chkDoencaGrave.setSelected(Boolean.TRUE.equals(anamnese.getDoencagrave()));
        chkFumante.setSelected(Boolean.TRUE.equals(anamnese.geteFumante()));
        chkUsaMedicamento.setSelected(Boolean.TRUE.equals(anamnese.getUsaMedicamento()));
        chkTemAlergia.setSelected(Boolean.TRUE.equals(anamnese.getTemAlergia()));
        txtMedicamentosAnamnese.setText(nvl(anamnese.getMedicamentos()));
        txtAlergiasAnamnese.setText(nvl(anamnese.getAlergias()));
        txtCirurgiasAnteriores.setText(nvl(anamnese.getCirurgiasAnteriores()));
        txtOutrasDoencas.setText(nvl(anamnese.getOutrasDoencas()));
        txtHabitosRelevantes.setText(nvl(anamnese.getHabitosRelevantes()));
        txtObservacoesAnamnese.setText(nvl(anamnese.getObservacoes()));
    }

    @FXML
    private void guardarAnamnese() {
        if (atendimento == null) return;
        try {
            var existingOpt = anamneseService.buscarPorAtendimento(atendimento.getId());
            Anamnese a = existingOpt.orElseGet(() -> {
                Anamnese nova = new Anamnese();
                nova.setIdAtendimento(atendimento);
                return nova;
            });
            a.setData(LocalDate.now());
            a.setMotivo("Avaliacao clinica");
            a.setDiabetes(chkDiabetes.isSelected());
            a.setHipertensao(chkHipertensao.isSelected());
            a.setHepatite(chkHepatite.isSelected());
            a.setDoencagrave(chkDoencaGrave.isSelected());
            a.seteFumante(chkFumante.isSelected());
            a.setUsaMedicamento(chkUsaMedicamento.isSelected());
            a.setTemAlergia(chkTemAlergia.isSelected());
            a.setMedicamentos(txtMedicamentosAnamnese.getText());
            a.setAlergias(txtAlergiasAnamnese.getText());
            a.setCirurgiasAnteriores(txtCirurgiasAnteriores.getText());
            a.setOutrasDoencas(txtOutrasDoencas.getText());
            a.setHabitosRelevantes(txtHabitosRelevantes.getText());
            a.setObservacoes(txtObservacoesAnamnese.getText());
            anamneseService.salvar(a);
            anamnese = a;
            lblStatusAnamnese.setText("Atualizada em " + LocalDate.now().toString());
            mostrarInfo("Anamnese gravada com sucesso.");
        } catch (Exception e) {
            mostrarErro("Erro ao guardar anamnese: " + e.getMessage());
        }
    }

    // ─────────── TAB 3: DIAGNÓSTICO ───────────
    private void carregarProcedimentos() {
        if (atendimento == null) return;
        txtDiagnostico.setText(nvl(atendimento.getDiagnostico()));
        txtObservacoes.setText(nvl(atendimento.getObservacoes()));
        if (atendimento.getProcedimentos() != null) {
            procedimentosList.setAll(atendimento.getProcedimentos());
        }
    }

    @FXML
    private void adicionarProcedimento() {
        Procedimento proc = cbProcedimento.getValue();
        if (proc == null) { mostrarErro("Selecione um procedimento."); return; }
        if (atendimento == null || atendimento.getId() == null) { mostrarErro("Atendimento nao iniciado."); return; }
        try {
            AtendimentoProcedimento ap = new AtendimentoProcedimento();
            ap.setIdAtendimento(atendimento);
            ap.setIdProcedimento(proc);
            ap.setQuantidade(spProcQuantidade.getValue());
            atendimentoProcedimentoService.salvar(ap);
            procedimentosList.add(ap);
            cbProcedimento.setValue(null);
            spProcQuantidade.getValueFactory().setValue(1);
        } catch (Exception e) {
            mostrarErro("Erro ao adicionar procedimento: " + e.getMessage());
        }
    }

    // ─────────── TAB 4: MATERIAIS ───────────
    private void carregarMateriais() {
        if (atendimento == null || atendimento.getId() == null) return;
        try {
            List<MaterialUtilizado> mats = materialUtilizadoService.listarPorAtendimento(atendimento.getId());
            materiaisList.setAll(mats);
        } catch (Exception ignored) {}
    }

    @FXML
    private void adicionarMaterial() {
        Material mat = cbMaterial.getValue();
        if (mat == null) { mostrarErro("Selecione um material."); return; }
        if (atendimento == null || atendimento.getId() == null) { mostrarErro("Atendimento nao iniciado."); return; }
        try {
            MaterialUtilizado mu = materialUtilizadoService.registar(atendimento.getId(), mat.getId(), spMatQuantidade.getValue());
            materiaisList.add(mu);
            cbMaterial.setValue(null);
            spMatQuantidade.getValueFactory().setValue(1);
        } catch (Exception e) {
            mostrarErro("Erro ao adicionar material: " + e.getMessage());
        }
    }

    // ─────────── TAB 5: PRESCRIÇÕES ───────────
    private void carregarPrescricoes() {
        try {
            List<Prescricao> prescricoes = prescricaoService.listarPorPaciente(paciente.getId());
            prescricoesList.setAll(prescricoes);
        } catch (Exception ignored) {}
    }

    @FXML
    private void emitirPrescricaoAtendimento() {
        String med = txtPrescMedicamento.getText();
        String pos = txtPrescPosologia.getText();
        if (med == null || med.isBlank()) { mostrarErro("Medicamento obrigatório."); return; }
        if (pos == null || pos.isBlank()) { mostrarErro("Posologia obrigatória."); return; }
        try {
            Prescricao p = new Prescricao();
            p.setPaciente(paciente);
            p.setDentista(dentistaLogado());
            p.setConsulta(consulta);
            p.setMedicamento(med);
            p.setPosologia(pos);
            p.setTempoTratamento(txtPrescTempo.getText());
            p.setObservacoes(txtPrescObservacoes.getText());
            p.setAssinatura(nomeDentista());
            p.setData(LocalDate.now());
            prescricaoService.salvar(p);
            mostrarInfo("Prescrição adicionada com sucesso!");
            txtPrescMedicamento.clear();
            txtPrescPosologia.clear();
            txtPrescTempo.clear();
            txtPrescObservacoes.clear();
            carregarPrescricoes();
        } catch (Exception e) {
            mostrarErro("Erro ao emitir prescrição: " + e.getMessage());
        }
    }

    // ─────────── TAB 6: PLANO DE TRATAMENTO ───────────
    private void carregarPlanosTratamento() {
        try {
            List<PlanoTratamento> planos = planoService.listarPorPaciente(paciente.getId());
            planosList.setAll(planos);
        } catch (Exception ignored) {}
    }

    private void carregarPlanoParaEdicao(PlanoTratamento p) {
        txtPlanoObjetivo.setText(nvl(p.getObjetivo()));
        txtPlanoEtapas.setText(nvl(p.getEtapas()));
        txtPlanoProcedimentos.setText(nvl(p.getProcedimentosPrevistos()));
        dpPlanoDataInicio.setValue(p.getDataPrevistaInicio());
        dpPlanoDataFim.setValue(p.getDataPrevistaFim());
        txtPlanoProgresso.setText(nvl(p.getProgresso()));
    }

    @FXML
    private void novoPlano() {
        txtPlanoObjetivo.clear();
        txtPlanoEtapas.clear();
        txtPlanoProcedimentos.clear();
        dpPlanoDataInicio.setValue(null);
        dpPlanoDataFim.setValue(null);
        txtPlanoProgresso.clear();
    }

    @FXML
    private void salvarPlanoAtendimento() {
        String obj = txtPlanoObjetivo.getText();
        if (obj == null || obj.isBlank()) { mostrarErro("Objetivo do tratamento é obrigatório."); return; }
        try {
            PlanoTratamento plano;
            PlanoTratamento selected = tblPlanosTratamento.getSelectionModel().getSelectedItem();
            if (selected != null) {
                plano = selected;
            } else {
                plano = new PlanoTratamento();
                plano.setPaciente(paciente);
                plano.setDentista(dentistaLogado());
            }
            plano.setObjetivo(obj);
            plano.setEtapas(txtPlanoEtapas.getText());
            plano.setProcedimentosPrevistos(txtPlanoProcedimentos.getText());
            plano.setDataPrevistaInicio(dpPlanoDataInicio.getValue());
            plano.setDataPrevistaFim(dpPlanoDataFim.getValue());
            plano.setProgresso(txtPlanoProgresso.getText());
            if (plano.getEstado() == null) plano.setEstado(EstadoPlanoTratamento.PLANEADO);
            if (plano.getValorEstimado() == null) plano.setValorEstimado(BigDecimal.ZERO);
            planoService.salvar(plano);
            mostrarInfo("Plano de tratamento guardado com sucesso!");
            novoPlano();
            carregarPlanosTratamento();
        } catch (Exception e) {
            mostrarErro("Erro ao salvar plano: " + e.getMessage());
        }
    }

    // ─────────── AÇÕES PRINCIPAIS ───────────
    private void habilitarEdicao(boolean enabled) {
        // Tab 1 - Prontuário
        txtGrupoSanguineo.setEditable(enabled);
        txtHistoricoMedico.setEditable(enabled);
        txtAlergiasProntuario.setEditable(enabled);
        txtMedicamentosProntuario.setEditable(enabled);
        txtHistoricoOdontologico.setEditable(enabled);
        txtObservacoesProntuario.setEditable(enabled);
        btnGuardarProntuario.setDisable(!enabled);

        // Tab 2 - Anamnese
        btnGuardarAnamnese.setDisable(!enabled);

        // Tab 3 - Diagnóstico
        txtDiagnostico.setEditable(enabled);
        txtObservacoes.setEditable(enabled);
        cbProcedimento.setDisable(!enabled);
        spProcQuantidade.setDisable(!enabled);
        btnAdicionarProcedimento.setDisable(!enabled);

        // Tab 4 - Materiais
        cbMaterial.setDisable(!enabled);
        spMatQuantidade.setDisable(!enabled);
        btnAdicionarMaterial.setDisable(!enabled);

        // Tab 5 - Prescrições
        // Fields kept enabled for easy input, validation at save time

        // Tab 6 - Plano
        btnPlanoNovo.setDisable(!enabled);
        btnPlanoSalvar.setDisable(!enabled);

        // Action buttons
        btnGuardar.setDisable(!enabled);
        btnFinalizar.setDisable(!enabled);
    }

    @FXML
    private void guardarAtendimento() {
        if (atendimento == null) { mostrarErro("Nenhum atendimento ativo."); return; }
        atendimento.setDiagnostico(txtDiagnostico.getText());
        atendimento.setObservacoes(txtObservacoes.getText());
        atendimento.setAssinaturaDentista(nomeDentista());
        atendimento.setProcedimentosRealizados(gerarResumoProcedimentos());
        try {
            atendimentoService.salvar(atendimento);
            mostrarInfo("Atendimento guardado com sucesso!");
        } catch (Exception e) {
            mostrarErro("Erro ao guardar atendimento: " + e.getMessage());
        }
    }

    @FXML
    private void finalizarConsulta() {
        // Save everything before finalizing
        guardarAtendimento();

        // Save prontuario
        if (prontuario != null) {
            guardarProntuario();
        }

        // Save anamnese
        try {
            var existingOpt = anamneseService.buscarPorAtendimento(atendimento.getId());
            if (existingOpt.isPresent() || algumCheckAnamneseSelecionado()) {
                guardarAnamnese();
            }
        } catch (Exception ignored) {}

        try {
            consultaService.finalizarConsulta(consulta.getId());
            consulta = consultaService.buscarPorId(consulta.getId());
            lblEstadoConsulta.setText("Concluída");
            habilitarEdicao(false);
            btnFinalizar.setDisable(true);
            btnGuardar.setDisable(true);
            mostrarInfo("Consulta concluída com sucesso!");
        } catch (Exception e) {
            mostrarErro("Erro ao finalizar consulta: " + e.getMessage());
        }
    }

    private boolean algumCheckAnamneseSelecionado() {
        return chkDiabetes.isSelected() || chkHipertensao.isSelected() || chkHepatite.isSelected()
                || chkDoencaGrave.isSelected() || chkFumante.isSelected() || chkUsaMedicamento.isSelected()
                || chkTemAlergia.isSelected();
    }

    private String gerarResumoProcedimentos() {
        StringBuilder sb = new StringBuilder();
        for (AtendimentoProcedimento ap : procedimentosList) {
            if (ap.getIdProcedimento() != null) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(ap.getIdProcedimento().getNome())
                        .append(" (x").append(ap.getQuantidade() != null ? ap.getQuantidade() : 1).append(")");
            }
        }
        return sb.toString();
    }

    @FXML
    private void voltar() {
        navegar("/fxml/dentista/agenda-dentista.fxml");
    }

    // ─────────── HELPERS ───────────
    private String textoEstado(EstadoConsulta estado) {
        if (estado == null) return "-";
        return switch (estado) {
            case AGENDADA -> "Agendada";
            case CONFIRMADA -> "Confirmada";
            case EM_ESPERA -> "Em espera";
            case EM_CONSULTA -> "Em consulta";
            case CONCLUIDA -> "Concluída";
            case CANCELADA -> "Cancelada";
            case FATURADA -> "Faturada";
            default -> estado.getDescricao();
        };
    }

    public static class HistoricoLinha {
        private final String data;
        private final String descricao;
        public HistoricoLinha(String data, String descricao) { this.data = data; this.descricao = descricao; }
        public String getData() { return data; }
        public String getDescricao() { return descricao; }
    }
}
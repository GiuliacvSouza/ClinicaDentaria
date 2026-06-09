package controller.dentista;

import bll.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.*;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProntuariosDentistaController extends BaseDentistaController {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "PT"));
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "PT"));

    // Pesquisa e filtros
    @FXML private TextField txtPesquisa;
    @FXML private TableView<Paciente> tblPacientes;
    @FXML private TableColumn<Paciente, String> colNomePaciente;
    @FXML private TableColumn<Paciente, String> colIdadePaciente;
    @FXML private TableColumn<Paciente, String> colTelefonePaciente;
    @FXML private TableColumn<Paciente, String> colUltimaConsulta;
    @FXML private Button btnTodos;
    @FXML private Button btnComHistorico;
    @FXML private Button btnComPlano;
    @FXML private Button btnComPrescricao;

    // Placeholder / Prontuário
    @FXML private VBox panePlaceholder;
    @FXML private VBox paneProntuario;
    @FXML private Label lblIniciais;
    @FXML private Label lblNomePaciente;
    @FXML private Label lblMetaPaciente;

    // Seção 1 - Dados do Paciente
    @FXML private Label lblDadosNome;
    @FXML private Label lblDadosDataNasc;
    @FXML private Label lblDadosIdade;
    @FXML private Label lblDadosNif;
    @FXML private Label lblDadosTelefone;
    @FXML private Label lblDadosEmail;
    @FXML private Label lblDadosMorada;

    // Seção 2 - Informações Médicas
    @FXML private Label lblMedAlergias;
    @FXML private Label lblMedMedicamentos;
    @FXML private Label lblMedDoencas;
    @FXML private Label lblMedCirurgias;
    @FXML private Label lblMedObservacoes;

    // Seção 3 - Histórico Odontológico
    @FXML private TableView<HistoricoLinha> tblHistoricoConsultas;
    @FXML private TableColumn<HistoricoLinha, String> colHistData;
    @FXML private TableColumn<HistoricoLinha, String> colHistTipo;
    @FXML private TableColumn<HistoricoLinha, String> colHistDentista;
    @FXML private TableColumn<HistoricoLinha, String> colHistEstado;
    @FXML private TableColumn<HistoricoLinha, String> colHistDiagnostico;

    // Seção 4 - Prescrições
    @FXML private TableView<Prescricao> tblPrescricoes;
    @FXML private TableColumn<Prescricao, String> colPrescData;
    @FXML private TableColumn<Prescricao, String> colPrescMedicamento;
    @FXML private TableColumn<Prescricao, String> colPrescPosologia;
    @FXML private TableColumn<Prescricao, String> colPrescDuracao;

    // Seção 5 - Planos de Tratamento
    @FXML private TableView<PlanoTratamento> tblPlanos;
    @FXML private TableColumn<PlanoTratamento, String> colPlanoDataInicio;
    @FXML private TableColumn<PlanoTratamento, String> colPlanoDataFim;
    @FXML private TableColumn<PlanoTratamento, String> colPlanoObjetivo;
    @FXML private TableColumn<PlanoTratamento, String> colPlanoEstado;
    @FXML private TableColumn<PlanoTratamento, String> colPlanoProgresso;

    // Seção 6 - Anamneses
    @FXML private TableView<Anamnese> tblAnamneses;
    @FXML private TableColumn<Anamnese, String> colAnamneseData;
    @FXML private TableColumn<Anamnese, String> colAnamneseResumo;

    // Services
    @Autowired private PacienteService pacienteService;
    @Autowired private ProntuarioService prontuarioService;
    @Autowired private AnamneseService anamneseService;
    @Autowired private ConsultaService consultaService;
    @Autowired private AtendimentoService atendimentoService;
    @Autowired private PrescricaoService prescricaoService;
    @Autowired private PlanoTratamentoService planoService;

    private ObservableList<Paciente> masterPacientes = FXCollections.observableArrayList();
    private FilteredList<Paciente> filteredPacientes;
    private Paciente pacienteAtual;
    private int filtroAtivo = 0; // 0=Todos, 1=ComHistórico, 2=ComPlano, 3=ComPrescricao

    @Override
    protected void inicializarEcra() {
        configurarListaPacientes();
        configurarTabelas();

        txtPesquisa.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        tblPacientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                carregarPacienteSelecionado(newSelection);
            } else {
                limparSelecao();
            }
        });

        carregarPacientes();
        atualizarEstiloChips(btnTodos);
    }

    private void configurarListaPacientes() {
        colNomePaciente.setCellValueFactory(cell -> {
            Utilizador u = cell.getValue().getUtilizador();
            return new SimpleStringProperty(u != null ? (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim() : "-");
        });

        colIdadePaciente.setCellValueFactory(cell -> {
            Utilizador u = cell.getValue().getUtilizador();
            if (u != null && u.getDataNascimento() != null) {
                int idade = LocalDate.now().getYear() - u.getDataNascimento().getYear();
                return new SimpleStringProperty(String.valueOf(idade));
            }
            return new SimpleStringProperty("-");
        });

        colTelefonePaciente.setCellValueFactory(cell -> {
            Utilizador u = cell.getValue().getUtilizador();
            return new SimpleStringProperty(u != null ? nvl(u.getTelemovel()) : "-");
        });

        colUltimaConsulta.setCellValueFactory(cell -> {
            try {
                List<Consulta> consultas = consultaService.listarPorPaciente(cell.getValue().getId());
                if (!consultas.isEmpty()) {
                    Consulta c = consultas.get(0);
                    if (c.getDataHoraInicio() != null) {
                        return new SimpleStringProperty(LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault()).format(DATE_FMT));
                    }
                }
            } catch (Exception ignored) {}
            return new SimpleStringProperty("-");
        });
    }

    private void configurarTabelas() {
        // Histórico de Consultas
        colHistData.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDataHora()));
        colHistTipo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTipo()));
        colHistDentista.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDentista()));
        colHistEstado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEstado()));
        colHistDiagnostico.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDiagnosticoProcedimentos()));

        // Prescrições
        colPrescData.setCellValueFactory(cell -> {
            LocalDate d = cell.getValue().getData();
            return new SimpleStringProperty(d != null ? d.format(DATE_FMT) : "-");
        });
        colPrescMedicamento.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getMedicamento())));
        colPrescPosologia.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getPosologia())));
        colPrescDuracao.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getTempoTratamento())));

        // Planos
        colPlanoDataInicio.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDataPrevistaInicio() != null ? cell.getValue().getDataPrevistaInicio().format(DATE_FMT) : "-"));
        colPlanoDataFim.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDataPrevistaFim() != null ? cell.getValue().getDataPrevistaFim().format(DATE_FMT) : "-"));
        colPlanoObjetivo.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getObjetivo())));
        colPlanoEstado.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getEstado() != null ? cell.getValue().getEstado().getDescricao() : "-"));
        colPlanoProgresso.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getProgresso())));

        // Anamneses
        colAnamneseData.setCellValueFactory(cell -> {
            LocalDate d = cell.getValue().getData();
            return new SimpleStringProperty(d != null ? d.format(DATE_FMT) : "-");
        });
        colAnamneseResumo.setCellValueFactory(cell -> {
            StringBuilder sb = new StringBuilder();
            Anamnese a = cell.getValue();
            if (Boolean.TRUE.equals(a.getDiabetes())) sb.append("Diabético. ");
            if (Boolean.TRUE.equals(a.getHipertensao())) sb.append("Hipertenso. ");
            if (Boolean.TRUE.equals(a.getHepatite())) sb.append("Hepatite. ");
            if (Boolean.TRUE.equals(a.geteFumante())) sb.append("Fumador. ");
            if (a.getObservacoes() != null && !a.getObservacoes().isBlank()) {
                sb.append("Obs: ").append(a.getObservacoes());
            }
            return new SimpleStringProperty(sb.isEmpty() ? "-" : sb.toString().trim());
        });
    }

    private void carregarPacientes() {
        try {
            masterPacientes.setAll(pacienteService.listarTodos());
            filteredPacientes = new FilteredList<>(masterPacientes, p -> true);
            tblPacientes.setItems(filteredPacientes);
        } catch (Exception e) {
            mostrarErro("Erro ao carregar pacientes: " + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        if (filteredPacientes == null) return;

        String termo = txtPesquisa.getText() != null ? txtPesquisa.getText().toLowerCase().trim() : "";
        int filtro = filtroAtivo;

        filteredPacientes.setPredicate(p -> {
            // Filtro por texto
            if (!termo.isEmpty()) {
                Utilizador u = p.getUtilizador();
                if (u == null) return false;
                String nome = (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).toLowerCase();
                String nif = nvl(u.getNif()).toLowerCase();
                String tel = nvl(u.getTelemovel()).toLowerCase();
                if (!nome.contains(termo) && !nif.contains(termo) && !tel.contains(termo)) {
                    return false;
                }
            }

            // Filtro por categoria
            if (filtro == 1) {
                try {
                    List<Consulta> consultas = consultaService.listarPorPaciente(p.getId());
                    return consultas.stream().anyMatch(c ->
                            c.getStatus() == EstadoConsulta.CONCLUIDA || c.getStatus() == EstadoConsulta.FATURADA);
                } catch (Exception e) {
                    return false;
                }
            } else if (filtro == 2) {
                try {
                    return !planoService.listarPorPaciente(p.getId()).isEmpty();
                } catch (Exception e) {
                    return false;
                }
            } else if (filtro == 3) {
                try {
                    return !prescricaoService.listarPorPaciente(p.getId()).isEmpty();
                } catch (Exception e) {
                    return false;
                }
            }

            return true;
        });
    }

    private void carregarPacienteSelecionado(Paciente paciente) {
        pacienteAtual = paciente;
        panePlaceholder.setVisible(false);
        paneProntuario.setVisible(true);

        Utilizador u = paciente.getUtilizador();
        String nomeCompleto = u != null ? (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim() : "Paciente";
        lblNomePaciente.setText(nomeCompleto);

        String initials = "";
        if (u != null) {
            String pNome = nvl(u.getPrimeiroNome());
            String uNome = nvl(u.getUltimoNome());
            if (!pNome.isEmpty()) initials += pNome.substring(0, 1).toUpperCase();
            if (!uNome.isEmpty()) initials += uNome.substring(0, 1).toUpperCase();
        }
        lblIniciais.setText(initials.isEmpty() ? "P" : initials);
        lblMetaPaciente.setText("NIF: " + (u != null ? nvl(u.getNif()) : "-") + " | Telemóvel: " + (u != null ? nvl(u.getTelemovel()) : "-"));

        // Seção 1: Dados do Paciente
        preencherDadosPaciente(u);

        // Seção 2: Informações Médicas (do prontuário)
        Prontuario prontuario = prontuarioService.obterOuCriarPorPaciente(paciente);
        preencherInfoMedicas(prontuario);

        // Seção 3: Histórico de Consultas
        carregarHistoricoConsultas(paciente);

        // Seção 4: Prescrições
        carregarPrescricoes(paciente);

        // Seção 5: Planos de Tratamento
        carregarPlanos(paciente);

        // Seção 6: Anamneses
        carregarAnamneses(paciente);
    }

    private void preencherDadosPaciente(Utilizador u) {
        if (u == null) {
            lblDadosNome.setText("-");
            lblDadosDataNasc.setText("-");
            lblDadosIdade.setText("-");
            lblDadosNif.setText("-");
            lblDadosTelefone.setText("-");
            lblDadosEmail.setText("-");
            lblDadosMorada.setText("-");
            return;
        }

        lblDadosNome.setText((nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim());
        if (u.getDataNascimento() != null) {
            lblDadosDataNasc.setText(u.getDataNascimento().format(DATE_FMT));
            int idade = LocalDate.now().getYear() - u.getDataNascimento().getYear();
            lblDadosIdade.setText(idade + " anos");
        } else {
            lblDadosDataNasc.setText("-");
            lblDadosIdade.setText("-");
        }
        lblDadosNif.setText(nvl(u.getNif()));
        lblDadosTelefone.setText(nvl(u.getTelemovel()));
        lblDadosEmail.setText(nvl(u.getEmail()));

        // Morada
        String morada = "";
        if (u.getRua() != null) morada += nvl(u.getRua());
        if (u.getNumeroPorta() != null) morada += ", n. " + nvl(u.getNumeroPorta());
        if (u.getCodigoPostal() != null) morada += " - " + nvl(u.getCodigoPostal().getCodigoPostal());
        lblDadosMorada.setText(morada.isBlank() ? "-" : morada);
    }

    private void preencherInfoMedicas(Prontuario p) {
        if (p == null) {
            lblMedAlergias.setText("-");
            lblMedMedicamentos.setText("-");
            lblMedDoencas.setText("-");
            lblMedCirurgias.setText("-");
            lblMedObservacoes.setText("-");
            return;
        }
        lblMedAlergias.setText(nvl(p.getAlergias()).isEmpty() ? "Nenhuma registada" : nvl(p.getAlergias()));
        lblMedMedicamentos.setText(nvl(p.getMedicamentosUso()).isEmpty() ? "Nenhum registado" : nvl(p.getMedicamentosUso()));
        lblMedDoencas.setText(nvl(p.getHistoricoMedico()).isEmpty() ? "Nenhuma registada" : nvl(p.getHistoricoMedico()));
        lblMedCirurgias.setText("-"); // Prontuario não tem campo específico de cirurgias
        lblMedObservacoes.setText(nvl(p.getObservacoes()).isEmpty() ? "-" : nvl(p.getObservacoes()));
    }

    private void carregarHistoricoConsultas(Paciente paciente) {
        try {
            List<Consulta> consultas = consultaService.listarPorPaciente(paciente.getId()).stream()
                    .sorted(Comparator.comparing(Consulta::getDataHoraInicio, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            ObservableList<HistoricoLinha> linhas = FXCollections.observableArrayList();
            for (Consulta c : consultas) {
                String dataStr = c.getDataHoraInicio() != null
                        ? LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault()).format(DATE_TIME_FMT)
                        : "-";

                Utilizador uD = c.getIdDentista() != null ? c.getIdDentista().getUtilizador() : null;
                String dentistaStr = uD != null ? (nvl(uD.getPrimeiroNome()) + " " + nvl(uD.getUltimoNome())).trim() : "-";

                String estadoStr = textoEstado(c.getStatus());

                Atendimento at = atendimentoService.buscarPorConsulta(c);
                String diagProcStr = "Sem atendimento iniciado.";
                if (at != null) {
                    diagProcStr = "Diagnóstico: " + nvl(at.getDiagnostico()) + "\nProcedimentos: " + nvl(at.getProcedimentosRealizados());
                }

                linhas.add(new HistoricoLinha(dataStr, nvl(c.getTipo()), dentistaStr, estadoStr, diagProcStr));
            }

            tblHistoricoConsultas.setItems(linhas);
        } catch (Exception e) {
            mostrarErro("Erro ao carregar histórico: " + e.getMessage());
        }
    }

    private void carregarPrescricoes(Paciente paciente) {
        try {
            List<Prescricao> prescricoes = prescricaoService.listarPorPaciente(paciente.getId());
            tblPrescricoes.setItems(FXCollections.observableArrayList(prescricoes));
        } catch (Exception ignored) {}
    }

    private void carregarPlanos(Paciente paciente) {
        try {
            List<PlanoTratamento> planos = planoService.listarPorPaciente(paciente.getId());
            tblPlanos.setItems(FXCollections.observableArrayList(planos));
        } catch (Exception ignored) {}
    }

    private void carregarAnamneses(Paciente paciente) {
        try {
            List<Consulta> consultas = consultaService.listarPorPaciente(paciente.getId());
            ObservableList<Anamnese> anamneses = FXCollections.observableArrayList();
            for (Consulta c : consultas) {
                Atendimento at = atendimentoService.buscarPorConsulta(c);
                if (at != null) {
                    var opt = anamneseService.buscarPorAtendimento(at.getId());
                    opt.ifPresent(anamneses::add);
                }
            }
            tblAnamneses.setItems(anamneses);
        } catch (Exception ignored) {}
    }

    private void limparSelecao() {
        pacienteAtual = null;
        panePlaceholder.setVisible(true);
        paneProntuario.setVisible(false);
    }

    // Filtros
    @FXML private void filtrarTodos() { filtroAtivo = 0; atualizarEstiloChips(btnTodos); aplicarFiltros(); }
    @FXML private void filtrarComHistorico() { filtroAtivo = 1; atualizarEstiloChips(btnComHistorico); aplicarFiltros(); }
    @FXML private void filtrarComPlano() { filtroAtivo = 2; atualizarEstiloChips(btnComPlano); aplicarFiltros(); }
    @FXML private void filtrarComPrescricao() { filtroAtivo = 3; atualizarEstiloChips(btnComPrescricao); aplicarFiltros(); }

    private void atualizarEstiloChips(Button ativo) {
        List<Button> chips = List.of(btnTodos, btnComHistorico, btnComPlano, btnComPrescricao);
        for (Button btn : chips) {
            if (btn == null) continue;
            if (btn == ativo) {
                btn.getStyleClass().removeAll("filter-chip");
                if (!btn.getStyleClass().contains("nav-button-active")) {
                    btn.getStyleClass().add("nav-button-active");
                }
                btn.setStyle("-fx-background-color: #e8f4ff; -fx-text-fill: #0066cc;");
            } else {
                btn.getStyleClass().removeAll("nav-button-active");
                if (!btn.getStyleClass().contains("filter-chip")) {
                    btn.getStyleClass().add("filter-chip");
                }
                btn.setStyle(null);
            }
        }
    }

    // Ações
    @FXML
    private void verHistoricoCompleto() {
        if (pacienteAtual != null) {
            // Redirecionar para agenda com o paciente selecionado
            // Por enquanto apenas mostra info
            mostrarInfo("Histórico completo do paciente disponível na seção acima.");
        }
    }

    private String textoEstado(EstadoConsulta estado) {
        if (estado == null) return "-";
        return switch (estado) {
            case AGENDADA -> "Agendada";
            case CONFIRMADA -> "Confirmada";
            case EM_ESPERA -> "Em espera";
            case EM_CONSULTA -> "Em consulta";
            case CONCLUIDA -> "Concluida";
            case CANCELADA -> "Cancelada";
            default -> estado.getDescricao();
        };
    }

    public static class HistoricoLinha {
        private final String dataHora;
        private final String tipo;
        private final String dentista;
        private final String estado;
        private final String diagnosticoProcedimentos;

        public HistoricoLinha(String dataHora, String tipo, String dentista, String estado, String diagnosticoProcedimentos) {
            this.dataHora = dataHora;
            this.tipo = tipo;
            this.dentista = dentista;
            this.estado = estado;
            this.diagnosticoProcedimentos = diagnosticoProcedimentos;
        }

        public String getDataHora() { return dataHora; }
        public String getTipo() { return tipo; }
        public String getDentista() { return dentista; }
        public String getEstado() { return estado; }
        public String getDiagnosticoProcedimentos() { return diagnosticoProcedimentos; }
    }
}
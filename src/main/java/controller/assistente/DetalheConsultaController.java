package controller.assistente;

import bll.AtendimentoService;
import bll.ConsultaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Atendimento;
import model.AtendimentoProcedimento;
import model.Consulta;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Controller do modal de detalhes de uma consulta — visão do Assistente.
 * Mostra apenas informação operacional: hora, paciente, dentista, tipo,
 * observações administrativas e procedimentos associados.
 * Não expõe diagnóstico clínico.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DetalheConsultaController {

    private static final DateTimeFormatter HORA_FMT     = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final DateTimeFormatter DATA_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private Label lblTituloModal;
    @FXML private Label lblSubtituloModal;
    @FXML private Label lblEstadoBadge;
    @FXML private Label lblHora;
    @FXML private Label lblDuracao;
    @FXML private Label lblPaciente;
    @FXML private Label lblDentista;
    @FXML private Label lblTipo;
    @FXML private Label lblDataMarcacao;
    @FXML private VBox  paneObservacoes;
    @FXML private Label lblObservacoes;

    @FXML private TableView<AtendimentoProcedimento>           tblProcedimentos;
    @FXML private TableColumn<AtendimentoProcedimento, String> colProcNome;
    @FXML private TableColumn<AtendimentoProcedimento, String> colProcTipo;
    @FXML private TableColumn<AtendimentoProcedimento, String> colProcQtd;
    @FXML private TableColumn<AtendimentoProcedimento, String> colProcDuracao;
    @FXML private TableColumn<AtendimentoProcedimento, String> colProcValor;
    @FXML private Label lblTotalProcedimentos;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private ConsultaService    consultaService;
    @Autowired private AtendimentoService atendimentoService;

    private Stage stage;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        configurarTabelaProcedimentos();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void carregarConsulta(Integer idConsulta) {
        try {
            Consulta consulta = consultaService.buscarPorId(idConsulta);
            preencherDados(consulta);
            carregarProcedimentos(consulta);
        } catch (Exception e) {
            lblTituloModal.setText("Detalhes da Consulta");
            lblSubtituloModal.setText("Não foi possível carregar os dados.");
        }
    }

    // ─── Preenchimento ────────────────────────────────────────────────────────

    private void preencherDados(Consulta c) {
        String hora = "-";
        String dataHoraStr = "-";
        if (c.getDataHoraInicio() != null) {
            LocalDateTime ldt = LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault());
            hora = ldt.format(HORA_FMT);
            dataHoraStr = ldt.format(DATETIME_FMT);
        }

        lblTituloModal.setText("Detalhes da Consulta — " + hora);
        lblSubtituloModal.setText(dataHoraStr);
        if (stage != null) stage.setTitle("Detalhes da Consulta — " + hora);

        // Badge de estado
        EstadoConsulta estado = c.getStatus();
        lblEstadoBadge.setText(textoEstado(estado));
        lblEstadoBadge.getStyleClass().setAll("status-pill", classeEstado(estado));

        // Campos
        lblHora.setText(hora);
        lblDuracao.setText(c.getDuracao() != null ? c.getDuracao() + " min" : "-");

        String nomePaciente = "-";
        if (c.getIdPaciente() != null && c.getIdPaciente().getUtilizador() != null) {
            nomePaciente = nomePessoa(c.getIdPaciente().getUtilizador().getPrimeiroNome(),
                    c.getIdPaciente().getUtilizador().getUltimoNome());
        }
        lblPaciente.setText(nomePaciente);

        String nomeDentista = "-";
        if (c.getIdDentista() != null && c.getIdDentista().getUtilizador() != null) {
            nomeDentista = "Dr(a). " + nomePessoa(c.getIdDentista().getUtilizador().getPrimeiroNome(),
                    c.getIdDentista().getUtilizador().getUltimoNome());
        }
        lblDentista.setText(nomeDentista);

        lblTipo.setText(vazio(c.getTipo()));
        lblDataMarcacao.setText(c.getDataMarcacao() != null ? c.getDataMarcacao().format(DATA_FMT) : "-");

        // Observações — só mostra se existirem
        String obs = extrairObservacoesAdministrativas(c.getObservacoes());
        if (obs != null && !obs.isBlank()) {
            lblObservacoes.setText(obs);
            paneObservacoes.setVisible(true);
            paneObservacoes.setManaged(true);
        }
    }

    private void carregarProcedimentos(Consulta consulta) {
        List<AtendimentoProcedimento> procs = Collections.emptyList();
        try {
            Atendimento atendimento = atendimentoService.buscarPorConsulta(consulta);
            if (atendimento != null && atendimento.getProcedimentos() != null) {
                procs = atendimento.getProcedimentos();
            }
        } catch (Exception ignored) { /* sem atendimento associado */ }

        tblProcedimentos.setItems(FXCollections.observableArrayList(procs));
        int total = procs.size();
        lblTotalProcedimentos.setText(total == 0 ? "0 procedimentos"
                : total == 1 ? "1 procedimento"
                : total + " procedimentos");
    }

    private void configurarTabelaProcedimentos() {
        colProcNome.setCellValueFactory(c -> {
            var p = c.getValue().getIdProcedimento();
            return new SimpleStringProperty(p != null && p.getNome() != null ? p.getNome() : "-");
        });

        colProcTipo.setCellValueFactory(c -> {
            var p = c.getValue().getIdProcedimento();
            return new SimpleStringProperty(p != null && p.getTipo() != null ? p.getTipo() : "-");
        });

        colProcQtd.setCellValueFactory(c -> {
            Integer qty = c.getValue().getQuantidade();
            return new SimpleStringProperty(qty != null ? String.valueOf(qty) : "1");
        });
        colProcQtd.setStyle("-fx-alignment: CENTER;");

        colProcDuracao.setCellValueFactory(c -> {
            var p = c.getValue().getIdProcedimento();
            return new SimpleStringProperty(
                    p != null && p.getDuracaoEstimada() != null ? p.getDuracaoEstimada() + " min" : "-");
        });
        colProcDuracao.setStyle("-fx-alignment: CENTER;");

        colProcValor.setCellValueFactory(c -> {
            var p = c.getValue().getIdProcedimento();
            if (p == null || p.getValor() == null) return new SimpleStringProperty("-");
            BigDecimal val = p.getValor();
            BigDecimal desc = c.getValue().getDesconto();
            if (desc != null && desc.compareTo(BigDecimal.ZERO) > 0) {
                val = val.subtract(desc);
            }
            return new SimpleStringProperty(String.format("%.2f €", val));
        });
        colProcValor.setStyle("-fx-alignment: CENTER_RIGHT;");
    }

    // ─── Ação ─────────────────────────────────────────────────────────────────

    @FXML
    private void fechar() {
        if (stage != null) stage.close();
    }

    // ─── Utilitários ──────────────────────────────────────────────────────────

    private String nomePessoa(String primeiro, String ultimo) {
        String a = primeiro != null ? primeiro.trim() : "";
        String b = ultimo   != null ? ultimo.trim()   : "";
        String nome = (a + " " + b).trim();
        return nome.isBlank() ? "-" : nome;
    }

    private String vazio(String v) {
        return v == null || v.isBlank() ? "-" : v.trim();
    }

    /**
     * Extrai apenas as observações administrativas do campo observacoes da Consulta.
     * As linhas que começam por "Procedimento:", "Tipo de consulta:" e "Seguro:"
     * são dados de sistema, não observações do assistente.
     */
    private String extrairObservacoesAdministrativas(String obs) {
        if (obs == null || obs.isBlank()) return null;
        StringBuilder sb = new StringBuilder();
        for (String linha : obs.split("\\R")) {
            if (linha.startsWith("Procedimento:") || linha.startsWith("Tipo de consulta:")
                    || linha.startsWith("Seguro:")) continue;
            if (!linha.isBlank()) sb.append(linha.trim()).append("\n");
        }
        return sb.toString().trim();
    }

    private String textoEstado(EstadoConsulta e) {
        if (e == null) return "-";
        return switch (e) {
            case AGENDADA    -> "Agendada";
            case CONFIRMADA  -> "Confirmada";
            case EM_ESPERA   -> "Em espera";
            case EM_CONSULTA -> "Em consulta";
            case CONCLUIDA   -> "Concluída";
            case CANCELADA   -> "Cancelada";
            case PENDENTE    -> "Pendente";
            default          -> e.getDescricao();
        };
    }

    private String classeEstado(EstadoConsulta e) {
        if (e == null) return "status-agendado";
        return switch (e) {
            case AGENDADA    -> "status-agendado";
            case CONFIRMADA  -> "status-confirmado";
            case EM_ESPERA   -> "status-em-espera";
            case EM_CONSULTA -> "status-em-consulta";
            case CONCLUIDA   -> "status-concluido";
            case CANCELADA   -> "status-cancelado";
            default          -> "status-agendado";
        };
    }
}

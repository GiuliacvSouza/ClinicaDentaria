package controller.assistente;

import bll.ConsultaService;
import bll.MaterialService;
import bll.PedidoCompraService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DashboardAssistenteController extends BaseAssistenteController {

    private static final DateTimeFormatter DATA_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new java.util.Locale("pt", "PT"));

    // ─── FXML ────────────────────────────────────────────────────────────────

    @FXML private Label lblDataHoje;
    @FXML private Label lblTotalConsultas;
    @FXML private Label lblEmEspera;
    @FXML private Label lblEmConsulta;
    @FXML private Label lblAlertasStock;
    @FXML private Label lblPedidosPendentes;
    @FXML private VBox  containerProximasConsultas;
    @FXML private VBox  containerAlertas;

    // ─── Serviços ─────────────────────────────────────────────────────────────

    @Autowired private ConsultaService    consultaService;
    @Autowired private MaterialService    materialService;
    @Autowired private PedidoCompraService pedidoCompraService;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        lblDataHoje.setText(LocalDate.now().format(DATA_FORMATTER));
        carregarEstatisticas();
        carregarProximasConsultas();
        carregarAlertasStock();
    }

    // ─── Carregamento de dados ────────────────────────────────────────────────

    private void carregarEstatisticas() {
        try {
            LocalDate hoje = LocalDate.now();

            long totalHoje = consultaService.listarTodasAgendadas().stream()
                    .filter(c -> c.getDataHoraInicio() != null)
                    .filter(c -> java.time.LocalDateTime
                            .ofInstant(c.getDataHoraInicio(), java.time.ZoneId.systemDefault())
                            .toLocalDate().equals(hoje))
                    .count();

            long emEspera = consultaService.listarPorStatus(EstadoConsulta.EM_ESPERA).size();

            long emConsulta = consultaService.listarPorStatus(EstadoConsulta.EM_CONSULTA).size();

            long alertas = materialService.listarTodos().stream()
                    .filter(m -> m.getQuantidadeAtual() != null
                            && m.getQuantidadeMinima() != null
                            && m.getQuantidadeAtual() <= m.getQuantidadeMinima())
                    .count();

            long pedidosPendentes = pedidoCompraService.listarTodos().stream()
                    .count(); // PedidoCompra ainda não tem campo estado — conta todos

            lblTotalConsultas.setText(String.valueOf(totalHoje));
            lblEmEspera.setText(String.valueOf(emEspera));
            lblEmConsulta.setText(String.valueOf(emConsulta));
            lblAlertasStock.setText(String.valueOf(alertas));
            lblPedidosPendentes.setText(String.valueOf(pedidosPendentes));

        } catch (Exception e) {
            System.err.println("[DASHBOARD-ASSISTENTE] Erro ao carregar estatísticas: " + e.getMessage());
        }
    }

    private void carregarProximasConsultas() {
        containerProximasConsultas.getChildren().clear();
        try {
            LocalDate hoje = LocalDate.now();
            var consultas = consultaService.listarTodasAgendadas().stream()
                    .filter(c -> c.getDataHoraInicio() != null)
                    .filter(c -> java.time.LocalDateTime
                            .ofInstant(c.getDataHoraInicio(), java.time.ZoneId.systemDefault())
                            .toLocalDate().equals(hoje))
                    .sorted(java.util.Comparator.comparing(
                            model.dto.ConsultaAgendadaDTO::getDataHoraInicio,
                            java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                    .limit(5)
                    .toList();

            if (consultas.isEmpty()) {
                Label vazio = new Label("Sem consultas agendadas para hoje.");
                vazio.getStyleClass().add("section-caption");
                containerProximasConsultas.getChildren().add(vazio);
                return;
            }

            for (var c : consultas) {
                VBox card = criarCardConsultaResumido(c);
                containerProximasConsultas.getChildren().add(card);
            }
        } catch (Exception e) {
            Label erro = new Label("Não foi possível carregar as consultas.");
            erro.getStyleClass().add("section-caption");
            containerProximasConsultas.getChildren().add(erro);
        }
    }

    private void carregarAlertasStock() {
        containerAlertas.getChildren().clear();
        try {
            var materiais = materialService.listarTodos().stream()
                    .filter(m -> m.getQuantidadeAtual() != null
                            && m.getQuantidadeMinima() != null
                            && m.getQuantidadeAtual() <= m.getQuantidadeMinima())
                    .limit(6)
                    .toList();

            if (materiais.isEmpty()) {
                Label ok = new Label("Sem alertas de stock. Tudo em ordem.");
                ok.getStyleClass().add("section-caption");
                containerAlertas.getChildren().add(ok);
                return;
            }

            for (var m : materiais) {
                VBox alerta = criarCardAlerta(m);
                containerAlertas.getChildren().add(alerta);
            }
        } catch (Exception e) {
            Label erro = new Label("Não foi possível carregar os alertas.");
            erro.getStyleClass().add("section-caption");
            containerAlertas.getChildren().add(erro);
        }
    }

    // ─── Builders de cards ─────────────────────────────────────────────────────

    private VBox criarCardConsultaResumido(model.dto.ConsultaAgendadaDTO c) {
        VBox card = new VBox(4);
        card.getStyleClass().add("consulta-card");

        String hora = c.getDataHoraInicio() != null
                ? java.time.LocalDateTime
                .ofInstant(c.getDataHoraInicio(), java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
                : "--:--";

        Label lblHora = new Label(hora);
        lblHora.getStyleClass().add("consulta-hora");

        String nomePaciente = c.getNomePaciente() != null ? c.getNomePaciente() : "Paciente";
        Label lblPaciente = new Label(nomePaciente);
        lblPaciente.getStyleClass().add("consulta-paciente");

        String dentista = c.getNomeDentista() != null ? "Dr(a). " + c.getNomeDentista() : "";
        String proc = c.getProcedimento() != null ? c.getProcedimento() : "";
        Label lblMeta = new Label(dentista + (dentista.isEmpty() || proc.isEmpty() ? "" : " · ") + proc);
        lblMeta.getStyleClass().add("consulta-meta");

        card.getChildren().addAll(lblHora, lblPaciente, lblMeta);
        return card;
    }

    private VBox criarCardAlerta(model.Material m) {
        boolean critico = m.getQuantidadeAtual() != null && m.getQuantidadeAtual() == 0;

        VBox card = new VBox(4);
        card.getStyleClass().add(critico ? "alerta-card-critico" : "alerta-card");

        Label nome = new Label(m.getNome() != null ? m.getNome() : "Material");
        nome.getStyleClass().add("alerta-titulo");

        String stockStr = (m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0)
                + " / mín " + (m.getQuantidadeMinima() != null ? m.getQuantidadeMinima() : 0)
                + " " + (m.getUnidadeMedida() != null ? m.getUnidadeMedida() : "");
        Label descricao = new Label(critico ? "⚠ Stock zerado — " + stockStr : "Stock baixo — " + stockStr);
        descricao.getStyleClass().add("alerta-descricao");

        card.getChildren().addAll(nome, descricao);
        return card;
    }
}

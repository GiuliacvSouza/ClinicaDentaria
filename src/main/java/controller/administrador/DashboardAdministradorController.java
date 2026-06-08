package controller.administrador;

import bll.ConsultaService;
import bll.DentistaService;
import bll.FaturaService;
import bll.MaterialService;
import bll.PacienteService;
import bll.PagamentoService;
import bll.PedidoCompraService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.Material;
import model.enums.EstadoFatura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DashboardAdministradorController extends BaseAdministradorController {

    private static final DateTimeFormatter DATA_FMT =
            DateTimeFormatter.ofPattern("d 'de' MMMM", new Locale("pt", "PT"));

    @FXML private Label lblSaudacao;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblConsultasHoje;
    @FXML private Label lblDentistasAtivos;
    @FXML private Label lblPacientesRegistados;
    @FXML private Label lblReceitasMes;
    @FXML private Label lblDespesasMes;
    @FXML private Label lblLucroEstimado;
    @FXML private Label lblAlertasPendentes;
    @FXML private VBox containerAlertas;
    @FXML private VBox containerMateriaisCriticos;

    @Autowired private ConsultaService consultaService;
    @Autowired private DentistaService dentistaService;
    @Autowired private PacienteService pacienteService;
    @Autowired private FaturaService faturaService;
    @Autowired private PagamentoService pagamentoService;
    @Autowired private MaterialService materialService;
    @Autowired private PedidoCompraService pedidoCompraService;

    @Override
    protected void inicializarEcra() {
        String nome = utilizadorLogado() != null
                ? utilizadorLogado().getPrimeiroNome() : "Administrador";
        set(lblSaudacao, "Painel de Administracao");
        set(lblSubtitulo, "Resumo geral da clinica, " + LocalDate.now().format(DATA_FMT) + ".");
        carregarIndicadores();
        carregarAlertas();
        carregarMateriaisCriticos();
    }

    private void carregarIndicadores() {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDate inicioMes = hoje.withDayOfMonth(1);

            // Consultas do dia
            long consultasHoje = consultaService.listarTodasAgendadas().stream()
                    .filter(c -> c.getDataHoraInicio() != null)
                    .filter(c -> java.time.LocalDateTime
                            .ofInstant(c.getDataHoraInicio(), java.time.ZoneId.systemDefault())
                            .toLocalDate().equals(hoje))
                    .count();
            set(lblConsultasHoje, String.valueOf(consultasHoje));

            // Dentistas ativos
            long dentistasAtivos = dentistaService.listarTodos().stream()
                    .filter(d -> d.getAtivo() != null && d.getAtivo())
                    .count();
            set(lblDentistasAtivos, String.valueOf(dentistasAtivos));

            // Pacientes registados
            long totalPacientes = pacienteService.listarTodos() != null
                    ? pacienteService.listarTodos().size() : 0;
            set(lblPacientesRegistados, String.valueOf(totalPacientes));

            // Receitas do mês (faturas PAGA do mês)
            BigDecimal receitasMes = BigDecimal.ZERO;
            if (faturaService.listarTodos() != null) {
                receitasMes = faturaService.listarTodos().stream()
                        .filter(f -> f.getEstado() == EstadoFatura.PAGA)
                        .filter(f -> f.getDataEmissao() != null
                                && !f.getDataEmissao().isBefore(inicioMes)
                                && !f.getDataEmissao().isAfter(hoje))
                        .map(f -> f.getValorFinal() != null ? f.getValorFinal() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            set(lblReceitasMes, "€ " + formatarValor(receitasMes));

            // Despesas do mês (pagamentos de compras - valor aproximado)
            BigDecimal despesasMes = BigDecimal.ZERO;
            if (pagamentoService != null) {
                try {
                    despesasMes = BigDecimal.ZERO; // Placeholder - usar serviço real se disponível
                } catch (Exception ignored) {}
            }
            set(lblDespesasMes, "€ " + formatarValor(despesasMes));

            // Lucro estimado
            BigDecimal lucro = receitasMes.subtract(despesasMes);
            set(lblLucroEstimado, "€ " + formatarValor(lucro));

        } catch (Exception e) {
            System.err.println("[DASHBOARD-ADMIN] Erro ao carregar indicadores: " + e.getMessage());
        }
    }

    private void carregarAlertas() {
        if (containerAlertas == null) return;
        containerAlertas.getChildren().clear();

        try {
            // Alertas de stock
            long alertasStock = materialService.listarTodos().stream()
                    .filter(m -> m.getQuantidadeAtual() != null
                            && m.getQuantidadeMinima() != null
                            && m.getQuantidadeAtual() <= m.getQuantidadeMinima())
                    .count();

            // Pedidos pendentes
            long pedidosPendentes = pedidoCompraService.listarTodos() != null
                    ? pedidoCompraService.listarTodos().size() : 0;

            set(lblAlertasPendentes, (alertasStock + pedidosPendentes) + " pendentes");

            if (alertasStock > 0) {
                Label alerta = new Label("⚠ " + alertasStock + " materiais com stock abaixo do minimo");
                alerta.getStyleClass().add("alerta-titulo");
                containerAlertas.getChildren().add(alerta);
            }
            if (pedidosPendentes > 0) {
                Label alerta = new Label("📦 " + pedidosPendentes + " pedidos de compra pendentes");
                alerta.getStyleClass().add("alerta-titulo");
                containerAlertas.getChildren().add(alerta);
            }
            if (alertasStock == 0 && pedidosPendentes == 0) {
                Label ok = new Label("Nenhum alerta pendente. Tudo em ordem.");
                ok.getStyleClass().add("section-caption");
                containerAlertas.getChildren().add(ok);
            }

        } catch (Exception e) {
            Label erro = new Label("Nao foi possivel carregar alertas.");
            erro.getStyleClass().add("section-caption");
            containerAlertas.getChildren().add(erro);
        }
    }

    private void carregarMateriaisCriticos() {
        if (containerMateriaisCriticos == null) return;
        containerMateriaisCriticos.getChildren().clear();

        try {
            List<Material> criticos = materialService.listarTodos().stream()
                    .filter(m -> m.getQuantidadeAtual() != null
                            && m.getQuantidadeMinima() != null
                            && m.getQuantidadeAtual() <= m.getQuantidadeMinima())
                    .limit(5)
                    .toList();

            if (criticos.isEmpty()) {
                Label ok = new Label("Nenhum material critico. Stock normal.");
                ok.getStyleClass().add("section-caption");
                containerMateriaisCriticos.getChildren().add(ok);
                return;
            }

            for (Material m : criticos) {
                VBox card = new VBox(4);
                boolean zerado = m.getQuantidadeAtual() == 0;
                card.getStyleClass().add(zerado ? "alerta-card-critico" : "alerta-card");

                Label nome = new Label(m.getNome() != null ? m.getNome() : "Material");
                nome.getStyleClass().add("alerta-titulo");

                String info = "Stock: " + m.getQuantidadeAtual() + " / Min: " + m.getQuantidadeMinima()
                        + " " + (m.getUnidadeMedida() != null ? m.getUnidadeMedida() : "");
                Label desc = new Label(zerado ? "⚠ CRITICO — " + info : info);
                desc.getStyleClass().add("alerta-descricao");

                card.getChildren().addAll(nome, desc);
                containerMateriaisCriticos.getChildren().add(card);
            }
        } catch (Exception e) {
            Label erro = new Label("Nao foi possivel carregar materiais.");
            erro.getStyleClass().add("section-caption");
            containerMateriaisCriticos.getChildren().add(erro);
        }
    }

    // ─── Atalhos ─────────────────────────────────────────────────────────────────

    @FXML private void atalhoUtilizadores() { abrirUtilizadores(); }
    @FXML private void atalhoProcedimentos() { abrirProcedimentos(); }
    @FXML private void atalhoFinanceiro() { abrirFinanceiro(); }
    @FXML private void atalhoAuditoria() { abrirAuditoria(); }

    // ─── Utilitários ─────────────────────────────────────────────────────────────

    private void set(Label label, String valor) {
        if (label != null) label.setText(valor);
    }

    private String formatarValor(BigDecimal valor) {
        if (valor == null) return "0,00";
        return String.format("%.2f", valor.doubleValue()).replace(".", ",");
    }
}
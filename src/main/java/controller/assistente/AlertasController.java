package controller.assistente;

import dal.MaterialRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AlertasController extends BaseAssistenteController {

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private Label  lblTotalAlertas;
    @FXML private Label  lblStockBaixo;
    @FXML private Label  lblStockCritico;
    @FXML private Button btnTodos;
    @FXML private Button btnBaixo;
    @FXML private Button btnCritico;
    @FXML private VBox   containerAlertas;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private MaterialRepository materialRepository;

    /** null = todos, "baixo", "critico" */
    private String filtroAtual = null;
    private List<Material> todosOsAlertas;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        atualizarEstilosChip(btnTodos);
        carregarAlertas();
    }

    // ─── Filtros ──────────────────────────────────────────────────────────────

    @FXML private void filtrarTodos()   { filtroAtual = null;      atualizarEstilosChip(btnTodos);   renderizarAlertas(); }
    @FXML private void filtrarBaixo()   { filtroAtual = "baixo";   atualizarEstilosChip(btnBaixo);   renderizarAlertas(); }
    @FXML private void filtrarCritico() { filtroAtual = "critico"; atualizarEstilosChip(btnCritico); renderizarAlertas(); }

    @FXML
    private void atualizarAlertas() {
        carregarAlertas();
    }

    private void atualizarEstilosChip(Button ativo) {
        List<Button> chips = List.of(btnTodos, btnBaixo, btnCritico);
        for (Button b : chips) {
            b.getStyleClass().remove("filter-chip-active");
            if (!b.getStyleClass().contains("filter-chip")) b.getStyleClass().add("filter-chip");
        }
        if (!ativo.getStyleClass().contains("filter-chip-active")) ativo.getStyleClass().add("filter-chip-active");
    }

    // ─── Carregamento ─────────────────────────────────────────────────────────

    private void carregarAlertas() {
        try {
            // Usa o repositório directamente para obter apenas materiais abaixo do mínimo
            // com JOIN FETCH do fornecedor (evita LazyInitializationException)
            todosOsAlertas = materialRepository.findAbaixoStockMinimo().stream()
                    .sorted(Comparator.comparingInt(m ->
                            m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0))
                    .collect(Collectors.toList());

            long baixo   = todosOsAlertas.stream()
                    .filter(m -> m.getQuantidadeAtual() != null && m.getQuantidadeAtual() > 0)
                    .count();
            long critico = todosOsAlertas.stream()
                    .filter(m -> m.getQuantidadeAtual() == null || m.getQuantidadeAtual() == 0)
                    .count();

            if (lblTotalAlertas != null)  lblTotalAlertas.setText(String.valueOf(todosOsAlertas.size()));
            if (lblStockBaixo != null)    lblStockBaixo.setText(String.valueOf(baixo));
            if (lblStockCritico != null)  lblStockCritico.setText(String.valueOf(critico));

            renderizarAlertas();

        } catch (Exception e) {
            if (containerAlertas != null) {
                containerAlertas.getChildren().clear();
                Label erro = new Label("Não foi possível carregar os alertas: " + e.getMessage());
                erro.getStyleClass().add("section-caption");
                containerAlertas.getChildren().add(erro);
            }
        }
    }

    private void renderizarAlertas() {
        if (containerAlertas == null) return;
        containerAlertas.getChildren().clear();

        if (todosOsAlertas == null) return;

        List<Material> filtrados = switch (filtroAtual == null ? "" : filtroAtual) {
            case "baixo"   -> todosOsAlertas.stream()
                    .filter(m -> m.getQuantidadeAtual() != null && m.getQuantidadeAtual() > 0)
                    .toList();
            case "critico" -> todosOsAlertas.stream()
                    .filter(m -> m.getQuantidadeAtual() == null || m.getQuantidadeAtual() == 0)
                    .toList();
            default        -> todosOsAlertas;
        };

        if (filtrados.isEmpty()) {
            Label vazio = new Label("Nenhum alerta para o filtro selecionado.");
            vazio.getStyleClass().add("section-caption");
            containerAlertas.getChildren().add(vazio);
            return;
        }

        for (Material m : filtrados) {
            containerAlertas.getChildren().add(criarCardAlerta(m));
        }
    }

    // ─── Card builder ─────────────────────────────────────────────────────────

    private VBox criarCardAlerta(Material m) {
        boolean critico = m.getQuantidadeAtual() == null || m.getQuantidadeAtual() == 0;

        VBox card = new VBox(6);
        card.getStyleClass().add(critico ? "alerta-card-critico" : "alerta-card");

        Label nome = new Label(m.getNome() != null ? m.getNome() : "Material sem nome");
        nome.getStyleClass().add("alerta-titulo");

        int qty = m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0;
        int min = m.getQuantidadeMinima() != null ? m.getQuantidadeMinima() : 0;
        String unidade = m.getUnidadeMedida() != null ? " " + m.getUnidadeMedida() : "";

        String textoEstado = critico
                ? "⚠ Stock zerado — Quantidade: 0" + unidade + " (mín. " + min + unidade + ")"
                : "↓ Stock baixo — Quantidade: " + qty + unidade + " (mín. " + min + unidade + ")";

        Label descricao = new Label(textoEstado);
        descricao.getStyleClass().add("alerta-descricao");
        descricao.setWrapText(true);

        Label acao = new Label(critico
                ? "Ação sugerida: criar pedido de compra urgente."
                : "Ação sugerida: verificar stock e ponderar novo pedido.");
        acao.getStyleClass().add("info-meta");
        acao.setWrapText(true);

        try {
            if (m.getIdFornecedor() != null && m.getIdFornecedor().getNome() != null) {
                Label fornecedor = new Label("Fornecedor: " + m.getIdFornecedor().getNome());
                fornecedor.getStyleClass().add("info-meta");
                card.getChildren().addAll(nome, descricao, acao, fornecedor);
            } else {
                card.getChildren().addAll(nome, descricao, acao);
            }
        } catch (Exception e) {
            card.getChildren().addAll(nome, descricao, acao);
        }

        return card;
    }
}

package controller.assistente;

import bll.MaterialService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import model.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MateriaisController extends BaseAssistenteController {

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private TextField                    txtPesquisa;
    @FXML private Button                       btnTodos;
    @FXML private Button                       btnBaixo;
    @FXML private Button                       btnCritico;
    @FXML private Label                        lblTotalMateriais;
    @FXML private TableView<Material>          tblMateriais;
    @FXML private TableColumn<Material, String> colNome;
    @FXML private TableColumn<Material, String> colCategoria;
    @FXML private TableColumn<Material, String> colQuantidade;
    @FXML private TableColumn<Material, String> colMinimo;
    @FXML private TableColumn<Material, String> colUnidade;
    @FXML private TableColumn<Material, String> colEstado;
    @FXML private TableColumn<Material, String> colAcoes;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private MaterialService materialService;

    private ObservableList<Material>  todosOsMateriais;
    private FilteredList<Material>    materiaisFiltrados;

    /** null = todos, "baixo" = stock ≤ mínimo, "critico" = stock == 0 */
    private String filtroStock = null;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        configurarTabela();
        atualizarEstilosChip(btnTodos);
        carregarMateriais();

        if (txtPesquisa != null) {
            txtPesquisa.textProperty().addListener((obs, o, n) -> aplicarFiltros());
        }
    }

    private void configurarTabela() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNome() != null ? c.getValue().getNome() : "-"));

        colCategoria.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDescricao() != null ? c.getValue().getDescricao() : "-"));

        colQuantidade.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getQuantidadeAtual() != null ? String.valueOf(c.getValue().getQuantidadeAtual()) : "0"));

        colMinimo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getQuantidadeMinima() != null ? String.valueOf(c.getValue().getQuantidadeMinima()) : "0"));

        colUnidade.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getUnidadeMedida() != null ? c.getValue().getUnidadeMedida() : "-"));

        colEstado.setCellValueFactory(c -> new SimpleStringProperty(""));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Material m = getTableRow().getItem();
                boolean critico = m.getQuantidadeAtual() != null && m.getQuantidadeAtual() == 0;
                boolean baixo   = !critico && m.getQuantidadeAtual() != null
                        && m.getQuantidadeMinima() != null
                        && m.getQuantidadeAtual() <= m.getQuantidadeMinima();

                Label badge = new Label(critico ? "Critico" : baixo ? "Baixo" : "OK");
                badge.getStyleClass().add(critico ? "badge-critico" : baixo ? "badge-baixo" : "badge-ok");
                setGraphic(badge);
                setText(null);
            }
        });

        colAcoes.setCellValueFactory(c -> new SimpleStringProperty(""));
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnMovimentacao = new Button("Movimentação");
            { btnMovimentacao.getStyleClass().add("table-action-button"); }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                btnMovimentacao.setOnAction(e -> {
                    try { abrirMovimentacoes(); } catch (Exception ex) { ex.printStackTrace(); }
                });
                setGraphic(btnMovimentacao);
                setText(null);
            }
        });
    }

    // ─── Filtros ──────────────────────────────────────────────────────────────

    @FXML private void filtrarTodos()   { filtroStock = null;      atualizarEstilosChip(btnTodos);   aplicarFiltros(); }
    @FXML private void filtrarBaixo()   { filtroStock = "baixo";   atualizarEstilosChip(btnBaixo);   aplicarFiltros(); }
    @FXML private void filtrarCritico() { filtroStock = "critico"; atualizarEstilosChip(btnCritico); aplicarFiltros(); }

    private void atualizarEstilosChip(Button ativo) {
        List<Button> chips = List.of(btnTodos, btnBaixo, btnCritico);
        for (Button b : chips) {
            b.getStyleClass().remove("filter-chip-active");
            if (!b.getStyleClass().contains("filter-chip")) b.getStyleClass().add("filter-chip");
        }
        if (!ativo.getStyleClass().contains("filter-chip-active")) ativo.getStyleClass().add("filter-chip-active");
    }

    // ─── Carregamento ─────────────────────────────────────────────────────────

    private void carregarMateriais() {
        try {
            todosOsMateriais = FXCollections.observableArrayList(materialService.listarTodos());
            materiaisFiltrados = new FilteredList<>(todosOsMateriais, m -> true);
            tblMateriais.setItems(materiaisFiltrados);
            aplicarFiltros();
        } catch (Exception e) {
            tblMateriais.setPlaceholder(new Label("Não foi possível carregar os materiais."));
        }
    }

    private void aplicarFiltros() {
        if (materiaisFiltrados == null) return;
        String pesquisa = txtPesquisa != null ? txtPesquisa.getText().trim().toLowerCase() : "";

        materiaisFiltrados.setPredicate(m -> {
            // filtro de pesquisa
            boolean correspondePesquisa = pesquisa.isBlank()
                    || (m.getNome() != null && m.getNome().toLowerCase().contains(pesquisa))
                    || (m.getDescricao() != null && m.getDescricao().toLowerCase().contains(pesquisa));

            // filtro de stock
            boolean correspondeStock = switch (filtroStock == null ? "" : filtroStock) {
                case "critico" -> m.getQuantidadeAtual() != null && m.getQuantidadeAtual() == 0;
                case "baixo"   -> m.getQuantidadeAtual() != null && m.getQuantidadeMinima() != null
                        && m.getQuantidadeAtual() <= m.getQuantidadeMinima();
                default        -> true;
            };

            return correspondePesquisa && correspondeStock;
        });

        int total = materiaisFiltrados.size();
        if (lblTotalMateriais != null) lblTotalMateriais.setText(total + " materiais");
    }
}

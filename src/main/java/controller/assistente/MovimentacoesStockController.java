package controller.assistente;

import bll.MovimentacaoEstoqueService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.MovimentacaoEstoque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MovimentacoesStockController extends BaseAssistenteController {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private TextField txtPesquisa;
    @FXML private Button    btnTodas;
    @FXML private Button    btnEntrada;
    @FXML private Button    btnSaida;
    @FXML private Button    btnAjuste;
    @FXML private Label     lblTotalMovimentacoes;
    @FXML private TableView<MovimentacaoEstoque>          tblMovimentacoes;
    @FXML private TableColumn<MovimentacaoEstoque, String> colData;
    @FXML private TableColumn<MovimentacaoEstoque, String> colMaterial;
    @FXML private TableColumn<MovimentacaoEstoque, String> colTipo;
    @FXML private TableColumn<MovimentacaoEstoque, String> colQuantidade;
    @FXML private TableColumn<MovimentacaoEstoque, String> colResponsavel;
    @FXML private TableColumn<MovimentacaoEstoque, String> colObservacoes;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private MovimentacaoEstoqueService movimentacaoService;

    private ObservableList<MovimentacaoEstoque> todasMovimentacoes;
    private FilteredList<MovimentacaoEstoque>   movimentacoesFiltradas;

    /** null = todas, "entrada" = quantidade>0, "saida" = quantidade<0, "ajuste" = motivo=AJUSTE */
    private String filtroTipo = null;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        configurarTabela();
        atualizarEstilosChip(btnTodas);
        carregarMovimentacoes();

        if (txtPesquisa != null) {
            txtPesquisa.textProperty().addListener((obs, o, n) -> aplicarFiltros());
        }
    }

    private void configurarTabela() {
        colData.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getData() != null ? c.getValue().getData().format(DATA_FMT) : "-"));

        colMaterial.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIdMaterial() != null && c.getValue().getIdMaterial().getNome() != null
                        ? c.getValue().getIdMaterial().getNome() : "-"));

        colTipo.setCellValueFactory(c -> {
            Integer qty = c.getValue().getQuantidade();
            String motivo = c.getValue().getMotivo();
            String tipo;
            if ("AJUSTE".equalsIgnoreCase(motivo)) {
                tipo = "Ajuste";
            } else if (qty != null && qty > 0) {
                tipo = "Entrada";
            } else {
                tipo = "Saída";
            }
            return new SimpleStringProperty(tipo);
        });

        colQuantidade.setCellValueFactory(c -> {
            Integer qty = c.getValue().getQuantidade();
            return new SimpleStringProperty(qty != null ? (qty > 0 ? "+" + qty : String.valueOf(qty)) : "0");
        });

        colResponsavel.setCellValueFactory(c -> {
            if (c.getValue().getIdUtilizador() != null
                    && c.getValue().getIdUtilizador().getUtilizador() != null) {
                var u = c.getValue().getIdUtilizador().getUtilizador();
                String nome = ((u.getPrimeiroNome() != null ? u.getPrimeiroNome() : "")
                        + " " + (u.getUltimoNome() != null ? u.getUltimoNome() : "")).trim();
                return new SimpleStringProperty(nome.isBlank() ? "-" : nome);
            }
            return new SimpleStringProperty("-");
        });

        colObservacoes.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getObservacao() != null ? c.getValue().getObservacao() : "-"));
    }

    // ─── Filtros ──────────────────────────────────────────────────────────────

    @FXML private void filtrarTodas()  { filtroTipo = null;      atualizarEstilosChip(btnTodas);   aplicarFiltros(); }
    @FXML private void filtrarEntrada(){ filtroTipo = "entrada"; atualizarEstilosChip(btnEntrada); aplicarFiltros(); }
    @FXML private void filtrarSaida()  { filtroTipo = "saida";   atualizarEstilosChip(btnSaida);   aplicarFiltros(); }
    @FXML private void filtrarAjuste() { filtroTipo = "ajuste";  atualizarEstilosChip(btnAjuste);  aplicarFiltros(); }

    private void atualizarEstilosChip(Button ativo) {
        List<Button> chips = List.of(btnTodas, btnEntrada, btnSaida, btnAjuste);
        for (Button b : chips) {
            b.getStyleClass().remove("filter-chip-active");
            if (!b.getStyleClass().contains("filter-chip")) b.getStyleClass().add("filter-chip");
        }
        if (!ativo.getStyleClass().contains("filter-chip-active")) ativo.getStyleClass().add("filter-chip-active");
    }

    @FXML
    private void abrirNovaMovimentacao() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Nova Movimentação");
        info.setHeaderText(null);
        info.setContentText("Funcionalidade de registo de movimentação será implementada em breve.");
        info.showAndWait();
    }

    // ─── Carregamento ─────────────────────────────────────────────────────────

    private void carregarMovimentacoes() {
        try {
            todasMovimentacoes = FXCollections.observableArrayList(movimentacaoService.listarTodos());
            movimentacoesFiltradas = new FilteredList<>(todasMovimentacoes, m -> true);
            tblMovimentacoes.setItems(movimentacoesFiltradas);
            aplicarFiltros();
        } catch (Exception e) {
            tblMovimentacoes.setPlaceholder(new Label("Não foi possível carregar as movimentações."));
        }
    }

    private void aplicarFiltros() {
        if (movimentacoesFiltradas == null) return;
        String pesquisa = txtPesquisa != null ? txtPesquisa.getText().trim().toLowerCase() : "";

        movimentacoesFiltradas.setPredicate(m -> {
            // filtro de pesquisa por nome do material
            boolean correspondePesquisa = pesquisa.isBlank()
                    || (m.getIdMaterial() != null && m.getIdMaterial().getNome() != null
                    && m.getIdMaterial().getNome().toLowerCase().contains(pesquisa));

            // filtro de tipo
            boolean correspondeTipo = switch (filtroTipo == null ? "" : filtroTipo) {
                case "entrada" -> m.getQuantidade() != null && m.getQuantidade() > 0
                        && !"AJUSTE".equalsIgnoreCase(m.getMotivo());
                case "saida"   -> m.getQuantidade() != null && m.getQuantidade() < 0;
                case "ajuste"  -> "AJUSTE".equalsIgnoreCase(m.getMotivo());
                default        -> true;
            };

            return correspondePesquisa && correspondeTipo;
        });

        int total = movimentacoesFiltradas.size();
        if (lblTotalMovimentacoes != null) lblTotalMovimentacoes.setText(total + " registos");
    }
}

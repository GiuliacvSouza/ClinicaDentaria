package controller.assistente;

import app.MainFX;
import bll.MovimentacaoEstoqueService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.MovimentacaoEstoque;
import model.enums.TipoMovimentacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MovimentacoesStockController extends BaseAssistenteController {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private TextField  txtPesquisa;
    @FXML private DatePicker dpDataInicio;
    @FXML private DatePicker dpDataFim;
    @FXML private Button     btnTodas;
    @FXML private Button     btnEntrada;
    @FXML private Button     btnSaida;
    @FXML private Label      lblTotalMovimentacoes;

    @FXML private TableView<MovimentacaoEstoque>           tblMovimentacoes;
    @FXML private TableColumn<MovimentacaoEstoque, String> colData;
    @FXML private TableColumn<MovimentacaoEstoque, String> colMaterial;
    @FXML private TableColumn<MovimentacaoEstoque, String> colTipo;
    @FXML private TableColumn<MovimentacaoEstoque, String> colQuantidade;
    @FXML private TableColumn<MovimentacaoEstoque, String> colMotivo;
    @FXML private TableColumn<MovimentacaoEstoque, String> colResponsavel;
    @FXML private TableColumn<MovimentacaoEstoque, String> colObservacoes;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private MovimentacaoEstoqueService movimentacaoService;

    private ObservableList<MovimentacaoEstoque> todas;
    private FilteredList<MovimentacaoEstoque>   filtradas;

    /** null = todas, TipoMovimentacao.ENTRADA, TipoMovimentacao.SAIDA */
    private TipoMovimentacao filtroTipo = null;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        configurarDatePickers();
        configurarTabela();
        atualizarEstilosChip(btnTodas);
        carregarMovimentacoes();

        if (txtPesquisa != null)
            txtPesquisa.textProperty().addListener((obs, o, n) -> aplicarFiltros());
    }

    // ─── DatePickers ──────────────────────────────────────────────────────────

    private void configurarDatePickers() {
        StringConverter<LocalDate> converter = new StringConverter<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override public String toString(LocalDate value) {
                return value != null ? value.format(fmt) : "";
            }
            @Override public LocalDate fromString(String value) {
                return value == null || value.isBlank() ? null : LocalDate.parse(value.trim(), fmt);
            }
        };
        if (dpDataInicio != null) dpDataInicio.setConverter(converter);
        if (dpDataFim    != null) dpDataFim.setConverter(converter);
    }

    // ─── Tabela ───────────────────────────────────────────────────────────────

    private void configurarTabela() {
        colData.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getData() != null ? c.getValue().getData().format(DATA_FMT) : "-"));

        colMaterial.setCellValueFactory(c -> {
            var m = c.getValue().getIdMaterial();
            return new SimpleStringProperty(m != null && m.getNome() != null ? m.getNome() : "-");
        });

        // Tipo — badge colorido a partir de TipoMovimentacao
        colTipo.setCellValueFactory(c -> {
            TipoMovimentacao tipo = MovimentacaoEstoqueService.extrairTipoEnum(c.getValue());
            return new SimpleStringProperty(MovimentacaoEstoqueService.textoTipo(tipo));
        });
        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); setText(null); return;
                }
                TipoMovimentacao tipo = MovimentacaoEstoqueService
                        .extrairTipoEnum((MovimentacaoEstoque) getTableRow().getItem());
                Label badge = new Label(MovimentacaoEstoqueService.textoTipo(tipo));
                badge.getStyleClass().add(MovimentacaoEstoqueService.classeTipoBadge(tipo));
                setGraphic(badge);
                setText(null);
            }
        });

        colQuantidade.setCellValueFactory(c -> {
            Integer qty = c.getValue().getQuantidade();
            if (qty == null) return new SimpleStringProperty("0");
            String unidade = c.getValue().getIdMaterial() != null
                    && c.getValue().getIdMaterial().getUnidadeMedida() != null
                    ? " " + c.getValue().getIdMaterial().getUnidadeMedida() : "";
            // Mostrar sempre positivo na tabela (o tipo já indica entrada/saída)
            return new SimpleStringProperty(Math.abs(qty) + unidade);
        });

        colMotivo.setCellValueFactory(c -> {
            String motivo = c.getValue().getMotivo();
            return new SimpleStringProperty(
                    motivo != null && !motivo.isBlank() ? motivo : "-");
        });

        colResponsavel.setCellValueFactory(c -> {
            var a = c.getValue().getIdUtilizador();
            if (a == null || a.getUtilizador() == null) return new SimpleStringProperty("-");
            var u = a.getUtilizador();
            String nome = ((u.getPrimeiroNome() != null ? u.getPrimeiroNome() : "")
                    + " " + (u.getUltimoNome() != null ? u.getUltimoNome() : "")).trim();
            return new SimpleStringProperty(nome.isBlank() ? "-" : nome);
        });

        colObservacoes.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getObservacao() != null && !c.getValue().getObservacao().isBlank()
                        ? c.getValue().getObservacao() : "-"));
    }

    // ─── Filtros ──────────────────────────────────────────────────────────────

    @FXML private void filtrarTodas()  { filtroTipo = null;                    atualizarEstilosChip(btnTodas);   aplicarFiltros(); }
    @FXML private void filtrarEntrada(){ filtroTipo = TipoMovimentacao.ENTRADA; atualizarEstilosChip(btnEntrada); aplicarFiltros(); }
    @FXML private void filtrarSaida()  { filtroTipo = TipoMovimentacao.SAIDA;   atualizarEstilosChip(btnSaida);   aplicarFiltros(); }

    @FXML
    private void filtrarPorData() {
        carregarMovimentacoes();
    }

    @FXML
    private void limparFiltros() {
        if (txtPesquisa  != null) txtPesquisa.clear();
        if (dpDataInicio != null) dpDataInicio.setValue(null);
        if (dpDataFim    != null) dpDataFim.setValue(null);
        filtroTipo = null;
        atualizarEstilosChip(btnTodas);
        carregarMovimentacoes();
    }

    private void atualizarEstilosChip(Button ativo) {
        for (Button b : List.of(btnTodas, btnEntrada, btnSaida)) {
            b.getStyleClass().remove("filter-chip-active");
            if (!b.getStyleClass().contains("filter-chip")) b.getStyleClass().add("filter-chip");
        }
        if (!ativo.getStyleClass().contains("filter-chip-active"))
            ativo.getStyleClass().add("filter-chip-active");
    }

    // ─── Carregamento ─────────────────────────────────────────────────────────

    private void carregarMovimentacoes() {
        try {
            List<MovimentacaoEstoque> lista;

            if (dpDataInicio != null && dpDataFim != null
                    && dpDataInicio.getValue() != null && dpDataFim.getValue() != null) {
                lista = movimentacaoService.listarPorPeriodo(
                        dpDataInicio.getValue(), dpDataFim.getValue());
            } else {
                lista = movimentacaoService.listarTodos();
            }

            todas    = FXCollections.observableArrayList(lista);
            filtradas = new FilteredList<>(todas, m -> true);
            tblMovimentacoes.setItems(filtradas);
            // Repor placeholder original (pode ter sido substituído por erro anterior)
            VBox placeholderVazio = new VBox(8);
            placeholderVazio.setAlignment(Pos.CENTER);
            placeholderVazio.setStyle("-fx-padding: 48 0;");
            Label lblVazioTitulo = new Label("Nenhuma movimentação registada.");
            lblVazioTitulo.getStyleClass().add("empty-table-title");
            Label lblVazioSub = new Label("Registe uma nova entrada ou saída de stock para começar.");
            lblVazioSub.getStyleClass().add("empty-table-subtitle");
            placeholderVazio.getChildren().addAll(lblVazioTitulo, lblVazioSub);
            tblMovimentacoes.setPlaceholder(placeholderVazio);
            aplicarFiltros();

        } catch (Exception e) {
            Label lblErro = new Label("Não foi possível carregar as movimentações.");
            lblErro.getStyleClass().add("empty-table-title");
            tblMovimentacoes.setPlaceholder(lblErro);
        }
    }

    private void aplicarFiltros() {
        if (filtradas == null) return;
        String pesquisa = txtPesquisa != null ? txtPesquisa.getText().trim().toLowerCase() : "";

        filtradas.setPredicate(m -> {
            boolean correspondePesquisa = pesquisa.isBlank()
                    || (m.getIdMaterial() != null && m.getIdMaterial().getNome() != null
                        && m.getIdMaterial().getNome().toLowerCase().contains(pesquisa));

            boolean correspondeTipo = filtroTipo == null
                    || filtroTipo == MovimentacaoEstoqueService.extrairTipoEnum(m);

            return correspondePesquisa && correspondeTipo;
        });

        int total = filtradas.size();
        if (lblTotalMovimentacoes != null)
            lblTotalMovimentacoes.setText(total + (total == 1 ? " registo" : " registos"));
    }

    // ─── Abrir modal ──────────────────────────────────────────────────────────

    @FXML
    private void abrirNovaMovimentacao() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/assistente/nova-movimentacao.fxml"));
            if (MainFX.getSpringContext() != null)
                loader.setControllerFactory(MainFX.getSpringContext()::getBean);

            Parent root = loader.load();
            NovaMovimentacaoController ctrl = loader.getController();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tblMovimentacoes.getScene().getWindow());
            modal.setResizable(false);
            modal.setTitle("Nova Movimentação de Stock");

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/assistente-style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            ctrl.setStage(modal);
            modal.setScene(scene);
            modal.showAndWait();

            if (ctrl.isSaved()) {
                carregarMovimentacoes();
            }

        } catch (Exception ex) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            a.setTitle("Erro");
            a.setHeaderText(null);
            a.setContentText("Não foi possível abrir o formulário: " + ex.getMessage());
            a.showAndWait();
        }
    }
}

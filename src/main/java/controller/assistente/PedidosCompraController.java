package controller.assistente;

import bll.PedidoCompraService;
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
import model.PedidoCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PedidosCompraController extends BaseAssistenteController {

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private TextField  txtPesquisa;
    @FXML private Button     btnTodos;
    @FXML private Button     btnPendente;
    @FXML private Button     btnEnviado;
    @FXML private Button     btnRecebido;
    @FXML private Button     btnCancelado;
    @FXML private Label      lblTotalPedidos;
    @FXML private TableView<PedidoCompra>          tblPedidos;
    @FXML private TableColumn<PedidoCompra, String> colNumero;
    @FXML private TableColumn<PedidoCompra, String> colFornecedor;
    @FXML private TableColumn<PedidoCompra, String> colDataPedido;
    @FXML private TableColumn<PedidoCompra, String> colDataEntrega;
    @FXML private TableColumn<PedidoCompra, String> colEstado;
    @FXML private TableColumn<PedidoCompra, String> colValorTotal;
    @FXML private TableColumn<PedidoCompra, String> colAcoes;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private PedidoCompraService pedidoCompraService;

    private ObservableList<PedidoCompra> todosPedidos;
    private FilteredList<PedidoCompra>   pedidosFiltrados;
    private String filtroEstado = null;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        configurarTabela();
        atualizarEstilosChip(btnTodos);
        carregarPedidos();

        if (txtPesquisa != null) {
            txtPesquisa.textProperty().addListener((obs, o, n) -> aplicarFiltros());
        }
    }

    private void configurarTabela() {
        colNumero.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getId() != null ? "#" + c.getValue().getId() : "-"));

        colFornecedor.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIdFornecedor() != null && c.getValue().getIdFornecedor().getNome() != null
                        ? c.getValue().getIdFornecedor().getNome() : "-"));

        // PedidoCompra ainda não tem campos de data/estado/valor no modelo — placeholder
        colDataPedido.setCellValueFactory(c -> new SimpleStringProperty("-"));
        colDataEntrega.setCellValueFactory(c -> new SimpleStringProperty("-"));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty("Pendente"));
        colValorTotal.setCellValueFactory(c -> new SimpleStringProperty("-"));

        colAcoes.setCellValueFactory(c -> new SimpleStringProperty(""));
        colAcoes.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final Button btnVer = new Button("Ver pormenores");
            { btnVer.getStyleClass().add("table-action-button"); }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                btnVer.setOnAction(e -> mostrarDetalhes(getIndex()));
                setGraphic(btnVer);
                setText(null);
            }
        });
    }

    // ─── Filtros ──────────────────────────────────────────────────────────────

    @FXML private void filtrarTodos()     { filtroEstado = null;        atualizarEstilosChip(btnTodos);     aplicarFiltros(); }
    @FXML private void filtrarPendente()  { filtroEstado = "PENDENTE";  atualizarEstilosChip(btnPendente);  aplicarFiltros(); }
    @FXML private void filtrarEnviado()   { filtroEstado = "ENVIADO";   atualizarEstilosChip(btnEnviado);   aplicarFiltros(); }
    @FXML private void filtrarRecebido()  { filtroEstado = "RECEBIDO";  atualizarEstilosChip(btnRecebido);  aplicarFiltros(); }
    @FXML private void filtrarCancelado() { filtroEstado = "CANCELADO"; atualizarEstilosChip(btnCancelado); aplicarFiltros(); }

    private void atualizarEstilosChip(Button ativo) {
        List<Button> chips = List.of(btnTodos, btnPendente, btnEnviado, btnRecebido, btnCancelado);
        for (Button b : chips) {
            b.getStyleClass().remove("filter-chip-active");
            if (!b.getStyleClass().contains("filter-chip")) b.getStyleClass().add("filter-chip");
        }
        if (!ativo.getStyleClass().contains("filter-chip-active")) ativo.getStyleClass().add("filter-chip-active");
    }

    @FXML
    private void criarNovoPedido() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Novo Pedido");
        info.setHeaderText(null);
        info.setContentText("Formulário de criação de pedido de compra será implementado em breve.");
        info.showAndWait();
    }

    private void mostrarDetalhes(int index) {
        if (index < 0 || index >= tblPedidos.getItems().size()) return;
        PedidoCompra p = tblPedidos.getItems().get(index);
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Pedido #" + p.getId());
        info.setHeaderText(null);
        info.setContentText("Fornecedor: " +
                (p.getIdFornecedor() != null ? p.getIdFornecedor().getNome() : "-"));
        info.showAndWait();
    }

    // ─── Carregamento ─────────────────────────────────────────────────────────

    private void carregarPedidos() {
        try {
            todosPedidos = FXCollections.observableArrayList(pedidoCompraService.listarTodos());
            pedidosFiltrados = new FilteredList<>(todosPedidos, p -> true);
            tblPedidos.setItems(pedidosFiltrados);
            aplicarFiltros();
        } catch (Exception e) {
            tblPedidos.setPlaceholder(new Label("Não foi possível carregar os pedidos."));
        }
    }

    private void aplicarFiltros() {
        if (pedidosFiltrados == null) return;
        String pesquisa = txtPesquisa != null ? txtPesquisa.getText().trim().toLowerCase() : "";

        pedidosFiltrados.setPredicate(p -> {
            boolean correspondePesquisa = pesquisa.isBlank()
                    || (p.getIdFornecedor() != null && p.getIdFornecedor().getNome() != null
                    && p.getIdFornecedor().getNome().toLowerCase().contains(pesquisa));

            // estado ainda não está no modelo — para já, todos passam
            boolean correspondeEstado = filtroEstado == null;

            return correspondePesquisa && correspondeEstado;
        });

        int total = pedidosFiltrados.size();
        if (lblTotalPedidos != null) lblTotalPedidos.setText(total + " pedidos");
    }
}

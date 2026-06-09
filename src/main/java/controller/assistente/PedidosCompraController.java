package controller.assistente;

import app.SceneManager;
import bll.PedidoCompraService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import model.ItemPedido;
import model.PedidoCompra;
import model.enums.EstadoPedidoCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PedidosCompraController extends BaseAssistenteController {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private TextField  txtPesquisa;
    @FXML private Button     btnTodos;
    @FXML private Button     btnPendente;
    @FXML private Button     btnEnviado;
    @FXML private Button     btnRecebido;
    @FXML private Button     btnCancelado;
    @FXML private Label      lblTotalPedidos;

    @FXML private TableView<PedidoCompra>           tblPedidos;
    @FXML private TableColumn<PedidoCompra, String> colNumero;
    @FXML private TableColumn<PedidoCompra, String> colFornecedor;
    @FXML private TableColumn<PedidoCompra, String> colDataPedido;
    @FXML private TableColumn<PedidoCompra, String> colAssistente;
    @FXML private TableColumn<PedidoCompra, String> colEstado;
    @FXML private TableColumn<PedidoCompra, String> colTotalItens;
    @FXML private TableColumn<PedidoCompra, String> colValorTotal;
    @FXML private TableColumn<PedidoCompra, String> colAcoes;

    // ─── Dependências ─────────────────────────────────────────────────────────

    @Autowired private PedidoCompraService pedidoCompraService;

    // ─── Estado ───────────────────────────────────────────────────────────────

    private ObservableList<PedidoCompra> todosPedidos;
    private FilteredList<PedidoCompra>   pedidosFiltrados;
    private EstadoPedidoCompra           filtroEstado = null;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        configurarTabela();
        atualizarEstilosChip(btnTodos);
        carregarPedidos();

        if (txtPesquisa != null)
            txtPesquisa.textProperty().addListener((obs, o, n) -> aplicarFiltros());
    }

    // ─── Tabela ───────────────────────────────────────────────────────────────

    private void configurarTabela() {
        // Alinhamento centrado/direito nas colunas adequadas
        colNumero.setStyle("-fx-alignment: CENTER;");
        colDataPedido.setStyle("-fx-alignment: CENTER;");
        colEstado.setStyle("-fx-alignment: CENTER;");
        colTotalItens.setStyle("-fx-alignment: CENTER;");
        colValorTotal.setStyle("-fx-alignment: CENTER-RIGHT;");

        colNumero.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getId() != null ? "#" + c.getValue().getId() : "-"));

        colFornecedor.setCellValueFactory(c -> {
            var f = c.getValue().getIdFornecedor();
            if (f == null) return new SimpleStringProperty("-");
            String nome = (f.getNome() != null ? f.getNome() : "")
                    + (f.getUltimoNome() != null && !f.getUltimoNome().isBlank()
                    ? " " + f.getUltimoNome() : "");
            return new SimpleStringProperty(nome.isBlank() ? "-" : nome.trim());
        });

        colDataPedido.setCellValueFactory(c -> {
            var d = c.getValue().getDataPedido();
            return new SimpleStringProperty(d != null ? d.format(DATA_FMT) : "-");
        });

        colAssistente.setCellValueFactory(c -> {
            var a = c.getValue().getIdAssistente();
            if (a == null || a.getUtilizador() == null) return new SimpleStringProperty("-");
            var u = a.getUtilizador();
            String nome = (u.getPrimeiroNome() != null ? u.getPrimeiroNome() : "")
                    + (u.getUltimoNome() != null ? " " + u.getUltimoNome() : "");
            return new SimpleStringProperty(nome.trim().isBlank() ? "-" : nome.trim());
        });

        // Estado — badge colorido
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(textoEstado(c.getValue().getEstado())));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                EstadoPedidoCompra estado = ((PedidoCompra) getTableRow().getItem()).getEstado();
                Label badge = new Label(textoEstado(estado));
                badge.getStyleClass().add(classeEstado(estado));
                setGraphic(badge);
                setAlignment(javafx.geometry.Pos.CENTER);
                setText(null);
            }
        });

        // Total de itens — usa a coleção já carregada via JOIN FETCH
        colTotalItens.setCellValueFactory(c -> {
            try {
                int n = c.getValue().getItens() != null ? c.getValue().getItens().size() : 0;
                return new SimpleStringProperty(String.valueOf(n));
            } catch (Exception e) {
                return new SimpleStringProperty("-");
            }
        });

        // Valor total — usa a coleção já carregada via JOIN FETCH
        colValorTotal.setCellValueFactory(c -> {
            try {
                List<ItemPedido> itens = c.getValue().getItens();
                if (itens == null) return new SimpleStringProperty("0,00 €");
                BigDecimal total = pedidoCompraService.calcularTotal(itens);
                return new SimpleStringProperty(String.format("%.2f €", total));
            } catch (Exception e) {
                return new SimpleStringProperty("-");
            }
        });

        // Ações — largura fixa com alinhamento consistente entre linhas
        colAcoes.setCellValueFactory(c -> null);
        colAcoes.setStyle("-fx-alignment: CENTER-LEFT;");
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer      = new Button("Ver");
            private final Button btnCancelar = new Button("Cancelar");
            private final Button btnReceber  = new Button("Recebido");
            private final HBox   box         = new HBox(8);
            private final Region spacer      = new Region();

            {
                btnVer.getStyleClass().add("table-action-button");
                btnVer.setMinWidth(80);
                btnVer.setPrefWidth(80);

                btnCancelar.getStyleClass().add("table-link-button");
                btnCancelar.setMinWidth(70);
                btnCancelar.setPrefWidth(70);

                btnReceber.getStyleClass().add("table-action-button");
                btnReceber.setMinWidth(90);
                btnReceber.setPrefWidth(90);

                // HBox com largura fixa para alinhamento uniforme entre todas as linhas
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                box.setMinWidth(270);
                box.setPrefWidth(270);
                box.setMaxWidth(270);

                // Spacer ocupa o espaço restante para manter os botões alinhados à esquerda
                HBox.setHgrow(spacer, Priority.ALWAYS);
                box.getChildren().addAll(btnVer, btnReceber, btnCancelar, spacer);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                PedidoCompra p = (PedidoCompra) getTableRow().getItem();
                EstadoPedidoCompra estado = p.getEstado();

                btnVer.setOnAction(e -> abrirDetalhesPedido(p));
                btnCancelar.setOnAction(e -> confirmarCancelar(p));
                btnReceber.setOnAction(e -> confirmarRecepcao(p));

                // Mostrar botões extra apenas para PENDENTE ou ENVIADO
                boolean acoesVisiveis = (estado == EstadoPedidoCompra.PENDENTE
                                      || estado == EstadoPedidoCompra.ENVIADO);
                btnReceber.setManaged(acoesVisiveis);
                btnReceber.setVisible(acoesVisiveis);
                btnCancelar.setManaged(acoesVisiveis);
                btnCancelar.setVisible(acoesVisiveis);

                setGraphic(box);
                setText(null);
            }
        });
    }

    // ─── Filtros ──────────────────────────────────────────────────────────────

    @FXML private void filtrarTodos()     { filtroEstado = null;                       atualizarEstilosChip(btnTodos);     aplicarFiltros(); }
    @FXML private void filtrarPendente()  { filtroEstado = EstadoPedidoCompra.PENDENTE;  atualizarEstilosChip(btnPendente);  aplicarFiltros(); }
    @FXML private void filtrarEnviado()   { filtroEstado = EstadoPedidoCompra.ENVIADO;   atualizarEstilosChip(btnEnviado);   aplicarFiltros(); }
    @FXML private void filtrarRecebido()  { filtroEstado = EstadoPedidoCompra.RECEBIDO;  atualizarEstilosChip(btnRecebido);  aplicarFiltros(); }
    @FXML private void filtrarCancelado() { filtroEstado = EstadoPedidoCompra.CANCELADO; atualizarEstilosChip(btnCancelado); aplicarFiltros(); }

    private void atualizarEstilosChip(Button ativo) {
        List<Button> chips = List.of(btnTodos, btnPendente, btnEnviado, btnRecebido, btnCancelado);
        for (Button b : chips) {
            b.getStyleClass().remove("filter-chip-active");
            if (!b.getStyleClass().contains("filter-chip")) b.getStyleClass().add("filter-chip");
        }
        if (!ativo.getStyleClass().contains("filter-chip-active")) ativo.getStyleClass().add("filter-chip-active");
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

            boolean correspondeEstado = filtroEstado == null
                    || filtroEstado == p.getEstado();

            return correspondePesquisa && correspondeEstado;
        });

        if (lblTotalPedidos != null)
            lblTotalPedidos.setText(pedidosFiltrados.size() + " pedidos");
    }

    // ─── Ações ────────────────────────────────────────────────────────────────

    @FXML
    private void criarNovoPedido() {
        try {
            SceneManager.trocarTela("/fxml/assistente/novo-pedido-compra.fxml",
                    "/css/assistente-style.css");
        } catch (Exception ex) {
            mostrarErro("Não foi possível abrir o formulário de pedido: " + ex.getMessage());
        }
    }

    private void abrirDetalhesPedido(PedidoCompra p) {
        try {
            PedidoCompra completo = pedidoCompraService.buscarCompletoParaDetalhes(p.getId());
            List<ItemPedido> itens = completo.getItens();
            StringBuilder sb = new StringBuilder();
            sb.append("Pedido #").append(completo.getId()).append("\n");
            sb.append("Fornecedor: ").append(completo.getIdFornecedor() != null && completo.getIdFornecedor().getNome() != null
                    ? completo.getIdFornecedor().getNome() : "-").append("\n");
            sb.append("Data: ").append(completo.getDataPedido() != null ? completo.getDataPedido().format(DATA_FMT) : "-").append("\n");
            sb.append("Estado: ").append(textoEstado(completo.getEstado())).append("\n\n");
            sb.append("Itens:\n");
            if (itens != null) {
                for (ItemPedido item : itens) {
                    String mat = item.getIdMaterial() != null && item.getIdMaterial().getNome() != null
                            ? item.getIdMaterial().getNome() : "-";
                    sb.append("  • ").append(mat)
                      .append(" — ").append(item.getQuantidade()).append(" un.")
                      .append(" × ").append(String.format("%.2f €", item.getValor()))
                      .append("\n");
                }
                BigDecimal total = pedidoCompraService.calcularTotal(itens);
                sb.append("\nTotal: ").append(String.format("%.2f €", total));
            }

            if (completo.getObservacoes() != null && !completo.getObservacoes().isBlank())
                sb.append("\n\nObservações: ").append(completo.getObservacoes());

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Pedido #" + completo.getId());
            a.setHeaderText("Detalhes do Pedido de Compra");
            a.setContentText(sb.toString());
            a.getDialogPane().setMinWidth(480);
            a.showAndWait();

        } catch (Exception e) {
            mostrarErro("Não foi possível carregar os detalhes: " + e.getMessage());
        }
    }

    private void confirmarCancelar(PedidoCompra p) {
        Optional<ButtonType> res = confirmar(
                "Cancelar Pedido #" + p.getId(),
                "Tem a certeza que pretende cancelar este pedido?\nEsta ação não pode ser desfeita.");
        if (res.isEmpty() || res.get() != ButtonType.OK) return;
        try {
            pedidoCompraService.cancelarPedido(p.getId());
            carregarPedidos();
            mostrarSucesso("Pedido cancelado com sucesso.");
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void confirmarRecepcao(PedidoCompra p) {
        Optional<ButtonType> res = confirmar(
                "Confirmar Receção — Pedido #" + p.getId(),
                "Ao confirmar a receção:\n"
                + "• O estado passará para Recebido.\n"
                + "• O stock dos materiais será atualizado.\n"
                + "• Será registada uma movimentação de entrada por cada material.\n\n"
                + "Confirmar?");
        if (res.isEmpty() || res.get() != ButtonType.OK) return;
        try {
            pedidoCompraService.marcarComoRecebido(p.getId());
            carregarPedidos();
            mostrarSucesso("Receção confirmada. O stock foi atualizado.");
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
    }

    // ─── Utilitários ──────────────────────────────────────────────────────────

    private String textoEstado(EstadoPedidoCompra e) {
        if (e == null) return "-";
        return switch (e) {
            case PENDENTE  -> "Pendente";
            case ENVIADO   -> "Enviado";
            case RECEBIDO  -> "Recebido";
            case CANCELADO -> "Cancelado";
        };
    }

    private String classeEstado(EstadoPedidoCompra e) {
        if (e == null) return "badge-estado-pendente";
        return switch (e) {
            case PENDENTE  -> "badge-estado-pendente";
            case ENVIADO   -> "badge-estado-enviado";
            case RECEBIDO  -> "badge-estado-recebido";
            case CANCELADO -> "badge-estado-cancelado";
        };
    }

    private Optional<ButtonType> confirmar(String titulo, String mensagem) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensagem);
        return a.showAndWait();
    }

    private void mostrarSucesso(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sucesso");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro");
        a.setHeaderText(null);
        a.setContentText(msg != null ? msg : "Ocorreu um erro inesperado.");
        a.showAndWait();
    }
}
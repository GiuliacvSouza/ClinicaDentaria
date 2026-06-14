package controller.assistente;

import app.MainFX;
import app.SceneManager;
import bll.PedidoCompraService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

        // N.º itens — número de materiais diferentes no pedido
        colTotalItens.setCellValueFactory(c -> {
            try {
                int n = c.getValue().getItens() != null ? c.getValue().getItens().size() : 0;
                return new SimpleStringProperty(String.valueOf(n));
            } catch (Exception e) {
                return new SimpleStringProperty("-");
            }
        });

        // Valor total
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

        // Ações — botões alinhados com slots de largura fixa
        colAcoes.setCellValueFactory(c -> null);
        colAcoes.setStyle("-fx-alignment: CENTER-LEFT;");
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer      = new Button("Ver");
            private final Button btnCancelar = new Button("Cancelar");
            private final Button btnReceber  = new Button("Recebido");
            private final HBox   box         = new HBox(8);

            {
                btnVer.getStyleClass().add("table-action-button");
                btnReceber.getStyleClass().add("table-action-button");
                btnCancelar.getStyleClass().add("table-link-button");

                btnVer.setPrefWidth(110);
                btnVer.setMinWidth(110);
                btnReceber.setPrefWidth(120);
                btnReceber.setMinWidth(120);
                btnCancelar.setPrefWidth(90);
                btnCancelar.setMinWidth(90);

                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                box.getChildren().addAll(btnVer, btnReceber, btnCancelar);
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

                boolean mostrarAcoesExtras = (estado == EstadoPedidoCompra.PENDENTE
                                           || estado == EstadoPedidoCompra.ENVIADO);
                // visible=false mantém managed=true → o slot preserva a largura
                btnReceber.setVisible(mostrarAcoesExtras);
                btnCancelar.setVisible(mostrarAcoesExtras);

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

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/assistente/detalhes-pedido-modal.fxml"));
            if (MainFX.getSpringContext() != null)
                loader.setControllerFactory(MainFX.getSpringContext()::getBean);

            Parent root = loader.load();
            DetalhesPedidoController ctrl = loader.getController();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tblPedidos.getScene().getWindow());
            modal.setResizable(false);
            modal.setTitle("Pedido #" + completo.getId());

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/assistente-style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            ctrl.setStage(modal);
            ctrl.setPedido(completo);
            modal.setScene(scene);
            modal.showAndWait();

        } catch (Exception e) {
            mostrarErro("Não foi possível carregar os detalhes: " + e.getMessage());
        }
    }

    private void confirmarCancelar(PedidoCompra p) {
        Optional<ButtonType> res = mostrarConfirmacao(
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
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/assistente/confirmar-rececao-modal.fxml"));
            if (MainFX.getSpringContext() != null)
                loader.setControllerFactory(MainFX.getSpringContext()::getBean);

            Parent root = loader.load();
            ConfirmarRececaoController ctrl = loader.getController();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tblPedidos.getScene().getWindow());
            modal.setResizable(false);
            modal.setTitle("Confirmar Receção — Pedido #" + p.getId());

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/assistente-style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            ctrl.setStage(modal);
            ctrl.setPedido(p);
            modal.setScene(scene);
            modal.showAndWait();

            if (ctrl.isConfirmed()) {
                try {
                    pedidoCompraService.marcarComoRecebido(p.getId());
                    carregarPedidos();
                    mostrarSucesso("Receção confirmada. O stock foi atualizado.");
                } catch (RuntimeException ex) {
                    mostrarErro(ex.getMessage());
                }
            }

        } catch (Exception ex) {
            mostrarErro("Não foi possível abrir a confirmação: " + ex.getMessage());
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

    private Optional<ButtonType> mostrarConfirmacao(String titulo, String mensagem) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensagem);
        aplicarCssAoAlert(a);
        ButtonType sim = new ButtonType("Sim");
        ButtonType nao = new ButtonType("Não");
        a.getButtonTypes().setAll(sim, nao);
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == sim) {
            return Optional.of(ButtonType.OK);
        }
        return Optional.empty();
    }

    private void mostrarSucesso(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sucesso");
        a.setHeaderText(null);
        a.setContentText(msg);
        aplicarCssAoAlert(a);
        a.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro");
        a.setHeaderText(null);
        a.setContentText(msg != null ? msg : "Ocorreu um erro inesperado.");
        aplicarCssAoAlert(a);
        a.showAndWait();
    }

    private void aplicarCssAoAlert(Alert alert) {
        var css = getClass().getResource("/css/assistente-style.css");
        if (css != null) {
            alert.getDialogPane().getStylesheets().add(css.toExternalForm());
            alert.getDialogPane().getStyleClass().add("custom-alert");
        }
    }
}

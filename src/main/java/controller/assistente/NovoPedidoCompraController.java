package controller.assistente;

import app.SceneManager;
import app.SessionContext;
import bll.FornecedorService;
import bll.MaterialService;
import bll.PedidoCompraService;
import dal.AssistenteRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Assistente;
import model.Fornecedor;
import model.ItemPedido;
import model.Material;
import model.PedidoCompra;
import model.Utilizador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller da página "Novo Pedido de Compra".
 * Funciona como página completa (não modal), integrada na navbar do módulo Assistente.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NovoPedidoCompraController extends BaseAssistenteController {

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private ComboBox<Fornecedor> cbFornecedor;
    @FXML private TextArea             txtObservacoes;
    @FXML private ComboBox<Material>   cbMaterial;
    @FXML private TextField            txtQuantidade;
    @FXML private TextField            txtValorUnitario;
    @FXML private Label                lblErroItem;

    @FXML private TableView<ItemPedido>           tblItens;
    @FXML private TableColumn<ItemPedido, String> colItemMaterial;
    @FXML private TableColumn<ItemPedido, String> colItemQtd;
    @FXML private TableColumn<ItemPedido, String> colItemValorUnit;
    @FXML private TableColumn<ItemPedido, String> colItemSubtotal;
    @FXML private TableColumn<ItemPedido, String> colItemRemover;

    @FXML private Label lblTotalItens;
    @FXML private Label lblTotalPedido;
    @FXML private Label lblErroPedido;

    // ─── Dependências ─────────────────────────────────────────────────────────

    @Autowired private FornecedorService   fornecedorService;
    @Autowired private MaterialService     materialService;
    @Autowired private PedidoCompraService pedidoCompraService;
    @Autowired private AssistenteRepository assistenteRepository;

    // ─── Estado ───────────────────────────────────────────────────────────────

    private final ObservableList<ItemPedido> itens = FXCollections.observableArrayList();

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        carregarFornecedores();
        carregarMateriais();
        configurarTabelaItens();
        atualizarTotais();

        cbMaterial.valueProperty().addListener((obs, anterior, novo) -> {
            if (novo != null && novo.getValorUnitario() != null) {
                txtValorUnitario.setText(
                        novo.getValorUnitario().setScale(2, RoundingMode.HALF_UP).toPlainString());
            } else {
                txtValorUnitario.clear();
            }
        });
    }

    // ─── Carregamento ─────────────────────────────────────────────────────────

    private void carregarFornecedores() {
        try {
            cbFornecedor.setItems(FXCollections.observableArrayList(fornecedorService.listarTodos()));
            cbFornecedor.setCellFactory(lv -> celulaFornecedor());
            cbFornecedor.setButtonCell(celulaFornecedor());
        } catch (Exception e) {
            mostrarErro("Não foi possível carregar os fornecedores.");
        }
    }

    private void carregarMateriais() {
        try {
            List<Material> ativos = materialService.listarTodos().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getAtivo()))
                    .sorted((a, b) -> {
                        if (a.getNome() == null) return 1;
                        if (b.getNome() == null) return -1;
                        return a.getNome().compareToIgnoreCase(b.getNome());
                    })
                    .toList();
            cbMaterial.setItems(FXCollections.observableArrayList(ativos));
            cbMaterial.setCellFactory(lv -> celulaMaterial());
            cbMaterial.setButtonCell(celulaMaterial());
        } catch (Exception e) {
            mostrarErro("Não foi possível carregar os materiais.");
        }
    }

    // ─── Tabela de itens ──────────────────────────────────────────────────────

    private void configurarTabelaItens() {
        colItemMaterial.setCellValueFactory(c -> {
            Material m = c.getValue().getIdMaterial();
            return new SimpleStringProperty(m != null && m.getNome() != null ? m.getNome() : "-");
        });

        colItemQtd.setCellValueFactory(c -> {
            Integer q = c.getValue().getQuantidade();
            String unidade = c.getValue().getIdMaterial() != null
                    && c.getValue().getIdMaterial().getUnidadeMedida() != null
                    ? " " + c.getValue().getIdMaterial().getUnidadeMedida() : "";
            return new SimpleStringProperty(q != null ? q + unidade : "-");
        });

        colItemValorUnit.setCellValueFactory(c ->
                new SimpleStringProperty(formatarEuros(c.getValue().getValor())));

        colItemSubtotal.setCellValueFactory(c -> {
            BigDecimal v = c.getValue().getValor();
            Integer q   = c.getValue().getQuantidade();
            if (v == null || q == null) return new SimpleStringProperty("-");
            return new SimpleStringProperty(formatarEuros(v.multiply(BigDecimal.valueOf(q))));
        });

        colItemRemover.setCellValueFactory(c -> new SimpleStringProperty(""));
        colItemRemover.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            { btn.getStyleClass().add("btn-remover-item"); }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                ItemPedido row = (ItemPedido) getTableRow().getItem();
                btn.setOnAction(e -> removerItem(row));
                setGraphic(btn);
                setText(null);
            }
        });

        tblItens.setItems(itens);
    }

    // ─── Ações ────────────────────────────────────────────────────────────────

    @FXML
    private void adicionarItem() {
        esconderErroItem();

        Material material = cbMaterial.getValue();
        if (material == null) { mostrarErroItem("Selecione um material."); return; }

        boolean repetido = itens.stream()
                .anyMatch(i -> i.getIdMaterial() != null
                        && i.getIdMaterial().getId().equals(material.getId()));
        if (repetido) { mostrarErroItem("Este material já foi adicionado ao pedido."); return; }

        String qStr = txtQuantidade.getText().trim();
        int quantidade;
        try {
            quantidade = Integer.parseInt(qStr);
            if (quantidade <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarErroItem("A quantidade deve ser superior a zero.");
            return;
        }

        String vStr = txtValorUnitario.getText().trim().replace(',', '.');
        BigDecimal valorUnitario;
        try {
            valorUnitario = new BigDecimal(vStr).setScale(2, RoundingMode.HALF_UP);
            if (valorUnitario.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarErroItem("O valor unitário não pode ser negativo.");
            return;
        }

        ItemPedido novo = new ItemPedido();
        novo.setIdMaterial(material);
        novo.setQuantidade(quantidade);
        novo.setValor(valorUnitario);

        itens.add(novo);
        atualizarTotais();

        cbMaterial.getSelectionModel().clearSelection();
        txtQuantidade.clear();
        txtValorUnitario.clear();
    }

    private void removerItem(ItemPedido item) {
        itens.remove(item);
        atualizarTotais();
    }

    private void atualizarTotais() {
        int n = itens.size();
        if (lblTotalItens != null)
            lblTotalItens.setText(n == 1 ? "1 item" : n + " itens");

        BigDecimal total = itens.stream()
                .filter(i -> i.getValor() != null && i.getQuantidade() != null)
                .map(i -> i.getValor().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (lblTotalPedido != null)
            lblTotalPedido.setText(formatarEuros(total));
    }

    @FXML
    private void guardarPedido() {
        esconderErroPedido();

        if (cbFornecedor.getValue() == null) {
            mostrarErroPedido("Selecione um fornecedor.");
            return;
        }
        if (itens.isEmpty()) {
            mostrarErroPedido("Adicione pelo menos um material ao pedido.");
            return;
        }

        Assistente assistente = resolverAssistente();
        if (assistente == null) {
            mostrarErro("Não foi possível identificar a assistente responsável. Por favor, reinicie a sessão.");
            return;
        }

        try {
            PedidoCompra pedido = new PedidoCompra();
            pedido.setIdFornecedor(cbFornecedor.getValue());
            pedido.setIdAssistente(assistente);
            String obs = txtObservacoes != null ? txtObservacoes.getText().trim() : "";
            if (!obs.isBlank()) pedido.setObservacoes(obs);

            pedidoCompraService.criarPedido(pedido, new ArrayList<>(itens));

            mostrarSucesso("Pedido de compra criado com sucesso.");
            voltarAosPedidos();

        } catch (RuntimeException ex) {
            mostrarErroPedido(ex.getMessage());
        }
    }

    @FXML
    private void voltarAosPedidos() {
        try {
            SceneManager.trocarTela("/fxml/assistente/pedidos-compra.fxml",
                    "/css/assistente-style.css");
        } catch (Exception e) {
            mostrarErro("Não foi possível voltar à lista de pedidos.");
        }
    }

    // ─── Utilitários ──────────────────────────────────────────────────────────

    private Assistente resolverAssistente() {
        Utilizador u = SessionContext.getUtilizadorLogado();
        if (u == null) return null;
        try {
            return assistenteRepository.findByUtilizadorId(u.getId()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private ListCell<Fornecedor> celulaFornecedor() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Fornecedor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String nome = (item.getNome() != null ? item.getNome() : "")
                        + (item.getUltimoNome() != null && !item.getUltimoNome().isBlank()
                        ? " " + item.getUltimoNome() : "");
                setText(nome.isBlank() ? "-" : nome.trim());
            }
        };
    }

    private ListCell<Material> celulaMaterial() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Material item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String nome    = item.getNome() != null ? item.getNome() : "-";
                String unidade = item.getUnidadeMedida() != null
                        ? " (" + item.getUnidadeMedida() + ")" : "";
                setText(nome + unidade);
            }
        };
    }

    private String formatarEuros(BigDecimal valor) {
        if (valor == null) return "-";
        return String.format("%.2f €", valor);
    }

    private void mostrarErroItem(String msg) {
        if (lblErroItem == null) return;
        lblErroItem.setText(msg);
        lblErroItem.setVisible(true);
        lblErroItem.setManaged(true);
    }

    private void esconderErroItem() {
        if (lblErroItem == null) return;
        lblErroItem.setVisible(false);
        lblErroItem.setManaged(false);
    }

    private void mostrarErroPedido(String msg) {
        if (lblErroPedido == null) return;
        lblErroPedido.setText(msg);
        lblErroPedido.setVisible(true);
        lblErroPedido.setManaged(true);
    }

    private void esconderErroPedido() {
        if (lblErroPedido == null) return;
        lblErroPedido.setVisible(false);
        lblErroPedido.setManaged(false);
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
        a.setContentText(msg);
        a.showAndWait();
    }
}

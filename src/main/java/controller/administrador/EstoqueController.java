package controller.administrador;

import app.SceneManager;
import bll.AuditoriaService;
import bll.MaterialService;
import bll.PedidoCompraService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Material;
import model.PedidoCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EstoqueController extends BaseAdministradorController {

    @FXML private TextField txtPesquisa;
    @FXML private Label lblTotalMateriais;
    @FXML private Label lblMateriaisCriticos;
    @FXML private Label lblPedidosPendentes;

    @FXML private TableView<Material> tabelaAlertas;
    @FXML private TableColumn<Material, Integer> colAlertId;
    @FXML private TableColumn<Material, String> colAlertMaterial;
    @FXML private TableColumn<Material, Integer> colAlertAtual;
    @FXML private TableColumn<Material, Integer> colAlertMinima;
    @FXML private TableColumn<Material, String> colAlertUnidade;
    @FXML private TableColumn<Material, String> colAlertEstado;

    @FXML private TableView<Material> tabelaMateriais;
    @FXML private TableColumn<Material, Integer> colMatId;
    @FXML private TableColumn<Material, String> colMatCodigo;
    @FXML private TableColumn<Material, String> colMatNome;
    @FXML private TableColumn<Material, Integer> colMatAtual;
    @FXML private TableColumn<Material, Integer> colMatMinima;
    @FXML private TableColumn<Material, String> colMatUnidade;

    @Autowired private MaterialService materialService;
    @Autowired private PedidoCompraService pedidoCompraService;
    @Autowired private AuditoriaService auditoriaService;

    private final ObservableList<Material> materiais = FXCollections.observableArrayList();
    private final ObservableList<Material> materiaisAlerta = FXCollections.observableArrayList();
    private List<Material> todosMateriais;

    @Override
    protected void inicializarEcra() {
        colAlertId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAlertMaterial.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colAlertAtual.setCellValueFactory(new PropertyValueFactory<>("quantidadeAtual"));
        colAlertMinima.setCellValueFactory(new PropertyValueFactory<>("quantidadeMinima"));
        colAlertUnidade.setCellValueFactory(new PropertyValueFactory<>("unidadeMedida"));
        colAlertEstado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getQuantidadeAtual() != null && c.getValue().getQuantidadeAtual() == 0
                                ? "CRÍTICO" : "BAIXO"));

        colMatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMatCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoInterno"));
        colMatNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colMatAtual.setCellValueFactory(new PropertyValueFactory<>("quantidadeAtual"));
        colMatMinima.setCellValueFactory(new PropertyValueFactory<>("quantidadeMinima"));
        colMatUnidade.setCellValueFactory(new PropertyValueFactory<>("unidadeMedida"));

        tabelaAlertas.setItems(materiaisAlerta);
        tabelaMateriais.setItems(materiais);

        carregar();
    }

    private void carregar() {
        try {
            todosMateriais = materialService.listarTodos();
            materiais.setAll(todosMateriais);
            materiaisAlerta.setAll(todosMateriais.stream()
                    .filter(m -> m.getQuantidadeAtual() != null
                            && m.getQuantidadeMinima() != null
                            && m.getQuantidadeAtual() <= m.getQuantidadeMinima())
                    .collect(Collectors.toList()));

            lblTotalMateriais.setText(String.valueOf(todosMateriais.size()));
            lblMateriaisCriticos.setText(String.valueOf(materiaisAlerta.size()));

            long pedidosPendentes = pedidoCompraService.listarTodos() != null
                    ? pedidoCompraService.listarTodos().size() : 0;
            lblPedidosPendentes.setText(String.valueOf(pedidosPendentes));
        } catch (Exception e) {
            mostrarErro("Erro ao carregar: " + e.getMessage());
        }
    }

    @FXML
    private void filtrar() {
        if (todosMateriais == null) return;
        String termo = txtPesquisa.getText() != null ? txtPesquisa.getText().trim().toLowerCase() : "";
        if (termo.isBlank()) {
            materiais.setAll(todosMateriais);
            return;
        }
        materiais.setAll(todosMateriais.stream()
                .filter(m -> (m.getNome() != null && m.getNome().toLowerCase().contains(termo))
                        || (m.getCodigoInterno() != null && m.getCodigoInterno().toLowerCase().contains(termo)))
                .collect(Collectors.toList()));
    }

    @FXML
    private void abrirMaterial() {
        Material m = tabelaAlertas.getSelectionModel().getSelectedItem();
        if (m == null) {
            m = tabelaMateriais.getSelectionModel().getSelectedItem();
        }
        if (m == null) {
            mostrarInfo("Selecione um material.");
            return;
        }
        mostrarInfo("Material: " + m.getNome() + "\nQuantidade: " + m.getQuantidadeAtual()
                + " " + m.getUnidadeMedida() + "\nMinima: " + m.getQuantidadeMinima()
                + "\nFornecedor: " + (m.getIdFornecedor() != null ? m.getIdFornecedor().getNome() : "N/D"));
    }

    @FXML
    private void criarPedidoCompra() {
        Material m = tabelaAlertas.getSelectionModel().getSelectedItem();
        if (m == null) {
            m = tabelaMateriais.getSelectionModel().getSelectedItem();
        }
        if (m == null) {
            mostrarInfo("Selecione um material para criar o pedido.");
            return;
        }
        try {
            PedidoCompra pedido = new PedidoCompra();
            pedido.setDataPedido(java.time.LocalDate.now());
            pedido.setObservacoes("Reposicao automatica de stock para: " + m.getNome());
            if (m.getIdFornecedor() != null) {
                pedido.setIdFornecedor(m.getIdFornecedor());
            }
            pedidoCompraService.criarPedido(pedido, java.util.List.of());
            auditoriaService.registar(utilizadorLogado(), "CRIAR_PEDIDO_COMPRA",
                    "Material: " + m.getNome() + " - Pedido automatico");
            carregar();
            mostrarInfo("Pedido de compra criado.");
        } catch (Exception e) {
            mostrarErro("Erro ao criar pedido: " + e.getMessage());
        }
    }
}

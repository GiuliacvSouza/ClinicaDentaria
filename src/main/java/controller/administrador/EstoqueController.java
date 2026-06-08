package controller.administrador;

import bll.AuditoriaService;
import bll.MaterialService;
import bll.PedidoCompraService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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
    @FXML private TableColumn<Material, Integer> colMatMaxima;
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
                new SimpleStringProperty(
                        c.getValue().getQuantidadeAtual() != null && c.getValue().getQuantidadeAtual() == 0
                                ? "CRÍTICO" : "BAIXO"));

        colMatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMatCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoInterno"));
        colMatNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colMatAtual.setCellValueFactory(new PropertyValueFactory<>("quantidadeAtual"));
        colMatMinima.setCellValueFactory(new PropertyValueFactory<>("quantidadeMinima"));
        colMatMaxima.setCellValueFactory(new PropertyValueFactory<>("quantidadeMaxima"));
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
        editarMaterial(m);
    }

    private void editarMaterial(Material material) {
        Dialog<Material> dialog = new Dialog<>();
        dialog.setTitle("Editar Material");
        dialog.setHeaderText("Configurar limites: " + material.getNome());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPrefWidth(400);
        grid.setPadding(new Insets(10));

        ColumnConstraints colLabel = new ColumnConstraints();
        colLabel.setPercentWidth(40);
        ColumnConstraints colField = new ColumnConstraints();
        colField.setPercentWidth(60);
        colField.setFillWidth(true);
        colField.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(colLabel, colField);

        TextField txtAtual = new TextField(String.valueOf(
                material.getQuantidadeAtual() != null ? material.getQuantidadeAtual() : 0));
        txtAtual.setPrefWidth(200);

        TextField txtMinima = new TextField(String.valueOf(
                material.getQuantidadeMinima() != null ? material.getQuantidadeMinima() : 0));
        txtMinima.setPrefWidth(200);

        TextField txtMaxima = new TextField(String.valueOf(
                material.getQuantidadeMaxima() != null ? material.getQuantidadeMaxima() : 0));
        txtMaxima.setPrefWidth(200);

        grid.add(new Label("Quantidade Atual:"), 0, 0);
        grid.add(txtAtual, 1, 0);
        grid.add(new Label("Quantidade Mínima:"), 0, 1);
        grid.add(txtMinima, 1, 1);
        grid.add(new Label("Quantidade Máxima:"), 0, 2);
        grid.add(txtMaxima, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(480);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    material.setQuantidadeAtual(Integer.parseInt(txtAtual.getText().trim()));
                    material.setQuantidadeMinima(Integer.parseInt(txtMinima.getText().trim()));
                    material.setQuantidadeMaxima(Integer.parseInt(txtMaxima.getText().trim()));
                } catch (NumberFormatException e) {
                    mostrarErro("Valores inválidos. Use números inteiros.");
                    return null;
                }
                return material;
            }
            return null;
        });

        Optional<Material> res = dialog.showAndWait();
        res.ifPresent(m -> {
            try {
                materialService.salvar(m);
                auditoriaService.registar(utilizadorLogado(), "EDITAR_MATERIAL",
                        "Material: " + m.getNome() + " - Atual:" + m.getQuantidadeAtual()
                        + " Min:" + m.getQuantidadeMinima() + " Max:" + m.getQuantidadeMaxima());
                carregar();
                mostrarInfo("Material atualizado.");
            } catch (Exception e) {
                mostrarErro("Erro ao atualizar: " + e.getMessage());
            }
        });
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
            // Criar pedido via UI simplificada – redireciona para edição de material para configurar limites
            editarMaterial(m);
        } catch (Exception e) {
            mostrarErro("Erro ao criar pedido: " + e.getMessage());
        }
    }
}
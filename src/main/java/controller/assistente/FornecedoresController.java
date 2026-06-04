package controller.assistente;

import bll.FornecedorService;
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
import model.Fornecedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FornecedoresController extends BaseAssistenteController {

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private TextField  txtPesquisa;
    @FXML private Label      lblTotalFornecedores;
    @FXML private TableView<Fornecedor>          tblFornecedores;
    @FXML private TableColumn<Fornecedor, String> colNome;
    @FXML private TableColumn<Fornecedor, String> colNif;
    @FXML private TableColumn<Fornecedor, String> colEmail;
    @FXML private TableColumn<Fornecedor, String> colTelefone;
    @FXML private TableColumn<Fornecedor, String> colCategoria;
    @FXML private TableColumn<Fornecedor, String> colAcoes;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private FornecedorService fornecedorService;

    private ObservableList<Fornecedor> todosFornecedores;
    private FilteredList<Fornecedor>   fornecedoresFiltrados;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        configurarTabela();
        carregarFornecedores();

        if (txtPesquisa != null) {
            txtPesquisa.textProperty().addListener((obs, o, n) -> aplicarFiltros());
        }
    }

    private void configurarTabela() {
        colNome.setCellValueFactory(c -> {
            Fornecedor f = c.getValue();
            String nome = (f.getNome() != null ? f.getNome() : "")
                    + (f.getUltimoNome() != null && !f.getUltimoNome().isBlank()
                    ? " " + f.getUltimoNome() : "");
            return new SimpleStringProperty(nome.isBlank() ? "-" : nome.trim());
        });

        // Fornecedor ainda não tem campo NIF no modelo
        colNif.setCellValueFactory(c -> new SimpleStringProperty("-"));

        colEmail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEmail() != null ? c.getValue().getEmail() : "-"));

        colTelefone.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTelefone() != null ? c.getValue().getTelefone() : "-"));

        // Fornecedor não tem categoria no modelo atual
        colCategoria.setCellValueFactory(c -> new SimpleStringProperty("-"));

        colAcoes.setCellValueFactory(c -> new SimpleStringProperty(""));
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("Ver detalhes");
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

    // ─── Carregamento ─────────────────────────────────────────────────────────

    private void carregarFornecedores() {
        try {
            todosFornecedores = FXCollections.observableArrayList(fornecedorService.listarTodos());
            fornecedoresFiltrados = new FilteredList<>(todosFornecedores, f -> true);
            tblFornecedores.setItems(fornecedoresFiltrados);
            atualizarContador();
        } catch (Exception e) {
            tblFornecedores.setPlaceholder(new Label("Não foi possível carregar os fornecedores."));
        }
    }

    private void aplicarFiltros() {
        if (fornecedoresFiltrados == null) return;
        String pesquisa = txtPesquisa != null ? txtPesquisa.getText().trim().toLowerCase() : "";

        fornecedoresFiltrados.setPredicate(f -> {
            if (pesquisa.isBlank()) return true;
            String nome = ((f.getNome() != null ? f.getNome() : "")
                    + " " + (f.getUltimoNome() != null ? f.getUltimoNome() : "")).trim().toLowerCase();
            String email = f.getEmail() != null ? f.getEmail().toLowerCase() : "";
            String tel   = f.getTelefone() != null ? f.getTelefone() : "";
            return nome.contains(pesquisa) || email.contains(pesquisa) || tel.contains(pesquisa);
        });

        atualizarContador();
    }

    private void atualizarContador() {
        int total = fornecedoresFiltrados != null ? fornecedoresFiltrados.size()
                : (todosFornecedores != null ? todosFornecedores.size() : 0);
        if (lblTotalFornecedores != null) lblTotalFornecedores.setText(total + " fornecedores");
    }

    private void mostrarDetalhes(int index) {
        if (index < 0 || index >= tblFornecedores.getItems().size()) return;
        Fornecedor f = tblFornecedores.getItems().get(index);
        javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        info.setTitle("Fornecedor");
        info.setHeaderText(f.getNome() != null ? f.getNome() : "-");
        info.setContentText("Email: " + (f.getEmail() != null ? f.getEmail() : "-")
                + "\nTelefone: " + (f.getTelefone() != null ? f.getTelefone() : "-"));
        info.showAndWait();
    }
}

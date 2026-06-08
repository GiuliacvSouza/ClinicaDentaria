package controller.assistente;

import bll.FornecedorService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

    @FXML private TableView<Fornecedor>           tblFornecedores;
    @FXML private TableColumn<Fornecedor, String> colNome;
    @FXML private TableColumn<Fornecedor, String> colEmail;
    @FXML private TableColumn<Fornecedor, String> colTelefone;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private FornecedorService fornecedorService;

    private ObservableList<Fornecedor> todosFornecedores;
    private FilteredList<Fornecedor>   fornecedoresFiltrados;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        configurarTabela();
        carregarFornecedores();

        if (txtPesquisa != null)
            txtPesquisa.textProperty().addListener((obs, o, n) -> aplicarFiltros());
    }

    // ─── Tabela ───────────────────────────────────────────────────────────────

    private void configurarTabela() {
        colNome.setCellValueFactory(c -> {
            Fornecedor f = c.getValue();
            String nome = (f.getNome() != null ? f.getNome() : "")
                    + (f.getUltimoNome() != null && !f.getUltimoNome().isBlank()
                    ? " " + f.getUltimoNome() : "");
            return new SimpleStringProperty(nome.isBlank() ? "-" : nome.trim());
        });

        colEmail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEmail() != null && !c.getValue().getEmail().isBlank()
                        ? c.getValue().getEmail() : "-"));

        colTelefone.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTelefone() != null && !c.getValue().getTelefone().isBlank()
                        ? c.getValue().getTelefone() : "-"));
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

    // ─── Filtros ──────────────────────────────────────────────────────────────

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

    // ─── Utilitários ──────────────────────────────────────────────────────────

    private void atualizarContador() {
        int total = fornecedoresFiltrados != null ? fornecedoresFiltrados.size()
                : (todosFornecedores != null ? todosFornecedores.size() : 0);
        if (lblTotalFornecedores != null)
            lblTotalFornecedores.setText(total + (total == 1 ? " fornecedor" : " fornecedores"));
    }
}

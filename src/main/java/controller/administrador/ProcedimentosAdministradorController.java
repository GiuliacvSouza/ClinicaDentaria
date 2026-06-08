package controller.administrador;

import bll.AuditoriaService;
import bll.ProcedimentoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import model.Procedimento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProcedimentosAdministradorController extends BaseAdministradorController {

    @FXML private TextField txtPesquisa;
    @FXML private TableView<Procedimento> tabelaProcedimentos;
    @FXML private TableColumn<Procedimento, Integer> colId;
    @FXML private TableColumn<Procedimento, String> colNome;
    @FXML private TableColumn<Procedimento, String> colCategoria;
    @FXML private TableColumn<Procedimento, Integer> colDuracao;
    @FXML private TableColumn<Procedimento, BigDecimal> colValor;
    @FXML private TableColumn<Procedimento, BigDecimal> colIva;
    @FXML private TableColumn<Procedimento, String> colEstado;

    @Autowired private ProcedimentoService procedimentoService;
    @Autowired private AuditoriaService auditoriaService;

    private List<Procedimento> procedimentos;
    private final ObservableList<Procedimento> dados = FXCollections.observableArrayList();

    @Override
    protected void inicializarEcra() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDuracao.setCellValueFactory(new PropertyValueFactory<>("duracaoEstimada"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colIva.setCellValueFactory(new PropertyValueFactory<>("taxaIva"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("status"));

        tabelaProcedimentos.setItems(dados);
        carregar();
    }

    private void carregar() {
        try {
            procedimentos = procedimentoService.listarTodos();
            dados.setAll(procedimentos);
        } catch (Exception e) {
            mostrarErro("Erro ao carregar procedimentos: " + e.getMessage());
        }
    }

    @FXML
    private void filtrar() {
        if (procedimentos == null) return;
        String termo = txtPesquisa.getText() != null ? txtPesquisa.getText().trim().toLowerCase() : "";
        if (termo.isBlank()) {
            dados.setAll(procedimentos);
            return;
        }
        dados.setAll(procedimentos.stream()
                .filter(p -> (p.getNome() != null && p.getNome().toLowerCase().contains(termo))
                        || (p.getTipo() != null && p.getTipo().toLowerCase().contains(termo)))
                .collect(Collectors.toList()));
    }

    @FXML
    private void novoProcedimento() {
        abrirDialogo(null);
    }

    @FXML
    private void editarSelecionado() {
        Procedimento p = tabelaProcedimentos.getSelectionModel().getSelectedItem();
        if (p == null) {
            mostrarInfo("Selecione um procedimento para editar.");
            return;
        }
        abrirDialogo(p);
    }

    @FXML
    private void desativarSelecionado() {
        Procedimento p = tabelaProcedimentos.getSelectionModel().getSelectedItem();
        if (p == null) {
            mostrarInfo("Selecione um procedimento para desativar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText("Desativar " + p.getNome() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            p.setStatus("inativo");
            try {
                procedimentoService.salvar(p);
                auditoriaService.registar(utilizadorLogado(), "DESATIVAR_PROCEDIMENTO",
                        "Procedimento: " + p.getNome());
                carregar();
                mostrarInfo("Procedimento desativado.");
            } catch (Exception e) {
                mostrarErro("Erro: " + e.getMessage());
            }
        }
    }

    private void abrirDialogo(Procedimento existente) {
        Dialog<Procedimento> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Novo Procedimento" : "Editar Procedimento");
        dialog.setHeaderText(existente == null
                ? "Criar novo procedimento" : "Editar: " + existente.getNome());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPrefWidth(420);

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome");
        TextField txtDescricao = new TextField();
        txtDescricao.setPromptText("Descricao");
        ComboBox<String> cmbCategoria = new ComboBox<>();
        cmbCategoria.getItems().addAll("terapeutico", "protese", "estetico");
        cmbCategoria.setValue("terapeutico");
        cmbCategoria.setPrefWidth(Double.MAX_VALUE);
        TextField txtDuracao = new TextField();
        txtDuracao.setPromptText("Minutos");
        TextField txtValor = new TextField();
        txtValor.setPromptText("0.00");
        ComboBox<String> cmbIva = new ComboBox<>();
        cmbIva.getItems().addAll("0", "6", "23");
        cmbIva.setValue("0");
        cmbIva.setPrefWidth(Double.MAX_VALUE);

        if (existente != null) {
            txtNome.setText(existente.getNome());
            txtDescricao.setText(existente.getDescricao());
            cmbCategoria.setValue(existente.getTipo());
            txtDuracao.setText(existente.getDuracaoEstimada() != null
                    ? String.valueOf(existente.getDuracaoEstimada()) : "");
            txtValor.setText(existente.getValor() != null ? existente.getValor().toString() : "");
            cmbIva.setValue(existente.getTaxaIva() != null ? existente.getTaxaIva().toPlainString() : "0");
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Descricao:"), 0, 1);
        grid.add(txtDescricao, 1, 1);
        grid.add(new Label("Categoria:"), 0, 2);
        grid.add(cmbCategoria, 1, 2);
        grid.add(new Label("Duracao (min):"), 0, 3);
        grid.add(txtDuracao, 1, 3);
        grid.add(new Label("Valor (€):"), 0, 4);
        grid.add(txtValor, 1, 4);
        grid.add(new Label("IVA (%):"), 0, 5);
        grid.add(cmbIva, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Procedimento p = existente != null ? existente : new Procedimento();
                p.setNome(txtNome.getText().trim());
                p.setDescricao(txtDescricao.getText().trim());
                p.setTipo(cmbCategoria.getValue());
                p.setDuracaoEstimada(txtDuracao.getText().isBlank() ? null : Integer.parseInt(txtDuracao.getText().trim()));
                p.setValor(txtValor.getText().isBlank() ? BigDecimal.ZERO : new BigDecimal(txtValor.getText().trim()));
                p.setTaxaIva(new BigDecimal(cmbIva.getValue()));
                p.setStatus("ativo");
                return p;
            }
            return null;
        });

        Optional<Procedimento> res = dialog.showAndWait();
        res.ifPresent(p -> {
            try {
                procedimentoService.salvar(p);
                String op = existente == null ? "CRIAR_PROCEDIMENTO" : "EDITAR_PROCEDIMENTO";
                auditoriaService.registar(utilizadorLogado(), op,
                        "Procedimento: " + p.getNome() + " (€" + p.getValor() + ")");
                carregar();
            } catch (Exception e) {
                mostrarErro("Erro ao guardar: " + e.getMessage());
            }
        });
    }
}

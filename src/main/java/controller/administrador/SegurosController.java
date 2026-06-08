package controller.administrador;

import bll.AuditoriaService;
import bll.SeguroService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import model.Seguro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SegurosController extends BaseAdministradorController {

    @FXML private TextField txtPesquisa;
    @FXML private TableView<Seguro> tabelaSeguros;
    @FXML private TableColumn<Seguro, Integer> colId;
    @FXML private TableColumn<Seguro, String> colNome;
    @FXML private TableColumn<Seguro, String> colPlano;
    @FXML private TableColumn<Seguro, String> colCodigo;
    @FXML private TableColumn<Seguro, String> colContacto;
    @FXML private TableColumn<Seguro, LocalDate> colValidade;
    @FXML private TableColumn<Seguro, String> colEstado;

    @Autowired private SeguroService seguroService;
    @Autowired private AuditoriaService auditoriaService;

    private final ObservableList<Seguro> dados = FXCollections.observableArrayList();
    private List<Seguro> todosSeguros;

    @Override
    protected void inicializarEcra() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeSeguro"));
        colPlano.setCellValueFactory(new PropertyValueFactory<>("tipoPlano"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoPlano"));
        colContacto.setCellValueFactory(new PropertyValueFactory<>("contactoSeguradora"));
        colValidade.setCellValueFactory(new PropertyValueFactory<>("validoAte"));
        colEstado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getValidoAte() != null
                                && c.getValue().getValidoAte().isBefore(LocalDate.now())
                                ? "Expirado" : "Ativo"));

        tabelaSeguros.setItems(dados);
        carregar();
    }

    private void carregar() {
        try {
            todosSeguros = seguroService.listarTodos();
            dados.setAll(todosSeguros != null ? todosSeguros : List.of());
        } catch (Exception e) {
            mostrarErro("Erro ao carregar: " + e.getMessage());
        }
    }

    @FXML
    private void filtrar() {
        if (todosSeguros == null) return;
        String termo = txtPesquisa.getText() != null ? txtPesquisa.getText().trim().toLowerCase() : "";
        if (termo.isBlank()) {
            dados.setAll(todosSeguros);
            return;
        }
        dados.setAll(todosSeguros.stream()
                .filter(s -> (s.getNomeSeguro() != null && s.getNomeSeguro().toLowerCase().contains(termo))
                        || (s.getTipoPlano() != null && s.getTipoPlano().toLowerCase().contains(termo)))
                .collect(Collectors.toList()));
    }

    @FXML
    private void novoSeguro() {
        abrirDialogo(null);
    }

    @FXML
    private void editarSelecionado() {
        Seguro s = tabelaSeguros.getSelectionModel().getSelectedItem();
        if (s == null) {
            mostrarInfo("Selecione um seguro.");
            return;
        }
        abrirDialogo(s);
    }

    @FXML
    private void desativarSelecionado() {
        Seguro s = tabelaSeguros.getSelectionModel().getSelectedItem();
        if (s == null) {
            mostrarInfo("Selecione um seguro.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Desativar " + s.getNomeSeguro() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                s.setValidoAte(LocalDate.now().minusDays(1));
                seguroService.salvar(s);
                auditoriaService.registar(utilizadorLogado(), "DESATIVAR_SEGURO",
                        "Seguro: " + s.getNomeSeguro());
                carregar();
                mostrarInfo("Seguro desativado.");
            } catch (Exception e) {
                mostrarErro("Erro: " + e.getMessage());
            }
        }
    }

    private void abrirDialogo(Seguro existente) {
        Dialog<Seguro> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Novo Seguro" : "Editar Seguro");
        dialog.setHeaderText(existente == null ? "Criar novo seguro" : "Editar: " + existente.getNomeSeguro());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPrefWidth(420);

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome do seguro");
        TextField txtPlano = new TextField();
        txtPlano.setPromptText("Plano");
        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Codigo");
        TextField txtContacto = new TextField();
        txtContacto.setPromptText("Email ou telefone");
        DatePicker dpValidade = new DatePicker();
        dpValidade.setValue(LocalDate.now().plusYears(1));

        if (existente != null) {
            txtNome.setText(existente.getNomeSeguro());
            txtPlano.setText(existente.getTipoPlano());
            txtCodigo.setText(existente.getCodigoPlano());
            txtContacto.setText(existente.getContactoSeguradora());
            dpValidade.setValue(existente.getValidoAte());
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Plano:"), 0, 1);
        grid.add(txtPlano, 1, 1);
        grid.add(new Label("Codigo:"), 0, 2);
        grid.add(txtCodigo, 1, 2);
        grid.add(new Label("Contacto:"), 0, 3);
        grid.add(txtContacto, 1, 3);
        grid.add(new Label("Valido ate:"), 0, 4);
        grid.add(dpValidade, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Seguro s = existente != null ? existente : new Seguro();
                s.setNomeSeguro(txtNome.getText().trim());
                s.setTipoPlano(txtPlano.getText().trim());
                s.setCodigoPlano(txtCodigo.getText().trim());
                s.setContactoSeguradora(txtContacto.getText().trim());
                s.setValidoAte(dpValidade.getValue());
                return s;
            }
            return null;
        });

        Optional<Seguro> res = dialog.showAndWait();
        res.ifPresent(s -> {
            try {
                seguroService.salvar(s);
                String op = existente == null ? "CRIAR_SEGURO" : "EDITAR_SEGURO";
                auditoriaService.registar(utilizadorLogado(), op,
                        "Seguro: " + s.getNomeSeguro());
                carregar();
            } catch (Exception e) {
                mostrarErro("Erro ao guardar: " + e.getMessage());
            }
        });
    }
}

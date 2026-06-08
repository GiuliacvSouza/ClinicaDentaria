package controller.administrador;

import bll.AuditoriaService;
import bll.SeguroService;
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

    private static final String[] PLANOS = {"Plano Base", "Plano Standard", "Plano Premium", "Plano Empresarial", "Plano Familiar"};

    @Override
    protected void inicializarEcra() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeSeguro"));
        colPlano.setCellValueFactory(new PropertyValueFactory<>("tipoPlano"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoPlano"));
        colContacto.setCellValueFactory(new PropertyValueFactory<>("contactoSeguradora"));
        colValidade.setCellValueFactory(new PropertyValueFactory<>("validoAte"));

        // Coluna estado com cor: verde para ativo, vermelho para inativo
        colEstado.setCellValueFactory(c -> {
            Seguro s = c.getValue();
            boolean ativo = s.getValidoAte() != null && !s.getValidoAte().isBefore(LocalDate.now());
            return new SimpleStringProperty(ativo ? "Ativo" : "Inativo");
        });
        colEstado.setCellFactory(col -> new TableCell<Seguro, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Ativo".equals(item)) {
                        setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    }
                }
            }
        });

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
        boolean isAtivo = s.getValidoAte() != null && !s.getValidoAte().isBefore(LocalDate.now());
        String acao = isAtivo ? "desativar" : "reativar";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(acao.toUpperCase() + " " + s.getNomeSeguro() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (isAtivo) {
                    s.setValidoAte(LocalDate.now().minusDays(1));
                    auditoriaService.registar(utilizadorLogado(), "DESATIVAR_SEGURO",
                            "Seguro: " + s.getNomeSeguro());
                } else {
                    s.setValidoAte(LocalDate.now().plusYears(3));
                    auditoriaService.registar(utilizadorLogado(), "REATIVAR_SEGURO",
                            "Seguro: " + s.getNomeSeguro());
                }
                seguroService.salvar(s);
                carregar();
                mostrarInfo("Seguro " + acao + "do com sucesso.");
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
        grid.setPrefWidth(480);
        grid.setPadding(new Insets(10));

        ColumnConstraints colLabel = new ColumnConstraints();
        colLabel.setPercentWidth(30);
        ColumnConstraints colField = new ColumnConstraints();
        colField.setPercentWidth(70);
        colField.setFillWidth(true);
        colField.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(colLabel, colField);

        TextField txtNome = new TextField();
        txtNome.setPromptText("Ex: Médis Dental");
        txtNome.setPrefWidth(300);

        ComboBox<String> cmbPlano = new ComboBox<>();
        cmbPlano.getItems().addAll(PLANOS);
        cmbPlano.setValue(PLANOS[0]);
        cmbPlano.setPrefWidth(Double.MAX_VALUE);

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Ex: MD-0001");
        txtCodigo.setPrefWidth(300);

        TextField txtContacto = new TextField();
        txtContacto.setPromptText("Email ou telefone");
        txtContacto.setPrefWidth(300);

        DatePicker dpValidade = new DatePicker();
        dpValidade.setValue(LocalDate.now().plusYears(1));
        dpValidade.setPrefWidth(300);

        if (existente != null) {
            txtNome.setText(existente.getNomeSeguro());
            cmbPlano.setValue(existente.getTipoPlano() != null && !existente.getTipoPlano().isBlank()
                    ? existente.getTipoPlano() : PLANOS[0]);
            txtCodigo.setText(existente.getCodigoPlano());
            txtContacto.setText(existente.getContactoSeguradora());
            dpValidade.setValue(existente.getValidoAte());
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Tipo de Plano:"), 0, 1);
        grid.add(cmbPlano, 1, 1);
        grid.add(new Label("Código:"), 0, 2);
        grid.add(txtCodigo, 1, 2);
        grid.add(new Label("Contacto:"), 0, 3);
        grid.add(txtContacto, 1, 3);
        grid.add(new Label("Válido até:"), 0, 4);
        grid.add(dpValidade, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(540);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Seguro s = existente != null ? existente : new Seguro();
                s.setNomeSeguro(txtNome.getText().trim());
                s.setTipoPlano(cmbPlano.getValue());
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
                        "Seguro: " + s.getNomeSeguro() + " (" + s.getTipoPlano() + ")");
                carregar();
            } catch (Exception e) {
                mostrarErro("Erro ao guardar: " + e.getMessage());
            }
        });
    }
}
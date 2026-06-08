package controller.administrador;

import bll.AuditoriaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.AuditoriaLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AuditoriaController extends BaseAdministradorController {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private TextField txtUtilizador;
    @FXML private ComboBox<String> cmbOperacao;
    @FXML private DatePicker dpDataInicio;
    @FXML private DatePicker dpDataFim;

    @FXML private TableView<AuditoriaLog> tabelaAuditoria;
    @FXML private TableColumn<AuditoriaLog, String> colData;
    @FXML private TableColumn<AuditoriaLog, String> colUtilizador;
    @FXML private TableColumn<AuditoriaLog, String> colPerfil;
    @FXML private TableColumn<AuditoriaLog, String> colOperacao;
    @FXML private TableColumn<AuditoriaLog, String> colDescricao;

    @Autowired private AuditoriaService auditoriaService;

    private final ObservableList<AuditoriaLog> dados = FXCollections.observableArrayList();
    private List<AuditoriaLog> todosLogs;

    @Override
    protected void inicializarEcra() {
        colData.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDataHora() != null
                                ? c.getValue().getDataHora().format(DATA_FMT)
                                : ""));
        colUtilizador.setCellValueFactory(new PropertyValueFactory<>("utilizadorNome"));
        colPerfil.setCellValueFactory(new PropertyValueFactory<>("utilizadorPerfil"));
        colOperacao.setCellValueFactory(new PropertyValueFactory<>("operacao"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        tabelaAuditoria.setItems(dados);

        cmbOperacao.getItems().addAll(
                "LOGIN", "LOGOUT", "CRIAR_UTILIZADOR", "EDITAR_UTILIZADOR",
                "ATIVAR_UTILIZADOR", "DESATIVAR_UTILIZADOR", "REDEFINIR_SENHA",
                "CRIAR_PROCEDIMENTO", "EDITAR_PROCEDIMENTO", "DESATIVAR_PROCEDIMENTO",
                "CONFIG_HORARIO_CLINICA", "CONFIG_HORARIO_DENTISTA",
                "CRIAR_PEDIDO_COMPRA", "CRIAR_SEGURO", "EDITAR_SEGURO", "DESATIVAR_SEGURO");

        carregar();
    }

    private void carregar() {
        try {
            todosLogs = auditoriaService.listarTodos();
            dados.setAll(todosLogs != null ? todosLogs : List.of());
        } catch (Exception e) {
            mostrarErro("Erro ao carregar logs: " + e.getMessage());
        }
    }

    @FXML
    private void aplicarFiltro() {
        if (todosLogs == null) return;
        String utilizador = txtUtilizador.getText() != null ? txtUtilizador.getText().trim().toLowerCase() : "";
        String operacao = cmbOperacao.getValue();
        LocalDate inicio = dpDataInicio.getValue();
        LocalDate fim = dpDataFim.getValue();

        dados.setAll(todosLogs.stream()
                .filter(l -> utilizador.isBlank()
                        || (l.getUtilizadorNome() != null && l.getUtilizadorNome().toLowerCase().contains(utilizador)))
                .filter(l -> operacao == null || operacao.isBlank()
                        || (l.getOperacao() != null && l.getOperacao().equalsIgnoreCase(operacao)))
                .filter(l -> inicio == null || (l.getDataHora() != null && !l.getDataHora().toLocalDate().isBefore(inicio)))
                .filter(l -> fim == null || (l.getDataHora() != null && !l.getDataHora().toLocalDate().isAfter(fim)))
                .collect(Collectors.toList()));
    }

    @FXML
    private void limparFiltro() {
        txtUtilizador.clear();
        cmbOperacao.setValue(null);
        dpDataInicio.setValue(null);
        dpDataFim.setValue(null);
        carregar();
    }
}
package controller.dentista;

import bll.PacienteService;
import bll.PlanoTratamentoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Paciente;
import model.PlanoTratamento;
import model.Utilizador;
import model.enums.EstadoPlanoTratamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanosTratamentoController extends BaseDentistaController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "PT"));

    @FXML private TextField txtPesquisa;
    @FXML private TableView<PlanoTratamento> tblPlanos;
    @FXML private TableColumn<PlanoTratamento, String> colDataInicio;
    @FXML private TableColumn<PlanoTratamento, String> colDataFim;
    @FXML private TableColumn<PlanoTratamento, String> colObjetivo;
    @FXML private TableColumn<PlanoTratamento, String> colEstado;
    @FXML private TableColumn<PlanoTratamento, String> colProgresso;
    @FXML private TableColumn<PlanoTratamento, String> colAcoes;

    // Right side: detail / creation form
    @FXML private VBox panePlaceholder;
    @FXML private VBox paneForm;
    @FXML private Label lblFormTitulo;
    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private TextField txtObjetivo;
    @FXML private TextArea txtEtapas;
    @FXML private TextArea txtProcedimentosPrevistos;
    @FXML private TextField txtValorEstimado;
    @FXML private DatePicker dpDataInicio;
    @FXML private DatePicker dpDataFim;
    @FXML private ComboBox<EstadoPlanoTratamento> cbEstado;
    @FXML private TextField txtProgresso;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    @Autowired private PlanoTratamentoService planoService;
    @Autowired private PacienteService pacienteService;

    private ObservableList<PlanoTratamento> masterData = FXCollections.observableArrayList();
    private FilteredList<PlanoTratamento> filteredData;
    private PlanoTratamento planoEmEdicao;
    private boolean editando = false;

    @Override
    protected void inicializarEcra() {
        txtPesquisa.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        configurarTabela();
        configurarForm();
        carregarDados();
        mostrarPlaceholder();
    }

    private void configurarTabela() {
        tblPlanos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colDataInicio.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDataPrevistaInicio() != null
                        ? cell.getValue().getDataPrevistaInicio().format(DATE_FMT) : "-"));
        colDataFim.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDataPrevistaFim() != null
                        ? cell.getValue().getDataPrevistaFim().format(DATE_FMT) : "-"));
        colObjetivo.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getObjetivo() != null ? cell.getValue().getObjetivo() : "-"));
        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getEstado() != null
                        ? cell.getValue().getEstado().getDescricao() : "-"));
        colProgresso.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getProgresso() != null ? cell.getValue().getProgresso() : "-"));

        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Editar");

            {
                btn.getStyleClass().add("table-action-button");
                btn.setOnAction(e -> {
                    PlanoTratamento p = getTableView().getItems().get(getIndex());
                    carregarPlanoParaEdicao(p);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void configurarForm() {
        // Configurar ComboBox de pacientes
        cbPaciente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Paciente p) {
                if (p == null) return "";
                Utilizador u = p.getUtilizador();
                return u != null ? (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())
                        + " (NIF: " + nvl(u.getNif()) + ")").trim() : "-";
            }

            @Override
            public Paciente fromString(String s) {
                return null;
            }
        });

        try {
            List<Paciente> pacientes = pacienteService.listarTodos();
            cbPaciente.setItems(FXCollections.observableArrayList(pacientes));
        } catch (Exception e) {
            mostrarErro("Erro ao carregar pacientes: " + e.getMessage());
        }

        // Configurar ComboBox de estados
        cbEstado.setItems(FXCollections.observableArrayList(EstadoPlanoTratamento.values()));
        cbEstado.setConverter(new StringConverter<>() {
            @Override
            public String toString(EstadoPlanoTratamento e) {
                return e != null ? e.getDescricao() : "";
            }

            @Override
            public EstadoPlanoTratamento fromString(String s) {
                return null;
            }
        });
    }

    private void carregarDados() {
        try {
            List<PlanoTratamento> planos = planoService.listarTodos();
            masterData.setAll(planos);
            filteredData = new FilteredList<>(masterData, p -> true);
            tblPlanos.setItems(filteredData);
        } catch (Exception e) {
            mostrarErro("Erro ao carregar planos de tratamento: " + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        if (filteredData == null) return;
        String termo = txtPesquisa.getText() != null ? txtPesquisa.getText().toLowerCase().trim() : "";
        filteredData.setPredicate(p -> {
            if (termo.isEmpty()) return true;
            String objetivo = p.getObjetivo() != null ? p.getObjetivo().toLowerCase() : "";
            String progresso = p.getProgresso() != null ? p.getProgresso().toLowerCase() : "";
            String paciente = "";
            if (p.getPaciente() != null && p.getPaciente().getUtilizador() != null) {
                Utilizador u = p.getPaciente().getUtilizador();
                paciente = (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).toLowerCase();
            }
            return objetivo.contains(termo) || progresso.contains(termo) || paciente.contains(termo);
        });
    }

    @FXML
    private void novoPlano() {
        planoEmEdicao = null;
        editando = false;
        limparFormulario();
        mostrarFormulario("Novo Plano de Tratamento");
    }

    private void carregarPlanoParaEdicao(PlanoTratamento p) {
        planoEmEdicao = p;
        editando = true;

        cbPaciente.setValue(p.getPaciente());
        txtObjetivo.setText(nvl(p.getObjetivo()));
        txtEtapas.setText(nvl(p.getEtapas()));
        txtProcedimentosPrevistos.setText(nvl(p.getProcedimentosPrevistos()));
        txtValorEstimado.setText(p.getValorEstimado() != null ? p.getValorEstimado().toString() : "");
        dpDataInicio.setValue(p.getDataPrevistaInicio());
        dpDataFim.setValue(p.getDataPrevistaFim());
        cbEstado.setValue(p.getEstado());
        txtProgresso.setText(nvl(p.getProgresso()));

        mostrarFormulario("Editar Plano de Tratamento");
    }

    @FXML
    private void salvarPlano() {
        Paciente pac = cbPaciente.getValue();
        String obj = txtObjetivo.getText();
        String etapas = txtEtapas.getText();
        String procsPrev = txtProcedimentosPrevistos.getText();
        String valorStr = txtValorEstimado.getText();
        LocalDate dataInicio = dpDataInicio.getValue();
        LocalDate dataFim = dpDataFim.getValue();
        EstadoPlanoTratamento estado = cbEstado.getValue();
        String progresso = txtProgresso.getText();

        if (pac == null) {
            mostrarErro("Selecione o paciente.");
            return;
        }
        if (obj == null || obj.isBlank()) {
            mostrarErro("O objetivo do tratamento e obrigatorio.");
            return;
        }

        try {
            PlanoTratamento plano = (planoEmEdicao != null) ? planoEmEdicao : new PlanoTratamento();
            plano.setPaciente(pac);
            plano.setDentista(dentistaLogado());
            plano.setObjetivo(obj);
            plano.setEtapas(etapas);
            plano.setProcedimentosPrevistos(procsPrev);

            if (valorStr != null && !valorStr.isBlank()) {
                try {
                    plano.setValorEstimado(new BigDecimal(valorStr.replace(",", ".")));
                } catch (NumberFormatException ignored) {
                }
            } else {
                plano.setValorEstimado(BigDecimal.ZERO);
            }

            plano.setDataPrevistaInicio(dataInicio);
            plano.setDataPrevistaFim(dataFim);
            plano.setEstado(estado != null ? estado : EstadoPlanoTratamento.PLANEADO);
            plano.setProgresso(progresso);

            planoService.salvar(plano);
            mostrarInfo("Plano de tratamento " + (editando ? "atualizado" : "criado") + " com sucesso!");

            limparFormulario();
            mostrarPlaceholder();
            carregarDados();
        } catch (Exception e) {
            mostrarErro("Erro ao salvar plano: " + e.getMessage());
        }
    }

    @FXML
    private void cancelarEdicao() {
        limparFormulario();
        mostrarPlaceholder();
    }

    private void limparFormulario() {
        cbPaciente.setValue(null);
        txtObjetivo.clear();
        txtEtapas.clear();
        txtProcedimentosPrevistos.clear();
        txtValorEstimado.clear();
        dpDataInicio.setValue(null);
        dpDataFim.setValue(null);
        cbEstado.setValue(null);
        txtProgresso.clear();
    }

    private void mostrarPlaceholder() {
        panePlaceholder.setVisible(true);
        paneForm.setVisible(false);
    }

    private void mostrarFormulario(String titulo) {
        lblFormTitulo.setText(titulo);
        panePlaceholder.setVisible(false);
        paneForm.setVisible(true);
    }
}

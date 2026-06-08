package controller.dentista;

import bll.ConsultaService;
import model.enums.EstadoConsulta;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.dto.ConsultaAgendadaDTO;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgendaDentistaController extends BaseDentistaController {

    private static final DateTimeFormatter DATA_LONG_FMT =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("pt", "PT"));
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private TextField txtPesquisa;
    @FXML private DatePicker dpData;
    @FXML private Button btnHoje;
    @FXML private Label lblDataSelecionada;

    @FXML private Button btnTodas;
    @FXML private Button btnConfirmadas;
    @FXML private Button btnEmEspera;
    @FXML private Button btnEmConsulta;
    @FXML private Button btnConcluidas;

    @FXML private TableView<ConsultaAgendadaDTO> tblConsultas;
    @FXML private TableColumn<ConsultaAgendadaDTO, String> colHora;
    @FXML private TableColumn<ConsultaAgendadaDTO, String> colPaciente;
    @FXML private TableColumn<ConsultaAgendadaDTO, String> colProcedimento;
    @FXML private TableColumn<ConsultaAgendadaDTO, String> colEstado;
    @FXML private TableColumn<ConsultaAgendadaDTO, String> colAcoes;

    @Autowired private ConsultaService consultaService;

    private ObservableList<ConsultaAgendadaDTO> masterData = FXCollections.observableArrayList();
    private FilteredList<ConsultaAgendadaDTO> filteredData;
    private EstadoConsulta estadoFiltro = null;

    @Override
    protected void inicializarEcra() {
        dpData.setValue(LocalDate.now());
        
        configurarTabela();
        configurarFiltros();
        
        carregarConsultas();
    }

    private void configurarTabela() {
        colHora.setCellValueFactory(cell -> {
            var inst = cell.getValue().getDataHoraInicio();
            if (inst == null) return new SimpleStringProperty("--:--");
            var dt = LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
            return new SimpleStringProperty(dt.format(HORA_FMT));
        });

        colPaciente.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getNomePaciente())));
        colProcedimento.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getProcedimento())));
        
        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(textoEstado(cell.getValue().getStatus())));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-pill", "status-agendado", "status-confirmado", "status-em-espera", "status-em-consulta", "status-concluido", "status-cancelado");
                    getStyleClass().add("status-pill");
                    ConsultaAgendadaDTO dto = getTableView().getItems().get(getIndex());
                    getStyleClass().add(classeEstado(dto.getStatus()));
                }
            }
        });

        colAcoes.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ConsultaAgendadaDTO dto = getTableView().getItems().get(getIndex());
                    Button btn = new Button();
                    btn.getStyleClass().add("table-action-button");
                    
                    if (dto.getStatus() == EstadoConsulta.EM_ESPERA || dto.getStatus() == EstadoConsulta.CONFIRMADA) {
                        btn.setText("Iniciar Consulta");
                        btn.getStyleClass().add("primary-button");
                        btn.setOnAction(e -> iniciarECarregarAtendimento(dto.getIdConsulta()));
                    } else if (dto.getStatus() == EstadoConsulta.EM_CONSULTA) {
                        btn.setText("Continuar");
                        btn.setOnAction(e -> abrirAtendimento(dto.getIdConsulta()));
                    } else if (dto.getStatus() == EstadoConsulta.CONCLUIDA || dto.getStatus() == EstadoConsulta.FATURADA) {
                        btn.setText("Ver Ficha");
                        btn.getStyleClass().add("secondary-button");
                        btn.setOnAction(e -> abrirAtendimento(dto.getIdConsulta()));
                    } else {
                        btn.setText("Detalhes");
                        btn.setDisable(true);
                    }
                    setGraphic(btn);
                }
            }
        });
    }

    private void configurarFiltros() {
        dpData.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                carregarConsultas();
            }
        });

        txtPesquisa.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        atualizarEstiloChips(btnTodas);
    }

    private void carregarConsultas() {
        LocalDate data = dpData.getValue();
        if (data == null) data = LocalDate.now();

        lblDataSelecionada.setText(data.format(DATA_LONG_FMT));

        try {
            List<ConsultaAgendadaDTO> consultas = consultaService
                    .listarAgendadasPorDentistaEDia(dentistaId(), data).stream()
                    .sorted(Comparator.comparing(ConsultaAgendadaDTO::getDataHoraInicio,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            masterData.setAll(consultas);
            filteredData = new FilteredList<>(masterData, c -> true);
            tblConsultas.setItems(filteredData);
            aplicarFiltros();
        } catch (Exception e) {
            mostrarErro("Erro ao carregar consultas: " + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        if (filteredData == null) return;

        String pesquisa = txtPesquisa.getText() != null ? txtPesquisa.getText().toLowerCase().trim() : "";

        filteredData.setPredicate(c -> {
            boolean matchesPesquisa = true;
            if (!pesquisa.isEmpty()) {
                String nome = c.getNomePaciente() != null ? c.getNomePaciente().toLowerCase() : "";
                String nif = c.getNifPaciente() != null ? c.getNifPaciente().toLowerCase() : "";
                matchesPesquisa = nome.contains(pesquisa) || nif.contains(pesquisa);
            }

            boolean matchesEstado = true;
            if (estadoFiltro != null) {
                matchesEstado = c.getStatus() == estadoFiltro;
            }

            return matchesPesquisa && matchesEstado;
        });
    }

    private void iniciarECarregarAtendimento(Integer idConsulta) {
        try {
            consultaService.iniciarConsulta(idConsulta);
            abrirAtendimento(idConsulta);
        } catch (Exception e) {
            mostrarErro("Erro ao iniciar consulta: " + e.getMessage());
        }
    }

    private void abrirAtendimento(Integer idConsulta) {
        navegar("/fxml/dentista/atendimento-dentista.fxml?consulta=" + idConsulta);
    }

    @FXML
    private void escolherHoje() {
        dpData.setValue(LocalDate.now());
    }

    @FXML private void filtrarTodas() { estadoFiltro = null; atualizarEstiloChips(btnTodas); aplicarFiltros(); }
    @FXML private void filtrarConfirmadas() { estadoFiltro = EstadoConsulta.CONFIRMADA; atualizarEstiloChips(btnConfirmadas); aplicarFiltros(); }
    @FXML private void filtrarEmEspera() { estadoFiltro = EstadoConsulta.EM_ESPERA; atualizarEstiloChips(btnEmEspera); aplicarFiltros(); }
    @FXML private void filtrarEmConsulta() { estadoFiltro = EstadoConsulta.EM_CONSULTA; atualizarEstiloChips(btnEmConsulta); aplicarFiltros(); }
    @FXML private void filtrarConcluidas() { estadoFiltro = EstadoConsulta.CONCLUIDA; atualizarEstiloChips(btnConcluidas); aplicarFiltros(); }

    private void atualizarEstiloChips(Button ativo) {
        List<Button> chips = List.of(btnTodas, btnConfirmadas, btnEmEspera, btnEmConsulta, btnConcluidas);
        for (Button btn : chips) {
            if (btn == null) continue;
            if (btn == ativo) {
                btn.getStyleClass().removeAll("filter-chip");
                if (!btn.getStyleClass().contains("nav-button-active")) {
                    btn.getStyleClass().add("nav-button-active");
                }
                btn.setStyle("-fx-background-color: #e8f4ff; -fx-text-fill: #0066cc;");
            } else {
                btn.getStyleClass().removeAll("nav-button-active");
                if (!btn.getStyleClass().contains("filter-chip")) {
                    btn.getStyleClass().add("filter-chip");
                }
                btn.setStyle(null);
            }
        }
    }

    private String textoEstado(EstadoConsulta estado) {
        if (estado == null) return "-";
        return switch (estado) {
            case AGENDADA -> "Agendada";
            case CONFIRMADA -> "Confirmada";
            case EM_ESPERA -> "Em espera";
            case EM_CONSULTA -> "Em consulta";
            case CONCLUIDA -> "Concluida";
            case CANCELADA -> "Cancelada";
            default -> estado.getDescricao();
        };
    }

    private String classeEstado(EstadoConsulta estado) {
        if (estado == null) return "status-agendado";
        return switch (estado) {
            case AGENDADA -> "status-agendado";
            case CONFIRMADA -> "status-confirmado";
            case EM_ESPERA -> "status-em-espera";
            case EM_CONSULTA -> "status-em-consulta";
            case CONCLUIDA -> "status-concluido";
            case CANCELADA -> "status-cancelado";
            default -> "status-agendado";
        };
    }
}

package controller.dentista;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import bll.ConsultaService;
import bll.DentistaService;
import bll.PacienteService;
import bll.PrescricaoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import model.Consulta;
import model.Paciente;
import model.Prescricao;
import model.Utilizador;
import model.enums.EstadoConsulta;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrescricoesDentistaController extends BaseDentistaController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TextField txtPesquisa;
    @FXML private TableView<Prescricao> tblPrescricoes;
    @FXML private TableColumn<Prescricao, String> colData;
    @FXML private TableColumn<Prescricao, String> colPaciente;
    @FXML private TableColumn<Prescricao, String> colMedicamento;
    @FXML private TableColumn<Prescricao, String> colPosologia;
    @FXML private TableColumn<Prescricao, String> colConsulta;

    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private TextField txtMedicamento;
    @FXML private TextField txtPosologia;
    @FXML private TextField txtTempoTratamento;
    @FXML private TextArea txtObservacoes;
    @FXML private TextField txtAssinatura;

    @Autowired private PrescricaoService prescricaoService;
    @Autowired private PacienteService pacienteService;
    @Autowired private DentistaService dentistaService;
    @Autowired private ConsultaService consultaService;

    private ObservableList<Prescricao> masterPrescricoes = FXCollections.observableArrayList();
    private FilteredList<Prescricao> filteredPrescricoes;

    @Override
    protected void inicializarEcra() {
        txtAssinatura.setText(nomeDentista());
        configurarTabela();
        configurarComboBox();
        carregarDados();
    }

    private void configurarTabela() {
        colData.setCellValueFactory(cell -> {
            LocalDate d = cell.getValue().getData();
            return new SimpleStringProperty(d != null ? d.format(DATE_FMT) : "-");
        });

        colPaciente.setCellValueFactory(cell -> {
            Paciente p = cell.getValue().getPaciente();
            Utilizador u = p != null ? p.getUtilizador() : null;
            return new SimpleStringProperty(u != null ? (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim() : "-");
        });

        colMedicamento.setCellValueFactory(cell -> new SimpleStringProperty(nvl(cell.getValue().getMedicamento())));
        colPosologia.setCellValueFactory(cell -> new SimpleStringProperty(
                nvl(cell.getValue().getPosologia()) + " (" + nvl(cell.getValue().getTempoTratamento()) + ")"
        ));

        colConsulta.setCellValueFactory(cell -> {
            Consulta c = cell.getValue().getConsulta();
            if (c == null) return new SimpleStringProperty("-");
            String dataStr = c.getDataHoraInicio() != null
                    ? LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))
                    : "-";
            return new SimpleStringProperty("#" + c.getId() + " " + dataStr);
        });

        txtPesquisa.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredPrescricoes != null) {
                String term = newVal.toLowerCase().trim();
                filteredPrescricoes.setPredicate(p -> {
                    if (term.isEmpty()) return true;
                    Paciente pac = p.getPaciente();
                    Utilizador u = pac != null ? pac.getUtilizador() : null;
                    if (u == null) return false;
                    String nome = (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).toLowerCase();
                    String nif = nvl(u.getNif()).toLowerCase();
                    return nome.contains(term) || nif.contains(term);
                });
            }
        });
    }

    private void configurarComboBox() {
        cbPaciente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Paciente p) {
                if (p == null) return "";
                Utilizador u = p.getUtilizador();
                return u != null ? (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome()) + " (NIF: " + nvl(u.getNif()) + ")").trim() : "-";
            }

            @Override
            public Paciente fromString(String string) {
                return null;
            }
        });
    }

    private void carregarDados() {
        try {
            // Carregar receitas do dentista logado
            List<Prescricao> prescricoes = prescricaoService.listarPorDentista(dentistaId());
            masterPrescricoes.setAll(prescricoes);
            filteredPrescricoes = new FilteredList<>(masterPrescricoes, p -> true);
            tblPrescricoes.setItems(filteredPrescricoes);

            // Carregar lista de pacientes para o combo
            List<Paciente> pacientes = pacienteService.listarTodos();
            cbPaciente.setItems(FXCollections.observableArrayList(pacientes));
        } catch (Exception e) {
            mostrarErro("Erro ao carregar dados: " + e.getMessage());
        }
    }

    @FXML
    private void emitirPrescricao() {
        Paciente pac = cbPaciente.getValue();
        String med = txtMedicamento.getText();
        String pos = txtPosologia.getText();
        String tempo = txtTempoTratamento.getText();
        String obs = txtObservacoes.getText();
        String ass = txtAssinatura.getText();

        if (pac == null) {
            mostrarErro("Selecione o paciente.");
            return;
        }
        if (med == null || med.isBlank()) {
            mostrarErro("O nome do medicamento é obrigatório.");
            return;
        }
        if (pos == null || pos.isBlank()) {
            mostrarErro("A posologia é obrigatória.");
            return;
        }

        try {
            Prescricao p = new Prescricao();
            p.setPaciente(pac);
            p.setDentista(dentistaLogado());
            p.setMedicamento(med);
            p.setPosologia(pos);
            p.setTempoTratamento(tempo);
            p.setObservacoes(obs);
            p.setAssinatura(ass);
            p.setData(LocalDate.now());

            // Associar à consulta mais recente do paciente com este dentista
            Consulta consultaAssociada = encontrarUltimaConsulta(pac, dentistaId());
            if (consultaAssociada != null) {
                p.setConsulta(consultaAssociada);
            }

            prescricaoService.salvar(p);
            mostrarInfo("Prescrição emitida com sucesso!");

            limparFormulario();
            carregarDados();
        } catch (Exception e) {
            mostrarErro("Erro ao emitir prescrição: " + e.getMessage());
        }
    }

    /**
     * Encontra a consulta mais recente do paciente associada a este dentista.
     */
    private Consulta encontrarUltimaConsulta(Paciente paciente, Integer dentistaId) {
        try {
            List<Consulta> consultas = consultaService.listarPorPaciente(paciente.getId()).stream()
                    .filter(c -> c.getIdDentista() != null && dentistaId.equals(c.getIdDentista().getId()))
                    .filter(c -> c.getStatus() == EstadoConsulta.EM_CONSULTA
                            || c.getStatus() == EstadoConsulta.CONCLUIDA
                            || c.getStatus() == EstadoConsulta.FATURADA)
                    .sorted(Comparator.comparing(Consulta::getDataHoraInicio,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            if (!consultas.isEmpty()) {
                return consultas.get(0);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @FXML
    private void limparFormulario() {
        cbPaciente.setValue(null);
        txtMedicamento.clear();
        txtPosologia.clear();
        txtTempoTratamento.clear();
        txtObservacoes.clear();
        txtAssinatura.setText(nomeDentista());
    }
}
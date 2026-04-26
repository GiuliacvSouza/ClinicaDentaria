package controller;

import bll.ConsultaService;
import bll.DentistaService;
import bll.PacienteService;
import bll.PacientexSeguroService;
import bll.ProcedimentoService;
import bll.SeguroService;
import bll.UtilizadorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import model.Consulta;
import model.Dentista;
import model.Paciente;
import model.PacientexSeguro;
import model.PacientexSeguroId;
import model.Procedimento;
import model.Seguro;
import model.Utilizador;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NovaMarcacaoController {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private TextField txtPesquisaPaciente;
    @FXML private ListView<Paciente> lstPacientesEncontrados;
    @FXML private Label lblPacienteSelecionado;
    @FXML private TextField txtNome;
    @FXML private TextField txtUltimoNome;
    @FXML private TextField txtNif;
    @FXML private TextField txtTelemovel;
    @FXML private ComboBox<Seguro> cbSeguro;
    @FXML private ComboBox<Dentista> cbDentista;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<String> cbHora;
    @FXML private ComboBox<Procedimento> cbProcedimento;
    @FXML private ComboBox<String> cbTipoConsulta;
    @FXML private TextArea txtObservacoes;

    @Autowired private ConsultaService consultaService;
    @Autowired private PacienteService pacienteService;
    @Autowired private DentistaService dentistaService;
    @Autowired private SeguroService seguroService;
    @Autowired private ProcedimentoService procedimentoService;
    @Autowired private UtilizadorService utilizadorService;
    @Autowired private PacientexSeguroService pacientexSeguroService;

    private final ObservableList<Paciente> pacientesEncontrados = FXCollections.observableArrayList();

    private Stage stage;
    private boolean saved;
    private Paciente pacienteSelecionado;
    private boolean preenchendoPacienteSelecionado;

    @FXML
    public void initialize() {
        configurarCampos();
        carregarCombos();
        configurarBuscaPacientes();
        configurarListeners();
        atualizarLabelPacienteSelecionado();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void fecharModal() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void cancelar() {
        fecharModal();
    }

    @FXML
    private void confirmar() {
        try {
            Dentista dentista = cbDentista.getValue();
            Procedimento procedimento = cbProcedimento.getValue();
            String tipoConsulta = cbTipoConsulta.getValue();
            LocalDate data = dpData.getValue();
            String horaSelecionada = cbHora.getValue();

            validarSelecaoAgenda(dentista, procedimento, tipoConsulta, data, horaSelecionada);

            Paciente paciente = resolverPaciente();
            Seguro seguro = cbSeguro.getValue();
            if (seguro != null) {
                garantirAssociacaoSeguro(paciente, seguro);
            }

            LocalTime hora = LocalTime.parse(horaSelecionada, HORA_FORMATTER);
            Consulta consulta = new Consulta();
            consulta.setIdPaciente(paciente);
            consulta.setIdDentista(dentista);
            consulta.setDataHoraInicio(LocalDateTime.of(data, hora).atZone(ZoneId.systemDefault()).toInstant());
            consulta.setDuracao(procedimento.getDuracaoEstimada() != null ? procedimento.getDuracaoEstimada() : 30);
            consulta.setTipo(procedimento.getNome());
            consulta.setStatus(EstadoConsulta.AGENDADA);
            consulta.setDataMarcacao(LocalDate.now());
            consulta.setObservacoes(construirObservacoes(procedimento.getNome(), tipoConsulta, seguro));

            consultaService.agendarConsulta(consulta);
            saved = true;
            fecharModal();
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void configurarCampos() {
        aplicarTextFormatter(txtNome, this::permitirNome);
        aplicarTextFormatter(txtUltimoNome, this::permitirNome);
        aplicarTextFormatter(txtNif, change -> permitirApenasDigitosComLimite(change, 9));
        aplicarTextFormatter(txtTelemovel, change -> permitirApenasDigitosComLimite(change, 9));

        cbHora.setPromptText("Selecione a hora");
        cbHora.setButtonCell(criarCellTexto("Selecione a hora"));
        cbHora.setCellFactory(listView -> criarCellTexto("Selecione a hora"));

        dpData.setPromptText("dd/mm/aaaa");
        dpData.setDayCellFactory(datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item == null || item.isBefore(LocalDate.now()));
            }
        });

        lstPacientesEncontrados.setItems(pacientesEncontrados);
        lstPacientesEncontrados.setVisible(false);
        lstPacientesEncontrados.setManaged(false);
        lstPacientesEncontrados.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                Utilizador utilizador = item.getUtilizador();
                String nome = formatarNome(utilizador);
                String nif = utilizador != null ? valorOuPadrao(utilizador.getNif()) : "-";
                setText(nome + "  |  NIF " + nif);
            }
        });
    }

    private void configurarBuscaPacientes() {
        txtPesquisaPaciente.textProperty().addListener((obs, oldValue, newValue) -> pesquisarPacientes(newValue));

        lstPacientesEncontrados.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selecionarPaciente(newValue);
            }
        });
    }

    private void configurarListeners() {
        cbDentista.valueProperty().addListener((obs, oldValue, newValue) -> atualizarHorariosDisponiveis());
        cbProcedimento.valueProperty().addListener((obs, oldValue, newValue) -> atualizarHorariosDisponiveis());
        dpData.valueProperty().addListener((obs, oldValue, newValue) -> atualizarHorariosDisponiveis());

        txtNome.textProperty().addListener((obs, oldValue, newValue) -> limparPacienteSelecionadoEmEdicaoManual());
        txtUltimoNome.textProperty().addListener((obs, oldValue, newValue) -> limparPacienteSelecionadoEmEdicaoManual());
        txtNif.textProperty().addListener((obs, oldValue, newValue) -> limparPacienteSelecionadoEmEdicaoManual());
        txtTelemovel.textProperty().addListener((obs, oldValue, newValue) -> limparPacienteSelecionadoEmEdicaoManual());
    }

    private void carregarCombos() {
        configurarComboSeguro();
        configurarComboDentista();
        configurarComboProcedimento();
        cbTipoConsulta.setItems(FXCollections.observableArrayList("Consulta Geral", "Avaliacao", "Retorno", "Urgencia"));
    }

    private void configurarComboSeguro() {
        cbSeguro.setItems(FXCollections.observableArrayList(seguroService.listarDisponiveisOuCriarPadrao()));
        cbSeguro.setButtonCell(criarCellSeguro("Selecione um seguro"));
        cbSeguro.setCellFactory(listView -> criarCellSeguro("Selecione um seguro"));
    }

    private void configurarComboDentista() {
        cbDentista.setItems(FXCollections.observableArrayList(dentistaService.listarAtivosOuCriarPadrao()));
        cbDentista.setButtonCell(criarCellDentista("Selecione um dentista"));
        cbDentista.setCellFactory(listView -> criarCellDentista("Selecione um dentista"));
    }

    private void configurarComboProcedimento() {
        cbProcedimento.setItems(FXCollections.observableArrayList(
                procedimentoService.listarTodos().stream()
                        .filter(procedimento -> procedimento.getStatus() == null
                                || procedimento.getStatus().isBlank()
                                || !"INATIVO".equalsIgnoreCase(procedimento.getStatus()))
                        .sorted(Comparator.comparing(procedimento -> valorOuPadrao(procedimento.getNome()), String.CASE_INSENSITIVE_ORDER))
                        .toList()
        ));
        cbProcedimento.setButtonCell(criarCellProcedimento("Selecione o procedimento"));
        cbProcedimento.setCellFactory(listView -> criarCellProcedimento("Selecione o procedimento"));
    }

    private void pesquisarPacientes(String termo) {
        if (termo == null || termo.isBlank()) {
            pacientesEncontrados.clear();
            lstPacientesEncontrados.getSelectionModel().clearSelection();
            lstPacientesEncontrados.setVisible(false);
            lstPacientesEncontrados.setManaged(false);
            return;
        }

        List<Paciente> resultados = pacienteService.pesquisarPorNomeOuNif(termo).stream()
                .sorted(Comparator.comparing(paciente -> valorOuPadrao(formatarNome(paciente.getUtilizador())), String.CASE_INSENSITIVE_ORDER))
                .limit(8)
                .toList();

        pacientesEncontrados.setAll(resultados);
        boolean possuiResultados = !resultados.isEmpty();
        lstPacientesEncontrados.setVisible(possuiResultados);
        lstPacientesEncontrados.setManaged(possuiResultados);
    }

    private void selecionarPaciente(Paciente paciente) {
        preenchendoPacienteSelecionado = true;
        pacienteSelecionado = paciente;

        Utilizador utilizador = paciente.getUtilizador();
        txtNome.setText(utilizador != null ? valorVazio(utilizador.getPrimeiroNome()) : "");
        txtUltimoNome.setText(utilizador != null ? valorVazio(utilizador.getUltimoNome()) : "");
        txtNif.setText(utilizador != null ? valorVazio(utilizador.getNif()) : "");
        txtTelemovel.setText(utilizador != null ? valorVazio(utilizador.getTelemovel()) : "");
        txtPesquisaPaciente.setText(formatarNome(utilizador));

        lstPacientesEncontrados.setVisible(false);
        lstPacientesEncontrados.setManaged(false);
        lstPacientesEncontrados.getSelectionModel().clearSelection();

        preenchendoPacienteSelecionado = false;
        atualizarLabelPacienteSelecionado();
    }

    private void limparPacienteSelecionadoEmEdicaoManual() {
        if (preenchendoPacienteSelecionado || pacienteSelecionado == null) {
            return;
        }

        Utilizador utilizador = pacienteSelecionado.getUtilizador();
        boolean mesmosDados = Objects.equals(valorVazio(utilizador.getPrimeiroNome()), valorVazio(txtNome.getText()))
                && Objects.equals(valorVazio(utilizador.getUltimoNome()), valorVazio(txtUltimoNome.getText()))
                && Objects.equals(valorVazio(utilizador.getNif()), valorVazio(txtNif.getText()))
                && Objects.equals(valorVazio(utilizador.getTelemovel()), valorVazio(txtTelemovel.getText()));

        if (!mesmosDados) {
            pacienteSelecionado = null;
            atualizarLabelPacienteSelecionado();
        }
    }

    private void atualizarLabelPacienteSelecionado() {
        if (pacienteSelecionado == null) {
            lblPacienteSelecionado.setText("Nao encontrou o paciente? Preencha os campos abaixo para registar e marcar.");
            return;
        }

        lblPacienteSelecionado.setText("Paciente selecionado: " + formatarNome(pacienteSelecionado.getUtilizador()));
    }

    private Paciente resolverPaciente() {
        if (pacienteSelecionado != null && pacienteSelecionado.getId() != null) {
            return pacienteService.buscarPorId(pacienteSelecionado.getId());
        }

        validarCamposPaciente();

        String nif = txtNif.getText().trim();
        Paciente existente = pacienteService.buscarPorNifOuNull(nif);
        if (existente != null) {
            return existente;
        }

        Utilizador utilizador = new Utilizador();
        utilizador.setPrimeiroNome(txtNome.getText().trim());
        utilizador.setUltimoNome(txtUltimoNome.getText().trim());
        utilizador.setTipoUtilizador("PACIENTE");
        utilizador.setEmail("paciente." + nif + "@clinica.pt");
        utilizador.setNif(nif);
        utilizador.setTelemovel(txtTelemovel.getText().trim());
        utilizador.setSenha("Paciente" + nif + "!");
        utilizador.setStatus("ATIVO");

        Utilizador utilizadorGuardado = utilizadorService.salvar(utilizador);

        Paciente paciente = new Paciente();
        paciente.setUtilizador(utilizadorGuardado);
        paciente.setStatus("ATIVO");
        paciente.setDataRegisto(LocalDate.now());

        Paciente pacienteGuardado = pacienteService.salvar(paciente);
        pacienteSelecionado = pacienteGuardado;
        atualizarLabelPacienteSelecionado();
        return pacienteGuardado;
    }

    private void garantirAssociacaoSeguro(Paciente paciente, Seguro seguro) {
        boolean jaAssociado = pacientexSeguroService.listarTodos().stream()
                .anyMatch(ps -> ps.getIdUtilizador() != null
                        && ps.getIdSeguro() != null
                        && Objects.equals(ps.getIdUtilizador().getId(), paciente.getId())
                        && Objects.equals(ps.getIdSeguro().getId(), seguro.getId()));

        if (jaAssociado) {
            return;
        }

        PacientexSeguro associacao = new PacientexSeguro();
        PacientexSeguroId id = new PacientexSeguroId();
        id.setIdUtilizador(paciente.getId());
        id.setIdSeguro(seguro.getId());

        associacao.setId(id);
        associacao.setIdUtilizador(paciente);
        associacao.setIdSeguro(seguro);
        associacao.setNumeroApolice("AUTO-" + paciente.getId() + "-" + seguro.getId());
        associacao.setDataInicioCobertura(LocalDate.now());
        associacao.setDataFimCobertura(seguro.getValidoAte() != null ? seguro.getValidoAte() : LocalDate.now().plusYears(1));
        pacientexSeguroService.salvar(associacao);
    }

    private void atualizarHorariosDisponiveis() {
        Dentista dentista = cbDentista.getValue();
        Procedimento procedimento = cbProcedimento.getValue();
        LocalDate data = dpData.getValue();

        String selecionadoAtual = cbHora.getValue();
        cbHora.getItems().clear();
        cbHora.getSelectionModel().clearSelection();

        if (dentista == null || procedimento == null || data == null) {
            return;
        }

        LocalTime inicio = dentista.getHorarioEntrada() != null ? dentista.getHorarioEntrada() : LocalTime.of(9, 0);
        LocalTime fim = dentista.getHorarioSaida() != null ? dentista.getHorarioSaida() : LocalTime.of(18, 0);
        int duracao = procedimento.getDuracaoEstimada() != null ? procedimento.getDuracaoEstimada() : 30;

        if (!fim.isAfter(inicio)) {
            return;
        }

        LocalTime primeiroHorario = ajustarInicioParaHoje(data, inicio);
        List<Consulta> consultasDoDia = consultaService.listarPorDentistaEDia(dentista.getId(), data).stream()
                .filter(consulta -> consulta.getStatus() != EstadoConsulta.CANCELADA)
                .toList();

        ObservableList<String> horariosDisponiveis = FXCollections.observableArrayList();
        for (LocalTime slot = primeiroHorario; !slot.plusMinutes(duracao).isAfter(fim); slot = slot.plusMinutes(30)) {
            LocalTime inicioSlot = slot;
            LocalTime fimSlot = inicioSlot.plusMinutes(duracao);
            boolean ocupado = consultasDoDia.stream().anyMatch(consulta -> conflita(inicioSlot, fimSlot, consulta));
            if (!ocupado) {
                horariosDisponiveis.add(inicioSlot.format(HORA_FORMATTER));
            }
        }

        cbHora.setItems(horariosDisponiveis);
        if (selecionadoAtual != null && horariosDisponiveis.contains(selecionadoAtual)) {
            cbHora.setValue(selecionadoAtual);
        }
    }

    private LocalTime ajustarInicioParaHoje(LocalDate data, LocalTime inicioPadrao) {
        if (!LocalDate.now().equals(data)) {
            return inicioPadrao;
        }

        LocalTime agora = LocalTime.now().plusMinutes(15);
        int minutoArredondado = ((agora.getMinute() + 29) / 30) * 30;
        LocalTime arredondado = agora.withMinute(0).withSecond(0).withNano(0);
        if (minutoArredondado >= 60) {
            arredondado = arredondado.plusHours(1);
            minutoArredondado = 0;
        }
        arredondado = arredondado.withMinute(minutoArredondado);
        return arredondado.isAfter(inicioPadrao) ? arredondado : inicioPadrao;
    }

    private boolean conflita(LocalTime inicioSlot, LocalTime fimSlot, Consulta consulta) {
        if (consulta.getDataHoraInicio() == null) {
            return false;
        }

        LocalTime inicioExistente = consulta.getDataHoraInicio()
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
                .withSecond(0)
                .withNano(0);
        int duracaoExistente = consulta.getDuracao() != null ? consulta.getDuracao() : 30;
        LocalTime fimExistente = inicioExistente.plusMinutes(duracaoExistente);
        return inicioSlot.isBefore(fimExistente) && fimSlot.isAfter(inicioExistente);
    }

    private void validarSelecaoAgenda(Dentista dentista, Procedimento procedimento, String tipoConsulta, LocalDate data, String horaSelecionada) {
        if (dentista == null) {
            throw new RuntimeException("Dentista é obrigatório.");
        }
        if (procedimento == null) {
            throw new RuntimeException("Procedimento é obrigatório.");
        }
        if (tipoConsulta == null || tipoConsulta.isBlank()) {
            throw new RuntimeException("Tipo de consulta é obrigatório.");
        }
        if (data == null) {
            throw new RuntimeException("Data é obrigatória.");
        }
        if (data.isBefore(LocalDate.now())) {
            throw new RuntimeException("Data inválida. Escolha uma data atual ou futura.");
        }
        if (horaSelecionada == null || horaSelecionada.isBlank()) {
            throw new RuntimeException("Hora é obrigatória.");
        }
    }

    private void validarCamposPaciente() {
        if (txtNome.getText() == null || txtNome.getText().isBlank()) {
            throw new RuntimeException("Nome do paciente é obrigatório.");
        }
        if (txtUltimoNome.getText() == null || txtUltimoNome.getText().isBlank()) {
            throw new RuntimeException("Apelido do paciente é obrigatório.");
        }
        if (txtNif.getText() == null || !txtNif.getText().matches("\\d{9}")) {
            throw new RuntimeException("NIF deve conter exatamente 9 dígitos.");
        }
        if (txtTelemovel.getText() == null || !txtTelemovel.getText().matches("\\d{9}")) {
            throw new RuntimeException("Telemóvel deve conter exatamente 9 dígitos.");
        }
    }

    private String construirObservacoes(String nomeProcedimento, String tipoConsulta, Seguro seguro) {
        StringBuilder builder = new StringBuilder();
        builder.append("Procedimento: ").append(nomeProcedimento);

        if (tipoConsulta != null && !tipoConsulta.isBlank()) {
            builder.append("\nTipo de consulta: ").append(tipoConsulta);
        }
        if (seguro != null && seguro.getNomeSeguro() != null && !seguro.getNomeSeguro().isBlank()) {
            builder.append("\nSeguro: ").append(seguro.getNomeSeguro());
        }
        if (txtObservacoes.getText() != null && !txtObservacoes.getText().isBlank()) {
            builder.append("\n").append(txtObservacoes.getText().trim());
        }
        return builder.toString();
    }

    private ListCell<Seguro> criarCellSeguro(String prompt) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Seguro item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item == null ? prompt : valorOuPadrao(item.getNomeSeguro()));
            }
        };
    }

    private ListCell<Dentista> criarCellDentista(String prompt) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Dentista item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item == null ? prompt : valorOuPadrao(formatarNome(item.getUtilizador())));
            }
        };
    }

    private ListCell<Procedimento> criarCellProcedimento(String prompt) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Procedimento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item == null ? prompt : valorOuPadrao(item.getNome()));
            }
        };
    }

    private ListCell<String> criarCellTexto(String prompt) {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item == null ? prompt : item);
            }
        };
    }

    private void aplicarTextFormatter(TextField field, UnaryOperator<TextFormatter.Change> filter) {
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    private TextFormatter.Change permitirNome(TextFormatter.Change change) {
        String novoTexto = change.getControlNewText();
        if (novoTexto.length() > 100) {
            return null;
        }
        if (novoTexto.matches("[\\p{L}\\s'`.-]*")) {
            return change;
        }
        return null;
    }

    private TextFormatter.Change permitirApenasDigitosComLimite(TextFormatter.Change change, int limite) {
        String novoTexto = change.getControlNewText();
        if (novoTexto.matches("\\d{0," + limite + "}")) {
            return change;
        }
        return null;
    }

    private String formatarNome(Utilizador utilizador) {
        if (utilizador == null) {
            return null;
        }
        String primeiroNome = utilizador.getPrimeiroNome() != null ? utilizador.getPrimeiroNome().trim() : "";
        String ultimoNome = utilizador.getUltimoNome() != null ? utilizador.getUltimoNome().trim() : "";
        String nomeCompleto = (primeiroNome + " " + ultimoNome).trim();
        return nomeCompleto.isEmpty() ? null : nomeCompleto;
    }

    private String valorOuPadrao(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private String valorVazio(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem != null && !mensagem.isBlank() ? mensagem : "Não foi possível concluir a operação.");
        alert.showAndWait();
    }
}

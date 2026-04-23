package controller;

import bll.ConsultaService;
import bll.DentistaService;
import bll.PacienteService;
import bll.ProcedimentoService;
import bll.SeguroService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Consulta;
import model.Dentista;
import model.Paciente;
import model.Procedimento;
import model.Seguro;
import model.Utilizador;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

@Component
public class NovaMarcacaoController {

    @FXML private TextField txtNome;
    @FXML private TextField txtUltimoNome;
    @FXML private TextField txtNif;
    @FXML private TextField txtTelemovel;
    @FXML private ComboBox<Seguro> cbSeguro;
    @FXML private ComboBox<Dentista> cbDentista;
    @FXML private DatePicker dpData;
    @FXML private TextField txtHora;
    @FXML private ComboBox<Procedimento> cbProcedimento;
    @FXML private ComboBox<String> cbTipoConsulta;
    @FXML private TextArea txtObservacoes;

    @Autowired private ConsultaService consultaService;
    @Autowired private PacienteService pacienteService;
    @Autowired private DentistaService dentistaService;
    @Autowired private SeguroService seguroService;
    @Autowired private ProcedimentoService procedimentoService;

    private Stage stage;
    private boolean saved;

    @FXML
    public void initialize() {
        txtHora.setPromptText("--:--");
        dpData.setPromptText("dd/mm/aaaa");
        carregarCombos();
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
            Paciente paciente = encontrarPaciente();
            Dentista dentista = cbDentista.getValue();
            Procedimento procedimento = cbProcedimento.getValue();
            String tipoConsulta = cbTipoConsulta.getValue();
            LocalDate data = dpData.getValue();
            LocalTime hora = parseHoraObrigatoria(txtHora.getText());

            validarCampos(paciente, dentista, procedimento, tipoConsulta, data);

            Consulta consulta = new Consulta();
            consulta.setIdPaciente(paciente);
            consulta.setIdDentista(dentista);
            consulta.setDataHoraInicio(LocalDateTime.of(data, hora).atZone(ZoneId.systemDefault()).toInstant());
            consulta.setDuracao(procedimento.getDuracaoEstimada() != null ? procedimento.getDuracaoEstimada() : 30);
            consulta.setTipo(procedimento.getNome());
            consulta.setStatus(EstadoConsulta.AGENDADA);
            consulta.setDataMarcacao(LocalDate.now());
            consulta.setObservacoes(construirObservacoes());

            consultaService.agendarConsulta(consulta);
            saved = true;
            fecharModal();
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void carregarCombos() {
        configurarComboSeguro();
        configurarComboDentista();
        configurarComboProcedimento();
        cbTipoConsulta.setItems(FXCollections.observableArrayList("Consulta Geral", "Avaliação", "Retorno", "Urgência"));
    }

    private void configurarComboSeguro() {
        cbSeguro.setItems(FXCollections.observableArrayList(
                seguroService.listarTodos().stream()
                        .sorted(Comparator.comparing(seguro -> valorOuPadrao(seguro.getNomeSeguro()), String.CASE_INSENSITIVE_ORDER))
                        .toList()
        ));
        cbSeguro.setButtonCell(criarCellSeguro("Selecione um seguro"));
        cbSeguro.setCellFactory(listView -> criarCellSeguro("Selecione um seguro"));
    }

    private void configurarComboDentista() {
        cbDentista.setItems(FXCollections.observableArrayList(
                dentistaService.listarTodos().stream()
                        .filter(dentista -> dentista.getAtivo() == null || dentista.getAtivo())
                        .sorted(Comparator.comparing(dentista -> valorOuPadrao(formatarNome(dentista.getUtilizador())), String.CASE_INSENSITIVE_ORDER))
                        .toList()
        ));
        cbDentista.setButtonCell(criarCellDentista("Selecione um dentista"));
        cbDentista.setCellFactory(listView -> criarCellDentista("Selecione um dentista"));
    }

    private void configurarComboProcedimento() {
        cbProcedimento.setItems(FXCollections.observableArrayList(
                procedimentoService.listarTodos().stream()
                        .sorted(Comparator.comparing(procedimento -> valorOuPadrao(procedimento.getNome()), String.CASE_INSENSITIVE_ORDER))
                        .toList()
        ));
        cbProcedimento.setButtonCell(criarCellProcedimento("Selecione o procedimento"));
        cbProcedimento.setCellFactory(listView -> criarCellProcedimento("Selecione o procedimento"));
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

    private Paciente encontrarPaciente() {
        String nome = normalizar(txtNome.getText());
        String ultimoNome = normalizar(txtUltimoNome.getText());
        String nif = normalizar(txtNif.getText());
        String telemovel = normalizar(txtTelemovel.getText());

        return pacienteService.listarTodos().stream()
                .filter(paciente -> correspondePaciente(paciente, nome, ultimoNome, nif, telemovel))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado com os dados informados."));
    }

    private boolean correspondePaciente(Paciente paciente, String nome, String ultimoNome, String nif, String telemovel) {
        Utilizador utilizador = paciente != null ? paciente.getUtilizador() : null;
        if (utilizador == null) {
            return false;
        }

        boolean porNome = !nome.isBlank() && !ultimoNome.isBlank()
                && normalizar(utilizador.getPrimeiroNome()).equals(nome)
                && normalizar(utilizador.getUltimoNome()).equals(ultimoNome);
        boolean porNif = !nif.isBlank() && normalizar(utilizador.getNif()).equals(nif);
        boolean porTelemovel = !telemovel.isBlank() && normalizar(utilizador.getTelemovel()).equals(telemovel);
        return porNome || porNif || porTelemovel;
    }

    private void validarCampos(Paciente paciente, Dentista dentista, Procedimento procedimento, String tipoConsulta, LocalDate data) {
        if (paciente == null) {
            throw new RuntimeException("Paciente é obrigatório.");
        }
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
    }

    private LocalTime parseHoraObrigatoria(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RuntimeException("Hora é obrigatória.");
        }

        try {
            return LocalTime.parse(valor.trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Hora inválida. Use o formato HH:mm.");
        }
    }

    private String construirObservacoes() {
        StringBuilder builder = new StringBuilder();
        if (cbTipoConsulta.getValue() != null && !cbTipoConsulta.getValue().isBlank()) {
            builder.append("Tipo de consulta: ").append(cbTipoConsulta.getValue());
        }
        if (cbSeguro.getValue() != null && cbSeguro.getValue().getNomeSeguro() != null && !cbSeguro.getValue().getNomeSeguro().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append("\n");
            }
            builder.append("Seguro: ").append(cbSeguro.getValue().getNomeSeguro());
        }
        if (txtObservacoes.getText() != null && !txtObservacoes.getText().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append("\n");
            }
            builder.append(txtObservacoes.getText().trim());
        }
        return builder.toString();
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

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem != null && !mensagem.isBlank() ? mensagem : "Não foi possível concluir a operação.");
        alert.showAndWait();
    }
}

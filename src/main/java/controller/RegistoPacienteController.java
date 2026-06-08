package controller;

import bll.PacienteService;
import bll.PacientexSeguroService;
import bll.SeguroService;
import bll.UtilizadorService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Paciente;
import model.PacientexSeguro;
import model.PacientexSeguroId;
import model.Seguro;
import model.Utilizador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RegistoPacienteController {

    @FXML private TextField txtNome;
    @FXML private TextField txtApelido;
    @FXML private TextField txtNif;
    @FXML private TextField txtTelemovel;
    @FXML private TextField txtEmail;
    @FXML private DatePicker dpDataNascimento;
    @FXML private ComboBox<Seguro> cbSeguro;

    @Autowired private UtilizadorService utilizadorService;
    @Autowired private PacienteService pacienteService;
    @Autowired private SeguroService seguroService;
    @Autowired private PacientexSeguroService pacientexSeguroService;

    private Stage stage;
    private boolean saved;

    @FXML
    public void initialize() {
        if (dpDataNascimento != null) {
            // Garantir que o promptText é aplicado também no editor (TextField interno)
            // e configurar o formato de apresentação para dd/MM/yyyy (PT-PT)
            configurarDatePickerPT(dpDataNascimento);
        }
        configurarComboSeguro();
    }

    /**
     * Aplica formatação e promptText consistentes ao DatePicker,
     * garantindo que o campo de data segue o mesmo design das restantes datas da aplicação.
     */
    private void configurarDatePickerPT(DatePicker datePicker) {
        if (datePicker == null) {
            return;
        }
        String pattern = "dd/MM/yyyy";
        datePicker.setPromptText(pattern);
        // Forçar o promptText a aparecer também no editor (TextField interno)
        if (datePicker.getEditor() != null) {
            datePicker.getEditor().setPromptText(pattern);
        }
        // Converter valor <-> texto em PT-PT (dd/MM/yyyy)
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(pattern);
        StringConverter<java.time.LocalDate> converter = new StringConverter<>() {
            @Override
            public String toString(java.time.LocalDate date) {
                return date == null ? "" : date.format(formatter);
            }

            @Override
            public java.time.LocalDate fromString(String string) {
                if (string == null || string.isBlank()) {
                    return null;
                }
                try {
                    return java.time.LocalDate.parse(string.trim(), formatter);
                } catch (java.time.format.DateTimeParseException ex) {
                    return null;
                }
            }
        };
        datePicker.setConverter(converter);
        if (datePicker.getEditor() != null) {
            datePicker.getEditor().setTextFormatter(
                new javafx.scene.control.TextFormatter<>(converter, null, change -> {
                    String novo = change.getControlNewText();
                    // Permitir apenas dígitos e barras, com tamanho máximo 10 (dd/MM/yyyy)
                    if (novo.length() > 10) {
                        return null;
                    }
                    if (!novo.matches("[0-9/]*")) {
                        return null;
                    }
                    return change;
                })
            );
        }
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
    private void registarPaciente() {
        try {
            String nome = normalizar(txtNome.getText());
            String apelido = normalizar(txtApelido.getText());
            String nif = normalizar(txtNif.getText());
            String telemovel = normalizar(txtTelemovel.getText());
            String email = normalizar(txtEmail.getText());
            LocalDate dataNascimento = dpDataNascimento.getValue();
            Seguro seguroSelecionado = cbSeguro.getValue();

            validarCamposObrigatorios(nome, apelido, nif, telemovel, dataNascimento);

            Utilizador utilizador = new Utilizador();
            utilizador.setPrimeiroNome(nome);
            utilizador.setUltimoNome(apelido);
            utilizador.setNif(nif);
            utilizador.setTelemovel(telemovel);
            utilizador.setEmail(email.isBlank() ? gerarEmailPlaceholder(nif) : email);
            utilizador.setDataNascimento(dataNascimento);
            utilizador.setTipoUtilizador("PACIENTE");
            utilizador.setStatus("ATIVO");
            utilizador.setSenha("Clinica2025!");

            Utilizador utilizadorGuardado = utilizadorService.salvar(utilizador);

            Paciente paciente = new Paciente();
            paciente.setUtilizador(utilizadorGuardado);
            paciente.setStatus("ATIVO");
            paciente.setDataRegisto(LocalDate.now());
            Paciente pacienteGuardado = pacienteService.salvar(paciente);

            if (seguroSelecionado != null && seguroSelecionado.getId() != null) {
                PacientexSeguroId id = new PacientexSeguroId();
                id.setIdUtilizador(pacienteGuardado.getId());
                id.setIdSeguro(seguroSelecionado.getId());

                PacientexSeguro relacao = new PacientexSeguro();
                relacao.setId(id);
                relacao.setIdUtilizador(pacienteGuardado);
                relacao.setIdSeguro(seguroSelecionado);
                relacao.setDataInicioCobertura(LocalDate.now());
                pacientexSeguroService.salvar(relacao);
            }

            saved = true;
            fecharModal();
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void configurarComboSeguro() {
        cbSeguro.setItems(FXCollections.observableArrayList(
                seguroService.listarTodos().stream()
                        .sorted(Comparator.comparing(seguro -> valorOuPadrao(seguro.getNomeSeguro()), String.CASE_INSENSITIVE_ORDER))
                        .toList()
        ));
        cbSeguro.setButtonCell(criarCellSeguro("Selecione o seguro"));
        cbSeguro.setCellFactory(listView -> criarCellSeguro("Selecione o seguro"));
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

    private void validarCamposObrigatorios(String nome, String apelido, String nif, String telemovel, LocalDate dataNascimento) {
        if (nome.isBlank()) {
            throw new RuntimeException("Nome é obrigatório.");
        }
        if (apelido.isBlank()) {
            throw new RuntimeException("Apelido é obrigatório.");
        }
        if (!nif.matches("\\d{9}")) {
            throw new RuntimeException("NIF inválido. Informe 9 dígitos.");
        }
        if (!telemovel.matches("\\d{9}")) {
            throw new RuntimeException("Telemóvel inválido. Informe 9 dígitos.");
        }
        if (dataNascimento == null) {
            throw new RuntimeException("Data de nascimento é obrigatória.");
        }
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new RuntimeException("Data de nascimento não pode ser futura.");
        }
    }

    private String gerarEmailPlaceholder(String nif) {
        return "paciente." + nif + "." + System.nanoTime() + "@clinica.local";
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String valorOuPadrao(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Não foi possível registar o paciente");
        alert.setContentText(mensagem == null || mensagem.isBlank() ? "Ocorreu um erro inesperado." : mensagem);
        alert.showAndWait();
    }
}

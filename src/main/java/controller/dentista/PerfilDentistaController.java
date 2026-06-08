package controller.dentista;

import app.SessionContext;
import bll.UtilizadorService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import model.Dentista;
import model.Utilizador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PerfilDentistaController extends BaseDentistaController {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CAMPO_INVALIDO = "campo-invalido";

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private Label     lblIniciais;
    @FXML private Label     lblNomeCompleto;
    @FXML private Label     lblEmail;
    @FXML private Label     lblTelefoneResumo;
    @FXML private Label     lblEstadoConta;
    @FXML private Label     lblDataAdmissao;
    @FXML private Label     lblNumeroOmd;
    @FXML private Button    btnEditar;
    @FXML private Button    btnGuardar;
    @FXML private Button    btnCancelar;
    @FXML private TextField txtPrimeiroNome;
    @FXML private TextField txtApelido;
    @FXML private TextField txtNif;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelemovel;
    @FXML private TextField txtTelefone;

    // ─── Estado ───────────────────────────────────────────────────────────────

    @Autowired private UtilizadorService utilizadorService;

    private boolean modoEdicao = false;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @Override
    protected void inicializarEcra() {
        aplicarFiltroNumerico(txtNif, 9);
        aplicarFiltroNumerico(txtTelemovel, 9);
        aplicarFiltroNumerico(txtTelefone, 9);
        configurarLimpezaErro();
        preencherPerfil();
        atualizarModoEdicao(false);
    }

    private void preencherPerfil() {
        Utilizador u = SessionContext.getUtilizadorLogado();
        Dentista d = SessionContext.getDentistaLogado();

        if (u == null) return;

        String primeiroNome = u.getPrimeiroNome() != null ? u.getPrimeiroNome().trim() : "";
        String ultimoNome   = u.getUltimoNome()   != null ? u.getUltimoNome().trim()   : "";
        String nomeCompleto = (primeiroNome + " " + ultimoNome).trim();

        // Iniciais no avatar
        String inicialA = primeiroNome.isEmpty() ? "" : primeiroNome.substring(0, 1).toUpperCase();
        String inicialB = ultimoNome.isEmpty()   ? "" : ultimoNome.substring(0, 1).toUpperCase();
        if (lblIniciais    != null) lblIniciais.setText((inicialA + inicialB).isBlank() ? "--" : inicialA + inicialB);
        if (lblNomeCompleto != null) lblNomeCompleto.setText(nomeCompleto.isBlank() ? "Médico Dentista" : nomeCompleto);
        if (lblEmail       != null) lblEmail.setText(u.getEmail() != null ? u.getEmail() : "-");
        if (lblTelefoneResumo != null) lblTelefoneResumo.setText(telefonePreferencial(u));
        if (lblEstadoConta != null) lblEstadoConta.setText(u.getStatus() != null && !u.getStatus().isBlank()
                ? u.getStatus().trim()
                : "-");

        if (d != null) {
            if (lblDataAdmissao != null)
                lblDataAdmissao.setText(d.getDataAdmissao() != null ? d.getDataAdmissao().format(DATA_FMT) : "-");
            if (lblNumeroOmd != null)
                lblNumeroOmd.setText(d.getNumeroOmd() != null && !d.getNumeroOmd().isBlank() ? d.getNumeroOmd() : "-");
        }

        // Preencher campos do formulário
        if (txtPrimeiroNome != null) txtPrimeiroNome.setText(primeiroNome);
        if (txtApelido      != null) txtApelido.setText(ultimoNome);
        if (txtNif          != null) txtNif.setText(u.getNif() != null ? u.getNif() : "");
        if (txtEmail        != null) txtEmail.setText(u.getEmail() != null ? u.getEmail() : "");
        if (txtTelemovel    != null) txtTelemovel.setText(u.getTelemovel() != null ? u.getTelemovel() : "");
        if (txtTelefone     != null) txtTelefone.setText(u.getTelefone() != null ? u.getTelefone() : "");
    }

    // ─── Edição ───────────────────────────────────────────────────────────────

    @FXML
    private void ativarEdicao() {
        atualizarModoEdicao(true);
    }

    @FXML
    private void cancelarEdicao() {
        preencherPerfil();
        atualizarModoEdicao(false);
    }

    @FXML
    private void guardarEdicao() {
        Utilizador u = SessionContext.getUtilizadorLogado();
        if (u == null) return;

        try {
            limparCamposInvalidos();
            validar();

            u.setPrimeiroNome(txtPrimeiroNome.getText().trim());
            u.setUltimoNome(txtApelido.getText().trim());
            u.setNif(valorOuNull(txtNif.getText()));
            u.setEmail(txtEmail.getText().trim());
            u.setTelemovel(valorOuNull(txtTelemovel.getText()));
            u.setTelefone(valorOuNull(txtTelefone.getText()));

            utilizadorService.salvar(u);
            preencherPerfil();
            atualizarModoEdicao(false);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Perfil atualizado");
            ok.setHeaderText(null);
            ok.setContentText("Os seus dados foram guardados com sucesso.");
            ok.showAndWait();

        } catch (RuntimeException ex) {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Erro");
            erro.setHeaderText(null);
            erro.setContentText(ex.getMessage());
            erro.showAndWait();
        }
    }

    private void validar() {
        if (txtPrimeiroNome.getText() == null || txtPrimeiroNome.getText().isBlank())
            throw new RuntimeException("Primeiro nome é obrigatório.");
        if (txtApelido.getText() == null || txtApelido.getText().isBlank())
            throw new RuntimeException("Apelido é obrigatório.");
        if (txtEmail.getText() == null || txtEmail.getText().isBlank() || !txtEmail.getText().contains("@"))
            throw new RuntimeException("Email inválido.");
        validarCampoNumerico(txtNif, false, "O NIF deve conter exatamente 9 dígitos.");
        validarCampoNumerico(txtTelemovel, true, "O telemóvel deve conter 9 dígitos.");
        validarCampoNumerico(txtTelefone, false, "O telefone deve conter 9 dígitos.");
    }

    private void atualizarModoEdicao(boolean ativo) {
        modoEdicao = ativo;

        java.util.List<TextField> campos = java.util.List.of(
                txtPrimeiroNome, txtApelido, txtNif, txtEmail, txtTelemovel, txtTelefone);

        for (TextField f : campos) {
            if (f != null) f.setDisable(!ativo);
        }

        if (btnEditar  != null) { btnEditar.setVisible(!ativo);  btnEditar.setManaged(!ativo); }
        if (btnGuardar != null) { btnGuardar.setVisible(ativo);  btnGuardar.setManaged(ativo); }
        if (btnCancelar != null){ btnCancelar.setVisible(ativo); btnCancelar.setManaged(ativo); }
    }

    private String valorOuNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void aplicarFiltroNumerico(TextField campo, int limite) {
        if (campo == null) return;

        campo.setTextFormatter(new TextFormatter<String>(change -> {
            String novoTexto = change.getControlNewText();
            return novoTexto.matches("\\d{0," + limite + "}") ? change : null;
        }));
    }

    private void configurarLimpezaErro() {
        configurarLimpezaErro(txtNif, false);
        configurarLimpezaErro(txtTelemovel, true);
        configurarLimpezaErro(txtTelefone, false);
    }

    private void configurarLimpezaErro(TextField campo, boolean obrigatorio) {
        if (campo == null) return;

        campo.textProperty().addListener((obs, antigo, novo) -> {
            if (campoNumericoValido(novo, obrigatorio)) {
                removerCampoInvalido(campo);
            }
        });
    }

    private void validarCampoNumerico(TextField campo, boolean obrigatorio, String mensagem) {
        String valor = campo != null ? campo.getText() : null;
        if (!campoNumericoValido(valor, obrigatorio)) {
            marcarCampoInvalido(campo);
            throw new RuntimeException(mensagem);
        }
    }

    private boolean campoNumericoValido(String valor, boolean obrigatorio) {
        if (valor == null || valor.isBlank()) {
            return !obrigatorio;
        }
        return valor.matches("\\d{9}");
    }

    private void limparCamposInvalidos() {
        removerCampoInvalido(txtNif);
        removerCampoInvalido(txtTelemovel);
        removerCampoInvalido(txtTelefone);
    }

    private void marcarCampoInvalido(TextField campo) {
        if (campo != null && !campo.getStyleClass().contains(CAMPO_INVALIDO)) {
            campo.getStyleClass().add(CAMPO_INVALIDO);
        }
    }

    private void removerCampoInvalido(TextField campo) {
        if (campo != null) {
            campo.getStyleClass().remove(CAMPO_INVALIDO);
        }
    }

    private String telefonePreferencial(Utilizador u) {
        if (u.getTelemovel() != null && !u.getTelemovel().isBlank()) return u.getTelemovel().trim();
        if (u.getTelefone()  != null && !u.getTelefone().isBlank())  return u.getTelefone().trim();
        return "-";
    }
}

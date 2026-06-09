package controller;

import app.SceneManager;
import app.SessionContext;
import bll.PacienteService;
import bll.ProntuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Paciente;
import model.Prontuario;
import model.Utilizador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Controller da página de Prontuário do paciente (vista Rececionista).
 * Apresenta e permite editar os dados clínicos registados no prontuário.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProntuarioController {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ── Header ─────────────────────────────── */
    @FXML private Label nomeUtilizador;

    /* ── Cabeçalho do paciente ───────────────── */
    @FXML private Label lblPacienteNome;
    @FXML private Label lblPacienteMeta;
    @FXML private Label lblDataCriacao;
    @FXML private Label lblUltimaAtualizacao;

    /* ── Campos do prontuário ────────────────── */
    @FXML private TextField txtGrupoSanguineo;
    @FXML private TextArea  txtHistoricoMedico;
    @FXML private TextArea  txtAlergias;
    @FXML private TextArea  txtMedicamentosUso;
    @FXML private TextArea  txtHistoricoOdontologico;
    @FXML private TextArea  txtObservacoesClinicas;
    @FXML private TextArea  txtObservacoes;

    /* ── Botões ──────────────────────────────── */
    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    @Autowired private ProntuarioService prontuarioService;
    @Autowired private PacienteService    pacienteService;

    private Integer pacienteId;
    private Prontuario prontuarioAtual;
    private boolean modoEdicao = false;

    /* ─────────────────────────────────────────────── */

    @FXML
    public void initialize() {
        Utilizador utilizadorLogado = SessionContext.getUtilizadorLogado();
        if (utilizadorLogado != null && nomeUtilizador != null) {
            nomeUtilizador.setText(formatarNome(utilizadorLogado));
        }
        atualizarModoEdicao(false);
    }

    /**
     * Ponto de entrada: definir o paciente cujo prontuário será exibido.
     * Chamado externamente (ex: PacientePerfilController) após carregar o FXML.
     */
    public void setPacienteId(Integer pacienteId) {
        this.pacienteId = pacienteId;
        carregarProntuario();
    }

    /* ─── Carregamento ───────────────────────── */

    private void carregarProntuario() {
        if (pacienteId == null) {
            mostrarErro("Paciente não identificado.");
            return;
        }

        try {
            Paciente paciente = pacienteService.buscarPorId(pacienteId);
            if (paciente == null) {
                mostrarErro("Paciente não encontrado.");
                return;
            }

            // Obtém ou cria o prontuário caso ainda não exista
            prontuarioAtual = prontuarioService.obterOuCriarPorPaciente(paciente);

            preencherCabecalho(paciente);
            preencherFormulario();
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void preencherCabecalho(Paciente paciente) {
        Utilizador utilizador = paciente.getUtilizador();
        lblPacienteNome.setText(valorOuPadrao(formatarNome(utilizador)));
        lblPacienteMeta.setText("NIF " + valorOuPadrao(utilizador != null ? utilizador.getNif() : null));
        lblDataCriacao.setText(prontuarioAtual.getDatacriacao() != null
                ? prontuarioAtual.getDatacriacao().format(DATA_FORMATTER) : "-");
        lblUltimaAtualizacao.setText(prontuarioAtual.getUltimaAtualizacao() != null
                ? prontuarioAtual.getUltimaAtualizacao().format(DATA_FORMATTER) : "-");
    }

    private void preencherFormulario() {
        txtGrupoSanguineo.setText(valorVazio(prontuarioAtual.getGrupoSanguineo()));
        txtHistoricoMedico.setText(valorVazio(prontuarioAtual.getHistoricoMedico()));
        txtAlergias.setText(valorVazio(prontuarioAtual.getAlergias()));
        txtMedicamentosUso.setText(valorVazio(prontuarioAtual.getMedicamentosUso()));
        txtHistoricoOdontologico.setText(valorVazio(prontuarioAtual.getHistoricoOdontologico()));
        txtObservacoesClinicas.setText(valorVazio(prontuarioAtual.getObservacoesClinicas()));
        txtObservacoes.setText(valorVazio(prontuarioAtual.getObservacoes()));
    }

    /* ─── Ações ──────────────────────────────── */

    @FXML
    private void ativarEdicao() {
        atualizarModoEdicao(true);
    }

    @FXML
    private void cancelarEdicao() {
        atualizarModoEdicao(false);
        preencherFormulario();
    }

    @FXML
    private void guardarEdicao() {
        if (prontuarioAtual == null) {
            mostrarErro("Prontuário não carregado.");
            return;
        }

        try {
            prontuarioAtual.setGrupoSanguineo(valorOuNull(txtGrupoSanguineo.getText()));
            prontuarioAtual.setHistoricoMedico(valorOuNull(txtHistoricoMedico.getText()));
            prontuarioAtual.setAlergias(valorOuNull(txtAlergias.getText()));
            prontuarioAtual.setMedicamentosUso(valorOuNull(txtMedicamentosUso.getText()));
            prontuarioAtual.setHistoricoOdontologico(valorOuNull(txtHistoricoOdontologico.getText()));
            prontuarioAtual.setObservacoesClinicas(valorOuNull(txtObservacoesClinicas.getText()));
            prontuarioAtual.setObservacoes(valorOuNull(txtObservacoes.getText()));

            prontuarioAtual = prontuarioService.atualizar(prontuarioAtual);

            // Atualizar data de última atualização no cabeçalho
            lblUltimaAtualizacao.setText(prontuarioAtual.getUltimaAtualizacao() != null
                    ? prontuarioAtual.getUltimaAtualizacao().format(DATA_FORMATTER) : "-");

            atualizarModoEdicao(false);
            mostrarInformacao("Prontuário atualizado com sucesso.");
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
    }

    /* ─── Navegação ──────────────────────────── */

    @FXML
    private void voltarPacientes() throws IOException {
        SceneManager.trocarTela("/fxml/pacientes.fxml", "/css/dashboard-style.css");
    }

    @FXML
    private void abrirAgenda() throws IOException {
        SceneManager.trocarTela("/fxml/Agenda.fxml", "/css/dashboard-style.css");
    }

    @FXML
    private void abrirPacientes() throws IOException {
        SceneManager.trocarTela("/fxml/pacientes.fxml", "/css/dashboard-style.css");
    }

    @FXML
    private void abrirFaturacao() throws IOException {
        SceneManager.trocarTela("/fxml/payment-view.fxml", "/css/payment-style.css");
    }

    @FXML
    private void fazerLogout() throws IOException {
        SessionContext.limparSessao();
        SceneManager.trocarTelaMaximizado("/fxml/login-view.fxml", "/css/login-style.css");
    }

    /* ─── Utilitários de UI ───────────────────── */

    private void atualizarModoEdicao(boolean editar) {
        this.modoEdicao = editar;

        java.util.List<javafx.scene.control.Control> campos = java.util.List.of(
                txtGrupoSanguineo,
                txtHistoricoMedico,
                txtAlergias,
                txtMedicamentosUso,
                txtHistoricoOdontologico,
                txtObservacoesClinicas,
                txtObservacoes
        );

        for (javafx.scene.control.Control c : campos) {
            c.setDisable(!editar);
        }

        btnEditar.setDisable(editar);
        btnGuardar.setVisible(editar);
        btnGuardar.setManaged(editar);
        btnCancelar.setVisible(editar);
        btnCancelar.setManaged(editar);
    }

    /* ─── Utilitários de texto ───────────────── */

    private String formatarNome(Utilizador u) {
        if (u == null) return null;
        String p = u.getPrimeiroNome() != null ? u.getPrimeiroNome().trim() : "";
        String ult = u.getUltimoNome() != null ? u.getUltimoNome().trim() : "";
        String nome = (p + " " + ult).trim();
        return nome.isEmpty() ? null : nome;
    }

    private String valorOuPadrao(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private String valorVazio(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String valorOuNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void mostrarInformacao(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem != null && !mensagem.isBlank() ? mensagem : "Não foi possível concluir a operação.");
        alert.showAndWait();
    }
}

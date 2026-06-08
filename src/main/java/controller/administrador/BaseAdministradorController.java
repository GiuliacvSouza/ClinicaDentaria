package controller.administrador;

import app.SceneManager;
import app.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import model.Utilizador;

import java.io.IOException;

/**
 * Controller base partilhado por todos os ecrãs do Administrador.
 * Segue o mesmo padrão visual de BaseAssistenteController e BaseDentistaController.
 */
public abstract class BaseAdministradorController {

    protected static final String CSS = "/css/assistente-style.css";

    @FXML
    protected Label lblNomeUtilizador;

    @FXML
    public void initialize() {
        preencherNomeUtilizador();
        inicializarEcra();
    }

    protected void inicializarEcra() {
        // Subclasses sobrescrevem conforme necessário
    }

    protected Utilizador utilizadorLogado() {
        return SessionContext.getUtilizadorLogado();
    }

    private void preencherNomeUtilizador() {
        if (lblNomeUtilizador != null) {
            Utilizador u = utilizadorLogado();
            if (u != null) {
                String nome = (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim();
                lblNomeUtilizador.setText(nome.isBlank() ? "Administrador" : nome);
            } else {
                lblNomeUtilizador.setText("Administrador");
            }
        }
    }

    // ─── Navegação ──────────────────────────────────────────────────────────────

    @FXML
    protected void abrirDashboard() {
        navegar("/fxml/administrador/dashboard-administrador.fxml");
    }

    @FXML
    protected void abrirUtilizadores() {
        navegar("/fxml/administrador/utilizadores.fxml");
    }

    @FXML
    protected void abrirAgendaClinica() {
        navegar("/fxml/administrador/agenda-clinica.fxml");
    }

    @FXML
    protected void abrirProcedimentos() {
        navegar("/fxml/administrador/procedimentos.fxml");
    }

    @FXML
    protected void abrirFinanceiro() {
        navegar("/fxml/administrador/financeiro.fxml");
    }

    @FXML
    protected void abrirEstoque() {
        navegar("/fxml/administrador/estoque.fxml");
    }

    @FXML
    protected void abrirAuditoria() {
        navegar("/fxml/administrador/auditoria.fxml");
    }

    @FXML
    protected void abrirSeguros() {
        navegar("/fxml/administrador/seguros.fxml");
    }

    @FXML
    protected void fazerLogout() {
        SessionContext.limparSessao();
        try {
            SceneManager.trocarTelaMaximizado("/fxml/login-view.fxml", "/css/login-style.css");
        } catch (IOException e) {
            mostrarErro("Erro ao fazer logout.");
        }
    }

    protected void navegar(String fxml) {
        try {
            SceneManager.trocarTela(fxml, CSS);
        } catch (IOException e) {
            mostrarErro("Pagina ainda nao disponivel: " + e.getMessage());
        }
    }

    // ─── Utilitários ────────────────────────────────────────────────────────────

    protected void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem != null && !mensagem.isBlank() ? mensagem : "Nao foi possivel concluir a operacao.");
        alert.showAndWait();
    }

    protected void mostrarInfo(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacao");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    protected String nvl(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
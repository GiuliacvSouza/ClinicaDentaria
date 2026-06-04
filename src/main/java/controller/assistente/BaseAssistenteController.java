package controller.assistente;

import app.SceneManager;
import app.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Utilizador;

import java.io.IOException;

/**
 * Controller base partilhado por todos os ecrãs do Assistente.
 * Centraliza navegação, logout e preenchimento do nome na topbar.
 */
public abstract class BaseAssistenteController {

    @FXML
    protected Label lblNomeUtilizador;

    // ─── Inicialização ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        preencherNomeUtilizador();
        inicializarEcra();
    }

    /**
     * Ponto de extensão para subclasses — chamado automaticamente após
     * o preenchimento do nome do utilizador.
     */
    protected void inicializarEcra() {
        // subclasses sobrescrevem conforme necessário
    }

    private void preencherNomeUtilizador() {
        Utilizador u = SessionContext.getUtilizadorLogado();
        if (u != null && lblNomeUtilizador != null) {
            String nome = (u.getPrimeiroNome() != null ? u.getPrimeiroNome() : "")
                    + " "
                    + (u.getUltimoNome() != null ? u.getUltimoNome() : "");
            lblNomeUtilizador.setText(nome.trim());
        }
    }

    // ─── Navegação ────────────────────────────────────────────────────────────

    private static final String CSS = "/css/assistente-style.css";

    @FXML
    protected void abrirDashboard() throws IOException {
        SceneManager.trocarTela("/fxml/assistente/dashboard-assistente.fxml", CSS);
    }

    @FXML
    protected void abrirConsultasDia() throws IOException {
        SceneManager.trocarTela("/fxml/assistente/consultas-dia.fxml", CSS);
    }

    @FXML
    protected void abrirMateriais() throws IOException {
        SceneManager.trocarTela("/fxml/assistente/materiais.fxml", CSS);
    }

    @FXML
    protected void abrirMovimentacoes() throws IOException {
        SceneManager.trocarTela("/fxml/assistente/movimentacoes-stock.fxml", CSS);
    }

    @FXML
    protected void abrirPedidosCompra() throws IOException {
        SceneManager.trocarTela("/fxml/assistente/pedidos-compra.fxml", CSS);
    }

    @FXML
    protected void abrirFornecedores() throws IOException {
        SceneManager.trocarTela("/fxml/assistente/fornecedores.fxml", CSS);
    }

    @FXML
    protected void abrirAlertas() throws IOException {
        SceneManager.trocarTela("/fxml/assistente/alertas.fxml", CSS);
    }

    @FXML
    protected void abrirPerfil() throws IOException {
        SceneManager.trocarTela("/fxml/assistente/perfil-assistente.fxml", CSS);
    }

    @FXML
    protected void fazerLogout() throws IOException {
        SessionContext.limparSessao();
        SceneManager.trocarTelaMaximizado("/fxml/login-view.fxml", "/css/login-style.css");
    }
}

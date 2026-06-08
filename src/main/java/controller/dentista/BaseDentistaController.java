package controller.dentista;

import app.SceneManager;
import app.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import model.Dentista;
import model.Utilizador;

import java.io.IOException;

public abstract class BaseDentistaController {

    protected static final String CSS = "/css/assistente-style.css";

    @FXML protected Label lblNomeUtilizador;

    @FXML
    public void initialize() {
        preencherNomeUtilizador();
        inicializarEcra();
    }

    protected void inicializarEcra() {
    }

    protected Dentista dentistaLogado() {
        return SessionContext.getDentistaLogado();
    }

    protected Utilizador utilizadorLogado() {
        return SessionContext.getUtilizadorLogado();
    }

    protected Integer dentistaId() {
        Dentista dentista = dentistaLogado();
        return dentista != null ? dentista.getId() : null;
    }

    protected String nomeDentista() {
        Utilizador u = utilizadorLogado();
        if (u == null) {
            Dentista d = dentistaLogado();
            u = d != null ? d.getUtilizador() : null;
        }
        if (u == null) {
            return "Dentista";
        }
        String nome = (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim();
        return nome.isBlank() ? "Dentista" : nome;
    }

    private void preencherNomeUtilizador() {
        if (lblNomeUtilizador != null) {
            lblNomeUtilizador.setText(nomeDentista());
        }
    }

    @FXML protected void abrirDashboard() { navegar("/fxml/dentista/dashboard-dentista.fxml"); }
    @FXML protected void abrirAgenda() { navegar("/fxml/dentista/agenda-dentista.fxml"); }
    @FXML protected void abrirProntuarios() { navegar("/fxml/dentista/prontuarios-dentista.fxml"); }
    @FXML protected void abrirPrescricoes() { navegar("/fxml/dentista/prescricoes-dentista.fxml"); }
    @FXML protected void abrirPlanosTratamento() { navegar("/fxml/dentista/planos-tratamento.fxml"); }
    @FXML protected void abrirPerfil() { navegar("/fxml/dentista/perfil-dentista.fxml"); }

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
            mostrarErro("Pagina ainda nao disponivel.");
        }
    }

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

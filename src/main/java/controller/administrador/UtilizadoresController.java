package controller.administrador;

import bll.AuditoriaService;
import bll.UtilizadorService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Utilizador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UtilizadoresController extends BaseAdministradorController {

    private static final List<String> TIPOS = List.of("ADMINISTRADOR", "DENTISTA", "ASSISTENTE", "RECEPCIONISTA");
    private static final List<String> ESTADOS = List.of("ativo", "inativo");

    @FXML private TextField txtPesquisa;
    @FXML private VBox containerLista;
    @FXML private TextField txtNome;
    @FXML private TextField txtApelido;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private PasswordField txtSenha;
    @FXML private TextField txtNif;
    @FXML private TextField txtTelefone;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblMensagem;

    @Autowired private UtilizadorService utilizadorService;
    @Autowired private AuditoriaService auditoriaService;

    private List<Utilizador> utilizadores;
    private Utilizador utilizadorSelecionado;

    @Override
    protected void inicializarEcra() {
        cmbTipo.setItems(FXCollections.observableArrayList(TIPOS));
        cmbEstado.setItems(FXCollections.observableArrayList(ESTADOS));
        cmbEstado.setValue("ativo");
        limparFormulario();
        carregarUtilizadores();
    }

    private void carregarUtilizadores() {
        try {
            utilizadores = utilizadorService.listarTodos();
            renderizarLista(utilizadores);
        } catch (Exception e) {
            mostrarErro("Erro ao carregar utilizadores: " + e.getMessage());
        }
    }

    @FXML
    private void filtrarUtilizadores() {
        if (utilizadores == null) return;
        String termo = txtPesquisa.getText().trim().toLowerCase();
        if (termo.isBlank()) {
            renderizarLista(utilizadores);
            return;
        }
        List<Utilizador> filtrados = utilizadores.stream()
                .filter(u -> (u.getPrimeiroNome() != null && u.getPrimeiroNome().toLowerCase().contains(termo))
                        || (u.getUltimoNome() != null && u.getUltimoNome().toLowerCase().contains(termo))
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(termo)))
                .collect(Collectors.toList());
        renderizarLista(filtrados);
    }

    private void renderizarLista(List<Utilizador> lista) {
        if (containerLista == null) return;
        containerLista.getChildren().clear();
        if (lista.isEmpty()) {
            Label vazio = new Label("Nenhum utilizador encontrado.");
            vazio.getStyleClass().add("section-caption");
            containerLista.getChildren().add(vazio);
            return;
        }
        for (Utilizador u : lista) {
            containerLista.getChildren().add(criarCartaoUtilizador(u));
        }
    }

    private HBox criarCartaoUtilizador(Utilizador u) {
        HBox cartao = new HBox(12);
        cartao.setAlignment(Pos.CENTER_LEFT);
        cartao.getStyleClass().add("consulta-card");
        if (utilizadorSelecionado != null && utilizadorSelecionado.getId() != null
                && utilizadorSelecionado.getId().equals(u.getId())) {
            cartao.setStyle("-fx-border-color: #2e7d72; -fx-border-width: 2;");
        }

        VBox textos = new VBox(2);
        String nomeCompleto = (nvl(u.getPrimeiroNome()) + " " + nvl(u.getUltimoNome())).trim();
        if (nomeCompleto.isBlank()) nomeCompleto = u.getEmail() != null ? u.getEmail() : "Utilizador";
        Label nome = new Label(nomeCompleto);
        nome.getStyleClass().add("consulta-paciente");

        Label email = new Label(u.getEmail() != null ? u.getEmail() : "");
        email.getStyleClass().add("consulta-meta");

        Label tipo = new Label(u.getTipoUtilizador() != null ? u.getTipoUtilizador() : "—");
        tipo.getStyleClass().add("stat-tag-blue");

        textos.getChildren().addAll(nome, email, tipo);
        HBox.setHgrow(textos, Priority.ALWAYS);

        // Estado
        String estado = u.getStatus() != null ? u.getStatus().toLowerCase() : "desconhecido";
        Label lblEstado = new Label(estado.equals("ativo") ? "Ativo" : "Inativo");
        lblEstado.getStyleClass().add(estado.equals("ativo") ? "badge-ok" : "badge-critico");

        cartao.getChildren().addAll(textos, lblEstado);

        cartao.setOnMouseClicked(e -> {
            utilizadorSelecionado = u;
            preencherFormulario(u);
            renderizarLista(listaAtual());
        });
        return cartao;
    }

    private List<Utilizador> listaAtual() {
        String termo = txtPesquisa.getText() != null ? txtPesquisa.getText().trim().toLowerCase() : "";
        if (termo.isBlank()) return utilizadores;
        return utilizadores.stream()
                .filter(u -> (u.getPrimeiroNome() != null && u.getPrimeiroNome().toLowerCase().contains(termo))
                        || (u.getUltimoNome() != null && u.getUltimoNome().toLowerCase().contains(termo))
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(termo)))
                .collect(Collectors.toList());
    }

    private void preencherFormulario(Utilizador u) {
        txtNome.setText(u.getPrimeiroNome() != null ? u.getPrimeiroNome() : "");
        txtApelido.setText(u.getUltimoNome() != null ? u.getUltimoNome() : "");
        txtEmail.setText(u.getEmail() != null ? u.getEmail() : "");
        cmbTipo.setValue(u.getTipoUtilizador());
        txtNif.setText(u.getNif() != null ? u.getNif() : "");
        txtTelefone.setText(u.getTelefone() != null ? u.getTelefone() : "");
        cmbEstado.setValue(u.getStatus() != null ? u.getStatus() : "ativo");
        txtSenha.clear();
    }

    @FXML
    private void novoUtilizador() {
        utilizadorSelecionado = null;
        limparFormulario();
        renderizarLista(utilizadores);
    }

    private void limparFormulario() {
        txtNome.clear();
        txtApelido.clear();
        txtEmail.clear();
        cmbTipo.setValue(null);
        txtSenha.clear();
        txtNif.clear();
        txtTelefone.clear();
        cmbEstado.setValue("ativo");
        if (lblMensagem != null) {
            lblMensagem.setVisible(false);
            lblMensagem.setManaged(false);
        }
    }

    @FXML
    private void salvarUtilizador() {
        try {
            if (txtNome.getText() == null || txtNome.getText().isBlank()) {
                mostrarMensagem("Primeiro nome e obrigatorio.", true);
                return;
            }
            if (txtEmail.getText() == null || txtEmail.getText().isBlank()) {
                mostrarMensagem("Email e obrigatorio.", true);
                return;
            }
            if (cmbTipo.getValue() == null) {
                mostrarMensagem("Tipo de utilizador e obrigatorio.", true);
                return;
            }

            Utilizador u = utilizadorSelecionado != null ? utilizadorSelecionado : new Utilizador();
            u.setPrimeiroNome(txtNome.getText().trim());
            u.setUltimoNome(txtApelido.getText().trim());
            u.setEmail(txtEmail.getText().trim());
            u.setTipoUtilizador(cmbTipo.getValue());
            u.setNif(txtNif.getText() != null ? txtNif.getText().trim() : null);
            u.setTelefone(txtTelefone.getText() != null ? txtTelefone.getText().trim() : null);
            u.setStatus(cmbEstado.getValue() != null ? cmbEstado.getValue() : "ativo");

            if (utilizadorSelecionado == null && (txtSenha.getText() == null || txtSenha.getText().isBlank())) {
                mostrarMensagem("Palavra-passe e obrigatoria para novos utilizadores.", true);
                return;
            }
            if (txtSenha.getText() != null && !txtSenha.getText().isBlank()) {
                u.setSenha(txtSenha.getText());
            }

            Utilizador salvo = utilizadorService.salvar(u);
            String operacao = utilizadorSelecionado == null ? "CRIAR_UTILIZADOR" : "EDITAR_UTILIZADOR";
            auditoriaService.registar(utilizadorLogado(), operacao,
                    "Utilizador: " + salvo.getEmail() + " (" + salvo.getTipoUtilizador() + ")");
            utilizadorSelecionado = salvo;
            mostrarMensagem("Utilizador guardado com sucesso.", false);
            carregarUtilizadores();
        } catch (Exception e) {
            mostrarMensagem("Erro ao guardar: " + e.getMessage(), true);
        }
    }

    @FXML
    private void redefinirSenha() {
        if (utilizadorSelecionado == null) {
            mostrarMensagem("Selecione um utilizador primeiro.", true);
            return;
        }
        String novaSenha = "alterar123";
        utilizadorSelecionado.setSenha(novaSenha);
        try {
            utilizadorService.salvar(utilizadorSelecionado);
            auditoriaService.registar(utilizadorLogado(), "REDEFINIR_SENHA",
                    "Utilizador: " + utilizadorSelecionado.getEmail());
            mostrarInfo("Palavra-passe redefinida para: " + novaSenha);
        } catch (Exception e) {
            mostrarMensagem("Erro: " + e.getMessage(), true);
        }
    }

    @FXML
    private void ativarDesativar() {
        if (utilizadorSelecionado == null) {
            mostrarMensagem("Selecione um utilizador primeiro.", true);
            return;
        }
        String novoEstado = "ativo".equalsIgnoreCase(utilizadorSelecionado.getStatus()) ? "inativo" : "ativo";
        utilizadorSelecionado.setStatus(novoEstado);
        try {
            utilizadorService.salvar(utilizadorSelecionado);
            auditoriaService.registar(utilizadorLogado(), novoEstado.equals("ativo") ? "ATIVAR_UTILIZADOR" : "DESATIVAR_UTILIZADOR",
                    "Utilizador: " + utilizadorSelecionado.getEmail());
            cmbEstado.setValue(novoEstado);
            carregarUtilizadores();
            mostrarInfo("Estado alterado para: " + novoEstado);
        } catch (Exception e) {
            mostrarMensagem("Erro: " + e.getMessage(), true);
        }
    }

    @FXML
    private void cancelar() {
        limparFormulario();
        utilizadorSelecionado = null;
        renderizarLista(utilizadores);
    }

    private void mostrarMensagem(String msg, boolean erro) {
        if (lblMensagem == null) return;
        lblMensagem.setText(msg);
        lblMensagem.setVisible(true);
        lblMensagem.setManaged(true);
        lblMensagem.getStyleClass().removeAll("error-label");
        lblMensagem.getStyleClass().add(erro ? "error-label" : "info-title");
    }
}
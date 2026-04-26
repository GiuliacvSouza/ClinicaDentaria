package controller;

import app.SceneManager;
import app.SessionContext;
import bll.RecepcionistaService;
import bll.UtilizadorService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import model.Recepcionista;
import model.Utilizador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.time.Instant;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private TextField txtPassword;
    @FXML private Button btnEntrar;
    @FXML private Button btnTogglePassword;
    @FXML private Label lblErro;

    private boolean senhaVisivel = false;
    private String senhaReal = "";
    private boolean atualizandoSenha = false;

    @Autowired
    private UtilizadorService utilizadorService;

    @Autowired
    private RecepcionistaService recepcionistaService;

    private Utilizador utilizadorLogado;
    private Recepcionista recepcionistaLogado;

    @FXML
    public void initialize() {
        setupEventHandlers();
        setupPlaceholders();
        setupPasswordToggle();
    }

    private void setupPasswordToggle() {
        // Setup já feito no FXML com SVGPath
    }

    private void setupEventHandlers() {
        btnEntrar.setOnAction(event -> handleLogin());

        txtEmail.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                txtPassword.requestFocus();
            }
        });

        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        txtEmail.textProperty().addListener((obs, old, newVal) -> {
            lblErro.setVisible(false);
            txtEmail.setStyle("");
        });

        txtPassword.textProperty().addListener((obs, old, newVal) -> {
            lblErro.setVisible(false);
            txtPassword.setStyle("");
            if (!atualizandoSenha) {
                if (!senhaVisivel) {
                    senhaReal = newVal;
                    atualizandoSenha = true;
                    int posCursor = txtPassword.getCaretPosition();
                    txtPassword.setText(new String(new char[newVal.length()]).replace('\0', '•'));
                    txtPassword.positionCaret(Math.min(posCursor, txtPassword.getText().length()));
                    atualizandoSenha = false;
                } else {
                    senhaReal = newVal;
                }
            }
        });
    }

    private void setupPlaceholders() {
        txtEmail.setPromptText("seu@email.com");
        txtPassword.setPromptText("••••••••");
    }

    @FXML
    private void togglePasswordVisibility() {
        senhaVisivel = !senhaVisivel;
        atualizandoSenha = true;
        if (senhaVisivel) {
            txtPassword.setText(senhaReal);
        } else {
            senhaReal = txtPassword.getText();
            if (!senhaReal.isEmpty()) {
                txtPassword.setText(new String(new char[senhaReal.length()]).replace('\0', '•'));
            }
        }
        atualizandoSenha = false;
    }

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String senha = senhaVisivel ? txtPassword.getText() : senhaReal;

        if (email.isEmpty()) {
            showError("Por favor, insira o email");
            txtEmail.requestFocus();
            highlightError(txtEmail);
            return;
        }

        if (senha.isEmpty()) {
            showError("Por favor, insira a palavra-passe");
            txtPassword.requestFocus();
            highlightError(txtPassword);
            return;
        }

        if (!isValidEmail(email)) {
            showError("Por favor, insira um email valido");
            txtEmail.requestFocus();
            highlightError(txtEmail);
            return;
        }

        btnEntrar.setDisable(true);
        btnEntrar.setText("A ENTRAR...");

        new Thread(() -> {
            try {
                Utilizador user = utilizadorService.autenticar(email, senha);

                javafx.application.Platform.runLater(() -> {
                    if (user != null && "ativo".equalsIgnoreCase(user.getStatus())) {
                        utilizadorLogado = user;

                        if ("RECEPCIONISTA".equals(user.getTipoUtilizador())) {
                            try {
                                recepcionistaLogado = recepcionistaService.buscarPorUtilizadorId(user.getId());
                            } catch (Exception ignored) {
                                recepcionistaLogado = null;
                            }
                        }

                        user.setUltimoAcesso(Instant.now());
                        utilizadorService.salvar(user);
                        SessionContext.iniciarSessao(utilizadorLogado, recepcionistaLogado);
                        showSuccessAndNavigate();
                    } else {
                        showError("Email, palavra-passe invalidos ou conta inativa");
                        txtPassword.clear();
                        txtPassword.requestFocus();
                        highlightError(txtEmail);
                        highlightError(txtPassword);
                        btnEntrar.setDisable(false);
                        btnEntrar.setText("ENTRAR");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Erro: " + e.getMessage());
                    btnEntrar.setDisable(false);
                    btnEntrar.setText("ENTRAR");
                });
            }
        }).start();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private void showSuccessAndNavigate() {
        btnEntrar.setStyle("-fx-background-color: #4CAF50;");

        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(event -> openAgenda());
        delay.play();
    }

    private void openAgenda() {
        try {
            SceneManager.trocarTela("/fxml/Agenda.fxml", "/css/dashboard-style.css");
            SceneManager.getMainStage().setTitle("Clinica Dentaria - Agenda");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao carregar a agenda: " + e.getMessage());
            btnEntrar.setDisable(false);
            btnEntrar.setText("ENTRAR");
            btnEntrar.setStyle("");
        }
    }

    private void showError(String message) {
        lblErro.setText(message);
        lblErro.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> lblErro.setVisible(false));
        delay.play();
    }

    private void highlightError(Control campo) {
        campo.setStyle("-fx-border-color: #FB2424; -fx-border-width: 2px;");

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> campo.setStyle(""));
        delay.play();
    }
}

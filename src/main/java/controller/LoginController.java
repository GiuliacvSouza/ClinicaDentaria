package controller;

import app.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import model.Utilizador;
import model.Recepcionista;
import bll.UtilizadorService;
import bll.RecepcionistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnEntrar;
    @FXML private Label lblErro;

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
        System.out.println("LoginController inicializado com Spring!");
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
        });
    }

    private void setupPlaceholders() {
        txtEmail.setPromptText("seu@email.com");
        txtPassword.setPromptText("••••••••");
    }

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String senha = txtPassword.getText();

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
            showError("Por favor, insira um email válido");
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
                    // Verifica se o usuário existe e está ativo (status = "ativo")
                    if (user != null && "ativo".equalsIgnoreCase(user.getStatus())) {
                        utilizadorLogado = user;

                        // Verifica se é recepcionista e carrega dados adicionais
                        if ("RECEPCIONISTA".equals(user.getTipoUtilizador())) {
                            try {
                                recepcionistaLogado = recepcionistaService.buscarPorUtilizadorId(user.getId());
                                System.out.println("Recepcionista logado - Turno: " + recepcionistaLogado.getTurno());
                            } catch (Exception e) {
                                System.out.println("Info: Utilizador não é recepcionista ou dados não encontrados");
                            }
                        }

                        // Atualiza último acesso (ultimoAcesso) com a data/hora atual
                        user.setUltimoAcesso(Instant.now());
                        utilizadorService.salvar(user);
                        SessionContext.iniciarSessao(utilizadorLogado, recepcionistaLogado);
                        showSuccessAndNavigate();
                    } else {
                        showError("Email, palavra-passe inválidos ou conta inativa");
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
        delay.setOnFinished(event -> openMenuPrincipal());
        delay.play();
    }

    private void openMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu-view.fxml"));
            Parent root = loader.load();

            MenuController menuController = loader.getController();
            menuController.setDadosLogin(utilizadorLogado, recepcionistaLogado);

            Stage stage = (Stage) btnEntrar.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);

            try {
                scene.getStylesheets().add(getClass().getResource("/css/menu-style.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("CSS do menu não encontrado");
            }

            stage.setScene(scene);
            stage.setTitle("Clínica Dentária - Menu Principal - Bem-vindo, " + utilizadorLogado.getPrimeiroNome());
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao carregar o menu: " + e.getMessage());
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

package controller;

import app.SceneManager;
import app.SessionContext;
import bll.AssistenteService;
import bll.DentistaService;
import bll.RecepcionistaService;
import bll.UtilizadorService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.SVGPath;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.Assistente;
import model.Dentista;
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
    @FXML private HBox loginRoot;
    @FXML private StackPane imagePanel;
    @FXML private VBox loginContent;
    @FXML private StackPane passwordWrapper;
    @FXML private ImageView imgClinica;

    private boolean senhaVisivel = false;
    private String senhaReal = "";
    private boolean atualizandoSenha = false;
    private static final String INPUT_ERROR_CLASS = "login-input-error";
    private static final String PASSWORD_FOCUSED_CLASS = "focused";
    private static final String BUTTON_SUCCESS_CLASS = "primary-button-success";

    @Autowired
    private UtilizadorService utilizadorService;

    @Autowired
    private RecepcionistaService recepcionistaService;

    @Autowired
    private AssistenteService assistenteService;

    @Autowired
    private DentistaService dentistaService;

    private Utilizador utilizadorLogado;
    private Recepcionista recepcionistaLogado;
    private Assistente assistenteLogado;
    private Dentista dentistaLogado;

    @FXML
    public void initialize() {
        setupResponsiveLayout();
        setupEventHandlers();
        setupPlaceholders();
        setupPasswordToggle();
    }

    private void setupResponsiveLayout() {
        if (loginRoot != null && imagePanel != null && loginContent != null) {
            imagePanel.prefWidthProperty().bind(loginRoot.widthProperty().multiply(0.40));
            loginContent.prefWidthProperty().bind(loginRoot.widthProperty().multiply(0.60));
        }

        if (imagePanel != null) {
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(imagePanel.widthProperty());
            clip.heightProperty().bind(imagePanel.heightProperty());
            clip.setArcWidth(42);
            clip.setArcHeight(42);
            imagePanel.setClip(clip);
        }

        if (imgClinica != null && imagePanel != null) {
            imgClinica.fitWidthProperty().bind(imagePanel.widthProperty());
            imgClinica.fitHeightProperty().bind(imagePanel.heightProperty());
            imgClinica.imageProperty().addListener((obs, antiga, nova) -> atualizarViewportImagem());
            imagePanel.widthProperty().addListener((obs, antiga, nova) -> atualizarViewportImagem());
            imagePanel.heightProperty().addListener((obs, antiga, nova) -> atualizarViewportImagem());
            atualizarViewportImagem();
        }

        if (passwordWrapper != null && txtPassword != null) {
            txtPassword.focusedProperty().addListener((obs, antigo, focado) -> alternarClasse(
                    passwordWrapper, PASSWORD_FOCUSED_CLASS, focado));
        }

        if (passwordWrapper != null && btnTogglePassword != null) {
            btnTogglePassword.focusedProperty().addListener((obs, antigo, focado) -> alternarClasse(
                    passwordWrapper, PASSWORD_FOCUSED_CLASS, focado));
        }

        if (lblErro != null) {
            lblErro.managedProperty().bind(lblErro.visibleProperty());
        }
    }

    private void atualizarViewportImagem() {
        if (imgClinica == null || imagePanel == null) return;

        Image imagem = imgClinica.getImage();
        double larguraPainel = imagePanel.getWidth();
        double alturaPainel = imagePanel.getHeight();
        if (imagem == null || larguraPainel <= 0 || alturaPainel <= 0) return;

        double larguraImagem = imagem.getWidth();
        double alturaImagem = imagem.getHeight();
        double proporcaoImagem = larguraImagem / alturaImagem;
        double proporcaoPainel = larguraPainel / alturaPainel;

        if (proporcaoImagem > proporcaoPainel) {
            double larguraVisivel = alturaImagem * proporcaoPainel;
            double x = (larguraImagem - larguraVisivel) / 2;
            imgClinica.setViewport(new Rectangle2D(x, 0, larguraVisivel, alturaImagem));
        } else {
            double alturaVisivel = larguraImagem / proporcaoPainel;
            double y = Math.max(0, (alturaImagem - alturaVisivel) / 2);
            imgClinica.setViewport(new Rectangle2D(0, y, larguraImagem, alturaVisivel));
        }
    }

    private void setupPasswordToggle() {
        if (btnTogglePassword == null) return;

        StackPane graphicStack = (StackPane) btnTogglePassword.getGraphic();
        if (graphicStack == null || graphicStack.getChildren().isEmpty()) return;

        SVGPath icon = (SVGPath) graphicStack.getChildren().get(0);
        icon.setContent(senhaVisivel
                ? "M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24M1 1l22 22"
                : "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
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
            removerClasse(txtEmail, INPUT_ERROR_CLASS);
        });

        txtPassword.textProperty().addListener((obs, old, newVal) -> {
            lblErro.setVisible(false);
            removerClasse(passwordWrapper, INPUT_ERROR_CLASS);
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
        setupPasswordToggle();
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
            highlightError(passwordWrapper);
            return;
        }

        if (!isValidEmail(email)) {
            showError("Por favor, insira um email válido");
            txtEmail.requestFocus();
            highlightError(txtEmail);
            return;
        }

        btnEntrar.setDisable(true);
        btnEntrar.setText("A entrar...");

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
                                assistenteLogado = null;
                                dentistaLogado = null;
                            } else if ("ASSISTENTE".equals(user.getTipoUtilizador())) {
                                try {
                                    assistenteLogado = assistenteService.buscarPorUtilizadorId(user.getId());
                                } catch (Exception ignored) {
                                    assistenteLogado = null;
                                }
                                recepcionistaLogado = null;
                                dentistaLogado = null;
                            } else if ("DENTISTA".equals(user.getTipoUtilizador())) {
                                try {
                                    dentistaLogado = dentistaService.buscarPorUtilizadorId(user.getId());
                                } catch (Exception ignored) {
                                    dentistaLogado = null;
                                }
                                recepcionistaLogado = null;
                                assistenteLogado = null;
                            } else if ("ADMINISTRADOR".equals(user.getTipoUtilizador())) {
                                // Administrador usa apenas o Utilizador base
                                recepcionistaLogado = null;
                                assistenteLogado = null;
                                dentistaLogado = null;
                            }

                            user.setUltimoAcesso(Instant.now());
                            utilizadorService.salvar(user);

                            if ("ASSISTENTE".equals(user.getTipoUtilizador())) {
                                SessionContext.iniciarSessaoAssistente(utilizadorLogado, assistenteLogado);
                            } else if ("DENTISTA".equals(user.getTipoUtilizador())) {
                                SessionContext.iniciarSessaoDentista(utilizadorLogado, dentistaLogado);
                            } else if ("ADMINISTRADOR".equals(user.getTipoUtilizador())) {
                                SessionContext.iniciarSessaoAdministrador(utilizadorLogado);
                            } else {
                                SessionContext.iniciarSessao(utilizadorLogado, recepcionistaLogado);
                            }

                        showSuccessAndNavigate();
                    } else {
                        showError("Email ou palavra-passe inválidos, ou conta inativa");
                        txtPassword.clear();
                        txtPassword.requestFocus();
                        highlightError(txtEmail);
                        highlightError(passwordWrapper);
                        btnEntrar.setDisable(false);
                        btnEntrar.setText("Entrar");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Erro: " + e.getMessage());
                    btnEntrar.setDisable(false);
                    btnEntrar.setText("Entrar");
                });
            }
        }).start();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private void showSuccessAndNavigate() {
        adicionarClasse(btnEntrar, BUTTON_SUCCESS_CLASS);

        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(event -> {
            Utilizador u = SessionContext.getUtilizadorLogado();
            if (u != null && "ASSISTENTE".equals(u.getTipoUtilizador())) {
                openDashboardAssistente();
            } else if (u != null && "DENTISTA".equals(u.getTipoUtilizador())) {
                openDashboardDentista();
            } else if (u != null && "ADMINISTRADOR".equals(u.getTipoUtilizador())) {
                openDashboardAdministrador();
            } else {
                openAgenda();
            }
        });
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
            btnEntrar.setText("Entrar");
            removerClasse(btnEntrar, BUTTON_SUCCESS_CLASS);
        }
    }

    private void openDashboardAssistente() {
        try {
            SceneManager.trocarTela("/fxml/assistente/dashboard-assistente.fxml", "/css/assistente-style.css");
            SceneManager.getMainStage().setTitle("Clinica Dentaria - Assistente");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao carregar o dashboard do assistente: " + e.getMessage());
            btnEntrar.setDisable(false);
            btnEntrar.setText("Entrar");
            removerClasse(btnEntrar, BUTTON_SUCCESS_CLASS);
        }
    }

    private void openDashboardDentista() {
        try {
            SceneManager.trocarTela("/fxml/dentista/dashboard-dentista.fxml", "/css/assistente-style.css");
            SceneManager.getMainStage().setTitle("Clinica Dentaria - Dentista");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao carregar o dashboard do dentista: " + e.getMessage());
            btnEntrar.setDisable(false);
            btnEntrar.setText("Entrar");
            removerClasse(btnEntrar, BUTTON_SUCCESS_CLASS);
        }
    }

    private void openDashboardAdministrador() {
        try {
            SceneManager.trocarTela("/fxml/administrador/dashboard-administrador.fxml", "/css/assistente-style.css");
            SceneManager.getMainStage().setTitle("Clinica Dentaria - Administrador");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao carregar o dashboard do administrador: " + e.getMessage());
            btnEntrar.setDisable(false);
            btnEntrar.setText("Entrar");
            removerClasse(btnEntrar, BUTTON_SUCCESS_CLASS);
        }
    }

    private void showError(String message) {
        lblErro.setText(message);
        lblErro.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> lblErro.setVisible(false));
        delay.play();
    }

    private void highlightError(Node campo) {
        adicionarClasse(campo, INPUT_ERROR_CLASS);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> removerClasse(campo, INPUT_ERROR_CLASS));
        delay.play();
    }

    private void adicionarClasse(Node node, String styleClass) {
        if (node != null && !node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        }
    }

    private void removerClasse(Node node, String styleClass) {
        if (node != null) {
            node.getStyleClass().remove(styleClass);
        }
    }

    private void alternarClasse(Node node, String styleClass, boolean ativa) {
        if (ativa) {
            adicionarClasse(node, styleClass);
        } else {
            removerClasse(node, styleClass);
        }
    }
}

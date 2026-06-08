package app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Gerenciador centralizado de mudanças de cena (trocar de tela).
 * Mantém uniformidade no comportamento de tamanho e estado da janela.
 */
public class SceneManager {

    private static Stage mainStage;

    private SceneManager() {
        // Utilitário estático - não instanciar
    }

    /**
     * Define o Stage principal da aplicação.
     * Deve ser chamado uma vez na inicialização, geralmente no LoginController.
     */
    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    private static String parseQueryAndGetCleanPath(String fxmlPath) {
        String cleanPath = fxmlPath;
        int qIndex = fxmlPath.indexOf('?');
        if (qIndex != -1) {
            cleanPath = fxmlPath.substring(0, qIndex);
            String query = fxmlPath.substring(qIndex + 1);
            SessionContext.setCurrentQuery(query);
        } else {
            SessionContext.limparQuery();
        }
        return cleanPath;
    }

    /**
     * Troca a cena preservando tamanho, posição e estado da janela.
     *
     * @param fxmlPath     Caminho do FXML a carregar (ex: "/fxml/Agenda.fxml")
     * @param cssPath      Caminho do CSS a aplicar (ex: "/css/dashboard-style.css")
     */
    public static void trocarTela(String fxmlPath, String cssPath) throws IOException {
        System.out.println("[SCENEMANAGER] trocarTela request: " + fxmlPath + ", css=" + cssPath);
        if (mainStage == null) {
            System.err.println("[SCENEMANAGER] ERRO: mainStage ainda nao inicializado.");
            throw new RuntimeException("SceneManager nao foi inicializado. Chame setMainStage() primeiro.");
        }

        String cleanPath = parseQueryAndGetCleanPath(fxmlPath);
        var resource = SceneManager.class.getResource(cleanPath);
        if (resource == null) {
            System.err.println("[SCENEMANAGER] ERRO: FXML nao encontrado: " + cleanPath);
            throw new RuntimeException("FXML nao encontrado: " + cleanPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        if (MainFX.getSpringContext() != null) {
            loader.setControllerFactory(MainFX.getSpringContext()::getBean);
        }

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("[SCENEMANAGER] ERRO ao carregar FXML: " + cleanPath);
            e.printStackTrace();
            throw e;
        }

        aplicarCenaPreservandoJanela(root, cssPath);
    }

    /**
     * Versão simplificada: apenas troca cena (sem controle de CSS).
     */
    public static void trocarTela(String fxmlPath) throws IOException {
        trocarTela(fxmlPath, null);
    }

    /**
     * Retorna o Stage principal.
     */
    public static Stage getMainStage() {
        return mainStage;
    }

    /**
     * Mantida por compatibilidade com controllers existentes.
     * Atualmente preserva o estado da janela em vez de forçar maximização.
     */
    public static void trocarTelaMaximizado(String fxmlPath, String cssPath) throws IOException {
        System.out.println("[SCENEMANAGER] trocarTelaMaximizado request: " + fxmlPath + ", css=" + cssPath);
        if (mainStage == null) {
            System.err.println("[SCENEMANAGER] ERRO: mainStage ainda nao inicializado (maximizado).");
            throw new RuntimeException("SceneManager nao foi inicializado.");
        }

        String cleanPath = parseQueryAndGetCleanPath(fxmlPath);
        var resource = SceneManager.class.getResource(cleanPath);
        if (resource == null) {
            System.err.println("[SCENEMANAGER] ERRO: FXML nao encontrado (maximizado): " + cleanPath);
            throw new RuntimeException("FXML nao encontrado: " + cleanPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        if (MainFX.getSpringContext() != null) {
            loader.setControllerFactory(MainFX.getSpringContext()::getBean);
        }

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("[SCENEMANAGER] ERRO ao carregar FXML (maximizado): " + cleanPath);
            e.printStackTrace();
            throw e;
        }

        aplicarCenaPreservandoJanela(root, cssPath);
    }

    private static void aplicarCenaPreservandoJanela(Parent root, String cssPath) {
        boolean eraMaximizado = mainStage.isMaximized();
        boolean eraFullScreen = mainStage.isFullScreen();
        double larguraCena = larguraCenaAtual();
        double alturaCena = alturaCenaAtual();
        double larguraJanela = larguraJanelaAtual(larguraCena);
        double alturaJanela = alturaJanelaAtual(alturaCena);
        double x = mainStage.getX();
        double y = mainStage.getY();

        Scene scene = new Scene(root, larguraCena, alturaCena);
        if (cssPath != null && !cssPath.isEmpty()) {
            var cssResource = SceneManager.class.getResource(cssPath);
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }
        }

        mainStage.setScene(scene);

        if (eraFullScreen) {
            mainStage.setFullScreen(true);
        } else if (eraMaximizado) {
            mainStage.setMaximized(true);
        } else {
            mainStage.setX(x);
            mainStage.setY(y);
            mainStage.setWidth(larguraJanela);
            mainStage.setHeight(alturaJanela);
        }

        mainStage.show();
    }

    private static double larguraCenaAtual() {
        if (mainStage.getScene() != null && mainStage.getScene().getWidth() > 0) {
            return mainStage.getScene().getWidth();
        }
        return Math.max(mainStage.getWidth(), 1);
    }

    private static double alturaCenaAtual() {
        if (mainStage.getScene() != null && mainStage.getScene().getHeight() > 0) {
            return mainStage.getScene().getHeight();
        }
        return Math.max(mainStage.getHeight(), 1);
    }

    private static double larguraJanelaAtual(double fallback) {
        return mainStage.getWidth() > 0 ? mainStage.getWidth() : fallback;
    }

    private static double alturaJanelaAtual(double fallback) {
        return mainStage.getHeight() > 0 ? mainStage.getHeight() : fallback;
    }
}

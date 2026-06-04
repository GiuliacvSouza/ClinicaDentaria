package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class MainFX extends Application {

    private static ConfigurableApplicationContext springContext;

    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }

    @Override
    public void init() {

        springContext = SpringApplication.run(MainApplication.class);

        String[] weights = {
                "Regular", "Bold", "Light", "Medium",
                "SemiBold", "ExtraBold", "Black",
                "Italic", "BoldItalic", "LightItalic"
        };
        for (String w : weights) {
            Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins/Poppins-" + w + ".ttf"),
                    14
            );
        }
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login-view.fxml")
        );
        loader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                getClass().getResource("/css/login-style.css").toExternalForm()
        );

        stage.setTitle("Clínica Dentária");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setWidth(1280);
        stage.setHeight(780);
        stage.setScene(scene);
        SceneManager.setMainStage(stage);
        stage.show();
        stage.centerOnScreen();
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

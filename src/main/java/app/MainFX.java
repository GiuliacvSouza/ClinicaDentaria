package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MainFX extends Application {

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        //Arranca o Spring Boot
        springContext = SpringApplication.run(MainFX.class);

        //Carrega as fontes Poppins AQUI (init corre antes do start)
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
        //Carrega o FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login-view.fxml")
        );
        loader.setControllerFactory(springContext::getBean);

        //Cria a cena e aplica o CSS
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                getClass().getResource("/css/login-style.css").toExternalForm()
        );

        //Configura e mostra o Stage
        stage.setTitle("Clínica Dentária");
        stage.setMaximized(true); // abre em fullscreen desktop
        stage.setScene(scene);
        stage.show();
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

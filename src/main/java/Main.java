import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import database.DBConnection;

public class Main extends Application {

    @Override
    public void start(Stage stage) {


        var conn = DBConnection.connect();

        Label label;

        if (conn != null) {
            label = new Label("DB Connected Successfully!");
        } else {
            label = new Label("DB Connection FAILED!");
        }

        try {
            // Използваме относителния път, който работи навсякъде локално
            java.io.File fxmlFile = new java.io.File("./src/main/resources/frontend/views/HomePage.fxml");

            if (!fxmlFile.exists()) {
                System.out.println("❌ Файлът не е намерен на: " + fxmlFile.getAbsolutePath());
            }

            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            Scene scene = new Scene(root, 1280, 768);
            stage.setTitle("GymApp - Начало");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Грешка при зареждането на FXML дизайна!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
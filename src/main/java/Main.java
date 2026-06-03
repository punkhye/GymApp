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
            // Променено: Първоначално зареждаме логин формата вместо HomePage
            java.io.File fxmlFile = new java.io.File("./src/main/resources/frontend/views/LoginPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            // Логин прозорецът се отваря в по-компактен, чист размер
            Scene scene = new Scene(root, 600, 550);

            stage.setTitle("GymApp - Сигурен вход");
            stage.setScene(scene);
            stage.setResizable(false); // Забраняваме разпъването на логин формата
            stage.show();

        } catch (Exception e) {
            System.err.println("Грешка при зареждането на логин дизайна!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
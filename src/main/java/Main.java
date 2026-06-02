import javafx.application.Application;
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

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("JavaFX App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
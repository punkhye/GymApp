package backend.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.File;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // ХАРДКОДНАТИ ДАННИ ЗА АДМИН: Проверка за съвпадение
        if (username.equals("admin") && password.equals("admin123")) {
            lblError.setVisible(false);
            switchToMainApp();
        } else {
            // Показваме червеното съобщение за грешка при грешни данни
            lblError.setVisible(true);
        }
    }

    /**
     * Превключва сцената от Login към основния HomePage интерфейс
     */
    private void switchToMainApp() {
        try {
            // Вземаме текущия прозорец (Stage)
            Stage stage = (Stage) txtUsername.getScene().getWindow();

            // Зареждаме основното табло
            File fxmlFile = new File("./src/main/resources/frontend/views/HomePage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            // Сменяме сцената с десктоп размерите на приложението
            Scene scene = new Scene(root, 1280, 768);
            stage.setScene(scene);
            stage.setTitle("GymApp - Начало");
            stage.centerOnScreen(); // Центрираме новия голям прозорец
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package backend.controllers;

import backend.utils.PasswordUtil;
import backend.utils.SessionManager;
import database.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleLogin() {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Попълни всички полета");
            return;
        }

        String sql = """
                SELECT id,
                       username,
                       password_hash,
                       role,
                       is_active
                FROM users
                WHERE username = ?
                """;

        try {

            PreparedStatement ps =
                    DBConnection.getConnection().prepareStatement(sql);

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                boolean active = rs.getBoolean("is_active");

                // ako akaunta e neaktiven
                if (!active) {
                    showError("Акаунтът е деактивиран. Свържете се с администратор.");
                    return;
                }

                String hash = rs.getString("password_hash");

                // greshna parola
                if (!PasswordUtil.verify(password, hash)) {
                    showError("Грешна парола");
                    return;
                }

                // uspeshno vlizane
                SessionManager.setUser(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                );

                switchToMainApp();

            } else {
                showError("Потребителят не съществува");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Грешка в системата");
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    private void switchToMainApp() {
        try {
            Stage stage = (Stage) txtUsername.getScene().getWindow();

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/frontend/views/HomePage.fxml"));

            Parent root = loader.load();

            Scene scene = new Scene(root, 1280, 768);

            stage.setScene(scene);
            stage.setTitle("GymApp - Начало");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
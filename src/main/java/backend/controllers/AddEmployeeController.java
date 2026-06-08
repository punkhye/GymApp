package backend.controllers;

import backend.utils.PasswordUtil;
import database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.PreparedStatement;

public class AddEmployeeController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleField;
    @FXML private CheckBox activeField;

    @FXML
    public void initialize() {

        roleField.getItems().addAll("ADMIN", "EMPLOYEE");
        roleField.setValue("EMPLOYEE");
        activeField.setSelected(true);
    }

    @FXML
    public void onSave() {

        if (usernameField.getText().isBlank() ||
                passwordField.getText().isBlank()) {
            showAlert("Username и парола са задължителни!");
            return;
        }

        try {

            String sql = """
                INSERT INTO users
                (username, password_hash, first_name, last_name, role, is_active)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement ps =
                    DBConnection.getConnection().prepareStatement(sql);

            String hashedPassword =
                    PasswordUtil.hash(passwordField.getText());

            ps.setString(1, usernameField.getText());
            ps.setString(2, hashedPassword);
            ps.setString(3, firstNameField.getText());
            ps.setString(4, lastNameField.getText());
            ps.setString(5, roleField.getValue());
            ps.setBoolean(6, activeField.isSelected());

            ps.executeUpdate();

            close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Грешка");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
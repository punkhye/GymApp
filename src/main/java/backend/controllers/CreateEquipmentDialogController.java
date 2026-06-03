package backend.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CreateEquipmentDialogController {

    @FXML private TextField nameField;
    @FXML private TextField typeField;
    @FXML private ComboBox<String> statusField;
    @FXML private TextArea notesField;

    private EquipmentController.EquipmentRow result;

    @FXML
    public void initialize() {

        statusField.setItems(FXCollections.observableArrayList(
                "Operational",
                "Under Repair",
                "Out of Service"
        ));

        statusField.setValue("Operational");
    }

    public EquipmentController.EquipmentRow getResult() {
        return result;
    }

    @FXML
    private void onSave() {

        if (nameField.getText().isBlank() || typeField.getText().isBlank()) {
            showAlert("Моля попълни всички полета.");
            return;
        }

        result = new EquipmentController.EquipmentRow(
                "NEW",
                nameField.getText(),
                typeField.getText(),
                statusField.getValue(),
                "",
                notesField.getText()
        );

        close();
    }

    @FXML
    private void onCancel() {
        result = null;
        close();
    }

    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
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
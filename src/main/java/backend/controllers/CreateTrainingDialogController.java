package backend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class CreateTrainingDialogController {

    @FXML private TextField typeField;
    @FXML private TextField trainerField;
    @FXML private TextField hallField;
    @FXML private DatePicker dateField;
    @FXML private TextField timeField;
    @FXML private Spinner<Integer> capacityField;
    private ScheduleController.TrainingRow result;
    @FXML
    public void initialize() {
        capacityField.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(
                        1,
                        100,
                        20
                )
        );
    }

    /**
     * Връща въведените данни.
     */
    public ScheduleController.TrainingRow getResult() {
        return result;
    }

    /**
     * Проверява дали е натиснат Save.
     */
    public boolean hasResult() {
        return result != null;
    }

    public String getType() {
        return result.getType();
    }

    public String getTrainer() {
        return result.getTrainer();
    }

    public String getHall() {
        return result.getHall();
    }

    public Integer getCapacity() {
        return result.getCapacity();
    }

    @FXML
    private void onSave() {
        // 1. Проверяваме абсолютно всички текстови полета + DatePicker-а
        if (typeField.getText().isBlank() ||
                trainerField.getText().isBlank() ||
                hallField.getText().isBlank() ||
                timeField.getText().isBlank() ||
                dateField.getValue() == null) {

            showAlert("Моля, попълнете абсолютно всички полета и изберете дата.");
            return;
        }

        // 2. Вече е напълно безопасно да вземем стойността на датата
        String dateStr = dateField.getValue().toString();

        result = new ScheduleController.TrainingRow(
                "NEW",
                typeField.getText(),
                trainerField.getText(),
                hallField.getText(),
                dateStr,
                timeField.getText(),
                capacityField.getValue()
        );
        close();
    }

    @FXML
    private void onCancel() {
        result = null;
        close();
    }

    private void close() {
        Stage stage = (Stage)
                typeField
                        .getScene()
                        .getWindow();
        stage.close();
    }

    private void showAlert(
            String msg
    ) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle("Грешка");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
package backend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDate;

public class CreateTrainingDialogController {

    @FXML private ComboBox<String> typeField;
    @FXML private ComboBox<String> trainerField;
    @FXML private TextField hallField;
    @FXML private DatePicker dateField;
    @FXML private TextField timeField;
    @FXML private Spinner<Integer> capacityField;
    @FXML private Button deleteButton;

    private ScheduleController.TrainingRow result;
    private Integer editId = null;
    private boolean editMode = false;
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

        deleteButton.setVisible(false);
        loadTypes();
        loadTrainers();

    }

    private void loadTypes() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                            """
                            SELECT name
                            FROM workout_types
                            ORDER BY name
                            """
                    );
            ResultSet rs = stmt.executeQuery();

            typeField
                    .getItems()
                    .clear();
            while (
                    rs.next()
            ) {
                typeField.getItems().add(rs.getString("name"));
            }

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

    private void loadTrainers() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    """
                    SELECT
                    first_name,
                    last_name
                    FROM coaches
                    ORDER BY first_name
                    """
                    );

            ResultSet rs = stmt.executeQuery();
            trainerField
                    .getItems()
                    .clear();

            while (rs.next()) {
                trainerField
                        .getItems()
                        .add(
                                rs.getString("first_name") + " " + rs.getString("last_name"));

            }

        }

        catch (Exception e) {
            e.printStackTrace();

        }

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
        if (typeField.getEditor().getText().isBlank() ||
            trainerField.getEditor().getText().isBlank() ||
            hallField.getText().isBlank() ||
            timeField.getText().isBlank() ||
            dateField.getValue() == null

        ) {

            showAlert("Попълни всички полета.");

            return;

        }

        if (editMode) {
            result = new ScheduleController.TrainingRow(String.valueOf(editId),
                    typeField.getEditor().getText(),
                    trainerField.getEditor().getText(),
                    hallField.getText(),
                    dateField.getValue().toString(),
                    timeField.getText(),
                    capacityField.getValue()
            );
            close();

            return;

        }

        try {
            Connection conn = DBConnection.getConnection();

            PreparedStatement stmt = conn.prepareStatement(
                    """
                    INSERT INTO schedules
                    (
                    workout_type_id,
                    coach_id,
                    hall_name,
                    start_time
                    )
                    VALUES
                    (
                    (
                    SELECT id
                    FROM workout_types
                    WHERE name = ?
                    LIMIT 1
                    ),
                    
                    (
                    SELECT id
                    FROM coaches
                    WHERE (first_name || ' ' || last_name) = ?
                    LIMIT 1
                    ),
                    
                    ?,
                    
                    ?
                    )
                    
                    """

            );

            stmt.setString(1, typeField.getEditor().getText());

            stmt.setString(2, trainerField.getEditor().getText());

            stmt.setString(3, hallField.getText());

            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(dateField.getValue() + " " + timeField.getText() + ":00"));

            stmt.executeUpdate();

            System.out.println("TRAINING SAVED"
            );

        }

        catch (Exception e) {
            e.printStackTrace();
        }

        result = new ScheduleController.TrainingRow(
                "NEW",
                typeField.getEditor().getText(),
                trainerField.getEditor().getText(),
                hallField.getText(),
                dateField.getValue().toString(),
                timeField.getText(),
                capacityField.getValue()

        );

        close();

    }

    @FXML
    private void onDelete() {
        if (editId == null) {
            return;
        }
        try {Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
            """
            DELETE
            FROM schedules
            WHERE id=?
            """
                    );

            stmt.setInt(1, editId);
            stmt.executeUpdate();
            System.out.println("TRAINING DELETED");
            result = null;
            close();
        }

        catch (Exception e) {
            e.printStackTrace();
        }

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

    public void loadTraining(
            String type,
            String trainer,
            String hall,
            String date,
            String time,
            Integer capacity
    ) {

        typeField.setValue(type);
        trainerField.setValue(trainer);
        hallField.setText(hall);
        dateField.setValue(LocalDate.parse(date));
        timeField.setText(time);
        capacityField.getValueFactory().setValue(capacity);
    }

    public void setEditMode(
            Integer id
    ) {

        editMode = true;

        editId = id;

        deleteButton.setVisible(
                true
        );

    }
}
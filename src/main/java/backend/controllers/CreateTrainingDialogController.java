package backend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
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
    private ScheduleController.TrainingRow result;

    @FXML public void initialize() {
        capacityField.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(
                        1,
                        100,
                        20
                )
        );

        loadWorkoutTypes();

        loadCoaches();

    }

    private void loadWorkoutTypes() {
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement st =
                        conn.prepareStatement(
                                "SELECT name FROM workout_types ORDER BY name"
                        )
        ) {

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                typeField
                        .getItems()
                        .add(rs.getString("name")
                        );
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadCoaches() {
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement st =
                        conn.prepareStatement(
                                """
                                SELECT
                                first_name,
                                last_name
                                FROM coaches
                                ORDER BY first_name
                                """
                        )
        ) {
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                trainerField
                        .getItems()
                        .add(rs.getString("first_name") + " " + rs.getString("last_name"));
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
        if (
                typeField.getEditor().getText().isBlank() ||
                trainerField.getEditor().getText().isBlank() ||
                hallField.getText().isBlank() ||
                timeField.getText().isBlank() ||
                dateField.getValue() == null
        ) {

            showAlert("Моля, попълнете всички полета.");
            return;

        }

        String dateStr = dateField
                                    .getValue()
                                    .toString();

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
                    (SELECT id
                    FROM workout_types
                    WHERE name=?
                    LIMIT 1),
                    
                    (SELECT id
                    FROM coaches
                    WHERE first_name || ' ' || last_name=?
                    LIMIT 1),
                    
                    ?,
                    
                    ?
                    )
                    """
            );

            stmt.setString(1, typeField
                                                     .getEditor()
                                                     .getText()
            );

            stmt.setString(2, trainerField
                                                        .getEditor()
                                                        .getText()
            );

            stmt.setString(3, hallField
                                                    .getText()
            );

            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(dateStr + " " + timeField.getText() + ":00")

            );

            stmt.executeUpdate();

            System.out.println("TRAINING SAVED");

        }

        catch (Exception e) {
            e.printStackTrace();

        }

        result = new ScheduleController.TrainingRow(
                "NEW",
                typeField
                        .getEditor()
                        .getText(),
                trainerField
                        .getEditor()
                        .getText(),
                hallField
                        .getText(),
                        dateStr,
                timeField
                        .getText(),
                capacityField
                        .getValue()

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
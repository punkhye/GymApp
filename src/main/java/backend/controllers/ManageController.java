package backend.controllers;

import database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ManageController {
    @FXML
    private TextField trainerField;
    @FXML
    private TextField sportField;
    @FXML
    private void addTrainer() {
        try {
            Connection conn = DBConnection.getConnection();
            String[] names = trainerField
                            .getText()
                            .trim()
                            .split(" ", 2);

            PreparedStatement stmt = conn.prepareStatement(
                            """
                                INSERT INTO coaches
                               (
                               first_name,
                               last_name,
                               phone
                               )
                               VALUES
                               (
                               ?,
                               ?,
                               ?
                               )
                            """
                    );
            stmt.setString(1, names[0]);
            stmt.setString(2, names.length > 1 ? names[1] : "-");
            stmt.setString(3, "-");


            stmt.executeUpdate();

            trainerField.clear();
            System.out.println("TRAINER ADDED"
            );

        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void addSport() {

        try {

            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(
                            """
                            INSERT INTO workout_types
                            (
                            name,
                            duration_minutes,
                            max_participants
                            )
                            VALUES
                            (
                            ?,
                            ?,
                            ?
                            )
                            """
                    );

            stmt.setString(
                    1,
                    sportField.getText()
            );

            stmt.setInt(
                    2,
                    60
            );

            stmt.setInt(
                    3,
                    20
            );

            stmt.executeUpdate();

            sportField.clear();

            System.out.println(
                    "SPORT ADDED"
            );

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

}
package backend.controllers;

import database.DBConnection;

import java.sql.*;

public class ScheduleService {

    public static void saveSchedule(

            String workoutName,
            String trainerName,
            String hall,
            String date,
            String time

    ) {

        try {

            Connection conn =
                    DBConnection.getConnection();

            // намираме id на тренировката
            PreparedStatement workoutQuery =

                    conn.prepareStatement(

                            """
                            SELECT id
                            FROM workout_types
                            WHERE name = ?
                            """

                    );

            workoutQuery.setString(
                    1,
                    workoutName
            );

            ResultSet workoutRs =
                    workoutQuery.executeQuery();

            if (!workoutRs.next()) {

                throw new RuntimeException(
                        "Няма такъв тип тренировка"
                );

            }

            int workoutId =
                    workoutRs.getInt("id");

            // намираме coach id

            String[] names =
                    trainerName.split(" ");

            PreparedStatement coachQuery =

                    conn.prepareStatement(

                            """
                            SELECT id
                            FROM coaches
                            WHERE first_name = ?
                            """

                    );

            coachQuery.setString(
                    1,
                    names[0]
            );

            ResultSet coachRs =
                    coachQuery.executeQuery();

            if (!coachRs.next()) {

                throw new RuntimeException(
                        "Няма такъв треньор"
                );

            }

            int coachId =
                    coachRs.getInt("id");

            // запис

            PreparedStatement insert =

                    conn.prepareStatement(

                            """
                            INSERT INTO schedules
                            (
                                workout_type_id,
                                coach_id,
                                hall_name,
                                start_time
                            )

                            VALUES

                            (?, ?, ?, ?)
                            """

                    );

            insert.setInt(
                    1,
                    workoutId
            );

            insert.setInt(
                    2,
                    coachId
            );

            insert.setString(
                    3,
                    hall
            );

            insert.setTimestamp(

                    4,

                    Timestamp.valueOf(
                            date
                                    +
                                    " "
                                    +
                                    time
                                    +
                                    ":00"
                    )

            );

            insert.executeUpdate();

            conn.close();

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

}
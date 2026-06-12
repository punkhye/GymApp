package backend.controllers;

import database.DBConnection;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReportsController {

    // --- JavaFX Елементи за графики и статистика ---
    @FXML private PieChart revenueChart; // Кръгова графика за финансовия отчет

    @FXML private Label totalRevenueLabel;

    // --- JavaFX Елементи за матрицата на треньорите ---
    @FXML private TableView<CoachRow> coachTable;
    @FXML private TableColumn<CoachRow, String> colCoachName;
    @FXML private TableColumn<CoachRow, String> colSpecialization;
    @FXML private TableColumn<CoachRow, Number> colClassesCount; // Използва Number за целочислени стойности
    @FXML private TableColumn<CoachRow, Number> colTotalMembers;

    // Референция към главния контролер за координация на навигацията
    private HomeController mainController;
    @FXML private VBox workoutPopularityContainer;

    // tozi mesec
    private LocalDate startDate = LocalDate.now().withDayOfMonth(1);
    private LocalDate endDate = LocalDate.now();


    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // 1. НАСТРОЙКА НА ГРАФИКАТА: Дефиниране и зареждане на секторите в PieChart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Месечна карта (60%)", 60),
                new PieChart.Data("Карта 10 посещения (25%)", 25),
                new PieChart.Data("Годишен абонамент (15%)", 15)
        );
        revenueChart.setData(pieChartData);

        // 2. СВЪРЗВАНЕ НА ТАБЛИЦАТА: Обвързване на колоните със свойствата на модела CoachRow
        colCoachName.setCellValueFactory(d -> d.getValue().name);
        colSpecialization.setCellValueFactory(d -> d.getValue().specialization);
        colClassesCount.setCellValueFactory(d -> d.getValue().classesCount);
        colTotalMembers.setCellValueFactory(d -> d.getValue().totalMembers);

        // Демонстрационни бизнес данни за натовареността на фитнес треньорите
        ObservableList<CoachRow> coachData = FXCollections.observableArrayList();
        loadCoachPerformance();
        loadRevenue();
        loadWorkoutPopularity();

    }

    // Помощен вътрешен клас (Модел) за представяне на ред от статистиката за треньори
    public static class CoachRow {
        public SimpleStringProperty name;
        public SimpleStringProperty specialization;
        public SimpleIntegerProperty classesCount;
        public SimpleIntegerProperty totalMembers;

        public CoachRow(String name, String spec, int classes, int members) {
            this.name = new SimpleStringProperty(name);
            this.specialization = new SimpleStringProperty(spec);
            this.classesCount = new SimpleIntegerProperty(classes);
            this.totalMembers = new SimpleIntegerProperty(members);
        }
    }

    private void loadCoachPerformance() {
        String sql =
                "SELECT c.first_name || ' ' || c.last_name AS coach_name, " +
                        "       c.specializations, " +
                        "       COUNT(DISTINCT s.id) AS classes_count, " +
                        "       COUNT(wr.id) AS total_members " +
                        "FROM coaches c " +
                        "JOIN schedules s ON s.coach_id = c.id " +
                        "LEFT JOIN workout_registrations wr ON wr.schedule_id = s.id AND wr.attended = TRUE " +
                        "WHERE s.start_time BETWEEN ? AND ? " +
                        "AND c.is_active = TRUE " +
                        "GROUP BY c.id, c.first_name, c.last_name, c.specializations " +
                        "ORDER BY classes_count DESC";

        ObservableList<CoachRow> coachData = FXCollections.observableArrayList();

        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(endDate.atTime(23, 59, 59)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coachData.add(new CoachRow(
                            rs.getString("coach_name"),
                            rs.getString("specializations"),
                            rs.getInt("classes_count"),
                            rs.getInt("total_members")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        coachTable.setItems(coachData);
    }

    private void loadRevenue() {
        String sql = "SELECT COALESCE(SUM(amount_paid), 0) AS total_revenue " +
                "FROM member_subscriptions " +
                "WHERE purchase_date BETWEEN ? AND ?";

        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("total_revenue");
                    totalRevenueLabel.setText(String.format("%,.2f EURO", total));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            totalRevenueLabel.setText("Грешка");
        }
    }

    private void loadWorkoutPopularity() {

        String sql =
                "SELECT " +
                        "wt.name, " +
                        "COUNT(s.id) AS schedules_count, " +
                        "wt.max_participants, " +
                        "COALESCE(COUNT(wr.id), 0) AS registrations, " +
                        "CASE " +
                        "   WHEN COUNT(s.id) = 0 THEN 0 " +
                        "   ELSE ROUND( " +
                        "       (COALESCE(COUNT(wr.id),0)::decimal / " +
                        "       (COUNT(s.id) * wt.max_participants)) * 100, 0 " +
                        "   ) " +
                        "END AS occupancy " +
                        "FROM workout_types wt " +
                        "JOIN schedules s ON s.workout_type_id = wt.id " +
                        "LEFT JOIN workout_registrations wr ON wr.schedule_id = s.id " +
                        "GROUP BY wt.id, wt.name, wt.max_participants " +
                        "HAVING COUNT(s.id) > 0 " +
                        "ORDER BY occupancy DESC";

        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            workoutPopularityContainer.getChildren().clear();

            while (rs.next()) {

                String name = rs.getString("name");
                int occupancy = rs.getInt("occupancy");

                System.out.println(name + " -> " + occupancy + "%");

                VBox card = new VBox(5);
                card.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 10; -fx-background-radius: 6;");

                Label title = new Label(name);
                title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                Label percent = new Label("Популярност: " + occupancy + "%");

                double progressValue = Math.max(0, Math.min(1, occupancy / 100.0));
                ProgressBar bar = new ProgressBar(progressValue);

                card.getChildren().addAll(title, percent, bar);

                workoutPopularityContainer.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
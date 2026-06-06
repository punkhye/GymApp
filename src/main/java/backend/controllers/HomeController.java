package backend.controllers;

import backend.models.ScheduleItem;
import backend.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import database.DBConnection;

public class HomeController {
    @FXML private Label lblLoggedUser;
    @FXML private Label lblActiveMembers;
    @FXML private Label lblExpiringSubscriptions;
    @FXML private Label lblMonthlyRevenue;
    // --- Таблица за днешния график (Начален екран / Dashboard) ---
    @FXML private TableView<ScheduleItem> scheduleTable;
    @FXML private TableColumn<ScheduleItem, String> colTime;
    @FXML private TableColumn<ScheduleItem, String> colClassName;
    @FXML private TableColumn<ScheduleItem, String> colCoach;
    @FXML private TableColumn<ScheduleItem, String> colHall;
    @FXML private TableColumn<ScheduleItem, String> colSlots;

    // --- Бутони от страничното меню (Sidebar Navigation) ---
    @FXML private Button btnDashboard;
    @FXML private Button btnMembers;
    @FXML private Button btnSchedule;
    @FXML private Button btnEquipment;
    @FXML private Button btnReports;
    @FXML private Button btnEmployees;

    // Главният контейнер на приложението. В неговия център (.setCenter) сменяме екраните динамично
    @FXML private BorderPane mainLayout;

    // Кеш за началния изглед (Dashboard overview), за да не го презареждаме от FXML всеки път
    private javafx.scene.control.ScrollPane dashboardView;

    @FXML
    public void initialize() {
        lblLoggedUser.setText("Служител: " + SessionManager.getLoggedInUsername());

        // 2. Инициализиране на колоните на таблицата
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colCoach.setCellValueFactory(new PropertyValueFactory<>("coach"));
        colHall.setCellValueFactory(new PropertyValueFactory<>("hall"));
        colSlots.setCellValueFactory(new PropertyValueFactory<>("slots"));

        // 3. Зареждане на данните
        loadKPIStats();
        loadTodaySchedule();
    }


    private void loadKPIStats() {
        String queryActive = "SELECT COUNT(*) FROM members";
        String queryExpiring = "SELECT COUNT(*) FROM member_subscriptions WHERE end_date = CURRENT_DATE + INTERVAL '7 days'";

        // 🌟 ВЕЧЕ Е НАПЪЛНО ТОЧНО И РЕАЛНО:
        // Използваме вашите истински колони amount_paid и purchase_date
        String queryRevenue =
                "SELECT COALESCE(SUM(amount_paid), 0) FROM member_subscriptions " +
                        "WHERE purchase_date >= DATE_TRUNC('month', CURRENT_DATE)";

        try {
            Connection conn = database.DBConnection.getConnection();

            // 1. Активни членове
            try (PreparedStatement stmt = conn.prepareStatement(queryActive); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) lblActiveMembers.setText(String.format("%,d", rs.getInt(1)));
            }

            // 2. Изтичащи абонаменти
            try (PreparedStatement stmt = conn.prepareStatement(queryExpiring); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) lblExpiringSubscriptions.setText(String.valueOf(rs.getInt(1)));
            }

            // 3. Месечен приход
            try (PreparedStatement stmt = conn.prepareStatement(queryRevenue); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double revenue = rs.getDouble(1);
                    lblMonthlyRevenue.setText(String.format("%,.2f ЕВРО", revenue));
                }
            }

        } catch (Exception e) {
            System.err.println("Грешка при зареждане на KPI статистиките!");
            e.printStackTrace();
        }
    }

    private void loadTodaySchedule() {
        ObservableList<ScheduleItem> todayClasses = FXCollections.observableArrayList();


        // Събираме истинските имена на тренировките, треньорите и смятаме свободните места динамично!
        String querySchedule =
                "SELECT " +
                        "   TO_CHAR(s.start_time, 'HH24:MI') AS class_time, " +
                        "   wt.name AS class_name, " +
                        "   (c.first_name || ' ' || c.last_name) AS coach_name, " +
                        "   s.hall_name AS room_name, " +
                        "   wt.max_participants AS max_slots, " +
                        "   (SELECT COUNT(*) FROM workout_registrations wr WHERE wr.schedule_id = s.id) AS booked_slots " +
                        "FROM schedules s " +
                        "JOIN workout_types wt ON s.workout_type_id = wt.id " +
                        "JOIN coaches c ON s.coach_id = c.id " +
                        "WHERE s.start_time::date = CURRENT_DATE " +
                        "ORDER BY s.start_time ASC";

        try {
            Connection conn = database.DBConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(querySchedule);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String time = rs.getString("class_time");
                    String className = rs.getString("class_name");
                    String coach = rs.getString("coach_name");
                    String room = rs.getString("room_name");

                    // Изчисляваме реалните свободни места спрямо капацитета на тренировката
                    int maxSlots = rs.getInt("max_slots");
                    int bookedSlots = rs.getInt("booked_slots");
                    int freeSlots = maxSlots - bookedSlots;

                    String slotsText = freeSlots + " / " + maxSlots;

                    todayClasses.add(new ScheduleItem(time, className, coach, "Зала " + room, slotsText));
                }
                scheduleTable.setItems(todayClasses);
            }

        } catch (Exception e) {
            System.err.println("Грешка при зареждане на графика за днес!");
            e.printStackTrace();
        }
    }
    // --- СЪБИТИЯ ПРИ КЛИК ВЪРХУ МЕНЮТАТА (Single Page Application Рутиране) ---

    @FXML
    public void handleMembersMenu() {
        try {
            // Зареждаме външния под-екран за Членовете
            File fxmlFile = new File("./src/main/resources/frontend/views/MembersPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            VBox membersView = loader.load();

            // Впръскваме референция на текущия HomeController в под-контролера (за обратна връзка)
            MembersController membersController = loader.getController();
            membersController.setMainController(this);

            // Инжектираме новия VBox в центъра на основния BorderPane и сменяме активния бутон в менюто
            mainLayout.setCenter(membersView);
            setActiveMenu(btnMembers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleScheduleMenu() {
        try {
            File fxmlFile = new File("./src/main/resources/frontend/views/SchedulePage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            VBox scheduleView = loader.load();

            ScheduleController scheduleController = loader.getController();
            scheduleController.setMainController(this);

            mainLayout.setCenter(scheduleView);
            setActiveMenu(btnSchedule);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEquipmentMenu() {
        try {
            File fxmlFile = new File("./src/main/resources/frontend/views/EquipmentPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            VBox equipmentView = loader.load();

            EquipmentController equipmentController = loader.getController();
            equipmentController.setMainController(this);

            mainLayout.setCenter(equipmentView);
            setActiveMenu(btnEquipment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEmployeesMenu() {
        try {
            File fxmlFile = new File("./src/main/resources/frontend/views/EmployeesPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            VBox equipmentView = loader.load();

            EmployeesController employeesController = loader.getController();
            employeesController.setMainController(this);

            mainLayout.setCenter(equipmentView);
            setActiveMenu(btnEmployees);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleReportsMenu() {
        try {
            File fxmlFile = new File("./src/main/resources/frontend/views/ReportsPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            ScrollPane reportsView = loader.load(); // Тук коренът е ScrollPane

            ReportsController reportsController = loader.getController();
            reportsController.setMainController(this);

            mainLayout.setCenter(reportsView);
            setActiveMenu(btnReports);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDashboardMenu() {
        try {
            File fxmlFile = new File("./src/main/resources/frontend/views/HomePage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());

            // 🌟 ПРАВИЛНО: Кастваме към BorderPane, защото това е коренът на FXML файла!
            BorderPane homeView = loader.load();

            // Вземаме текущия прозорец (Stage) и сменяме цялата сцена
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            Scene scene = new Scene(homeView, 1280, 768);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Грешка при зареждане на HomePage.fxml!");
            e.printStackTrace();
        }
    }
    @FXML
    public void handleLogout() {
        try {
            // Вземаме текущата сцена и я връщаме в първоначално състояние
            Stage stage = (Stage) btnDashboard.getScene().getWindow();

            File fxmlFile = new File("./src/main/resources/frontend/views/LoginPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            Scene scene = new Scene(root, 600, 550);
            stage.setScene(scene);
            stage.setTitle("GymApp - Сигурен вход");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Помощна UI Логика ---

    /**
     * Превключва CSS класовете на бутоните в менюто.
     * Премахва зеления цвят (.sidebar-btn-active) от стария бутон и го слага на новонатиснатия.
     */
    private void setActiveMenu(Button activeButton) {
        btnDashboard.getStyleClass().remove("sidebar-btn-active");
        btnMembers.getStyleClass().remove("sidebar-btn-active");
        btnSchedule.getStyleClass().remove("sidebar-btn-active");
        btnEquipment.getStyleClass().remove("sidebar-btn-active");
        btnReports.getStyleClass().remove("sidebar-btn-active");
        btnEmployees.getStyleClass().remove("sidebar-btn-active");

        if (!activeButton.getStyleClass().contains("sidebar-btn-active")) {
            activeButton.getStyleClass().add("sidebar-btn-active");
        }
    }
}
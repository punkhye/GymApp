package backend.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

public class HomeController {

    // --- Таблица за днешния график (Начален екран / Dashboard) ---
    @FXML private TableView<String[]> scheduleTable;
    @FXML private TableColumn<String[], String> colTime;
    @FXML private TableColumn<String[], String> colClassName;
    @FXML private TableColumn<String[], String> colCoach;
    @FXML private TableColumn<String[], String> colHall;
    @FXML private TableColumn<String[], String> colSlots;

    // --- Бутони от страничното меню (Sidebar Navigation) ---
    @FXML private Button btnDashboard;
    @FXML private Button btnMembers;
    @FXML private Button btnSchedule;
    @FXML private Button btnEquipment;
    @FXML private Button btnReports;

    // Главният контейнер на приложението. В неговия център (.setCenter) сменяме екраните динамично
    @FXML private BorderPane mainLayout;

    // Кеш за началния изглед (Dashboard overview), за да не го презареждаме от FXML всеки път
    private javafx.scene.control.ScrollPane dashboardView;

    @FXML
    public void initialize() {
        // Ръчно свързване на колоните на таблицата с индексите от масива String[]
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        colClassName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colCoach.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colHall.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        colSlots.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));

        // При първоначално стартиране взимаме и запазваме ScrollPane-а, който е зареден в центъра
        dashboardView = (javafx.scene.control.ScrollPane) mainLayout.getCenter();

        // Примерни статични данни (Mock data) за днешния фитнес график
        ObservableList<String[]> data = FXCollections.observableArrayList(
                new String[]{"08:30", "Кросфит сутрин", "Алекс Тодоров", "Зала А", "15 / 20 Свободни"},
                new String[]{"12:00", "Йога за начинаещи", "Мария Петрова", "Зала Б", "12 / 15 Свободни"},
                new String[]{"18:30", "Бокс интензивен", "Димитър Берберов", "Зала Бокс", "🔥 Само 3 / 20 места остават! (Почти пълно)"},
                new String[]{"19:30", "Аеробика", "Елена Георгиева", "Зала Б", "8 / 25 Свободни"}
        );
        scheduleTable.setItems(data);
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
            setActiveMenu(btnEquipment);
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
        // Връщане към началния екран: просто възстановяваме запазения в initialize() оригинален изглед
        if (dashboardView != null) {
            mainLayout.setCenter(dashboardView);
            setActiveMenu(btnDashboard);
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

        if (!activeButton.getStyleClass().contains("sidebar-btn-active")) {
            activeButton.getStyleClass().add("sidebar-btn-active");
        }
    }
}
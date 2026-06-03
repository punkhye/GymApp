package backend.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.File;

public class HomeController {

    @FXML private TableView<String[]> scheduleTable;
    @FXML private TableColumn<String[], String> colTime;
    @FXML private TableColumn<String[], String> colClassName;
    @FXML private TableColumn<String[], String> colCoach;
    @FXML private TableColumn<String[], String> colHall;
    @FXML private TableColumn<String[], String> colSlots;
    @FXML private Button btnDashboard;
    @FXML private Button btnMembers;
    @FXML private Button btnSchedule;
    @FXML private Button btnEquipment;
    @FXML private Button btnReports;
    private javafx.scene.control.ScrollPane dashboardView;
    @FXML
    public void initialize() {
        // Свързваме колоните с данните в масива
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        colClassName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colCoach.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colHall.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        colSlots.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));
        dashboardView = (javafx.scene.control.ScrollPane) mainLayout.getCenter();
        // Примерни данни (днешния график)
        ObservableList<String[]> data = FXCollections.observableArrayList(
                new String[]{"08:30", "Кросфит сутрин", "Алекс Тодоров", "Зала А", "15 / 20 Свободни"},
                new String[]{"12:00", "Йога за начинаещи", "Мария Петрова", "Зала Б", "12 / 15 Свободни"},
                new String[]{"18:30", "Бокс интензивен", "Димитър Берберов", "Зала Бокс", "🔥 Само 3 / 20 места остават! (Почти пълно)"},
                new String[]{"19:30", "Аеробика", "Елена Георгиева", "Зала Б", "8 / 25 Свободни"}
        );

        scheduleTable.setItems(data);
    }
    @FXML private BorderPane mainLayout; // Трябва да съвпада с fx:id на BorderPane

    @FXML
    public void handleMembersMenu() {
        try {
            File fxmlFile = new File("./src/main/resources/frontend/views/MembersPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            VBox membersView = loader.load();

            MembersController membersController = loader.getController();
            membersController.setMainController(this);

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
    public void handleReportsMenu() {
        try {
            File fxmlFile = new File("./src/main/resources/frontend/views/ReportsPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            ScrollPane reportsView = loader.load();

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
        if (dashboardView != null) {
            mainLayout.setCenter(dashboardView);
            setActiveMenu(btnDashboard);
        }
    }

    // Помощен метод, който управлява кое свети в зелено
    private void setActiveMenu(Button activeButton) {
        btnDashboard.getStyleClass().remove("sidebar-btn-active");
        btnMembers.getStyleClass().remove("sidebar-btn-active");
        btnSchedule.getStyleClass().remove("sidebar-btn-active");
        btnEquipment.getStyleClass().remove("sidebar-btn-active");
        btnReports.getStyleClass().remove("sidebar-btn-active"); // Изчистваме отчетите

        if (!activeButton.getStyleClass().contains("sidebar-btn-active")) {
            activeButton.getStyleClass().add("sidebar-btn-active");
        }
    }
}
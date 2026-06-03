package backend.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ReportsController {

    @FXML private PieChart revenueChart;

    @FXML private TableView<CoachRow> coachTable;
    @FXML private TableColumn<CoachRow, String> colCoachName;
    @FXML private TableColumn<CoachRow, String> colSpecialization;
    @FXML private TableColumn<CoachRow, Number> colClassesCount;
    @FXML private TableColumn<CoachRow, Number> colTotalMembers;

    private HomeController mainController;

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // 1. Попълване на Финансовия пай-чарт
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Месечна карта (60%)", 60),
                new PieChart.Data("Карта 10 посещения (25%)", 25),
                new PieChart.Data("Годишен абонамент (15%)", 15)
        );
        revenueChart.setData(pieChartData);

        // 2. Свързване на колоните за ефективност на треньорите
        colCoachName.setCellValueFactory(d -> d.getValue().name);
        colSpecialization.setCellValueFactory(d -> d.getValue().specialization);
        colClassesCount.setCellValueFactory(d -> d.getValue().classesCount);
        colTotalMembers.setCellValueFactory(d -> d.getValue().totalMembers);

        // Примерни данни съгласно изискванията на заданието
        ObservableList<CoachRow> coachData = FXCollections.observableArrayList(
                new CoachRow("Димитър Петров", "CrossFit", 24, 410),
                new CoachRow("Елена Георгиева", "Йога / Пилатес", 18, 195)
        );
        coachTable.setItems(coachData);
    }

    // Клас за редовете в матрицата на треньорите
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
}
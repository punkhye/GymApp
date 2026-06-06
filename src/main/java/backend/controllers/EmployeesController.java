package backend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class EmployeesController {

    @FXML private TableView<?> employeesTable;
    @FXML private TableColumn<?, ?> colActions;
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colFirstName;
    @FXML private TableColumn<?, ?> colLastName;
    @FXML private TableColumn<?, ?> colUsername;
    @FXML private TableColumn<?, ?> colRole;

    private HomeController mainController;

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

    }
    @FXML
    public void handleAddEmployee() {
        System.out.println("Отваряне на прозорец за нов служител...");
    }
}
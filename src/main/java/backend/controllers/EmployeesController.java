package backend.controllers;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import backend.models.Employee;
import javafx.stage.Stage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeesController {

    @FXML
    private TableView<Employee> employeesTable;

    @FXML
    private TableColumn<Employee, Integer> colId;

    @FXML
    private TableColumn<Employee, String> colFirstName;

    @FXML
    private TableColumn<Employee, String> colLastName;

    @FXML
    private TableColumn<Employee, String> colUsername;

    @FXML
    private TableColumn<Employee, String> colRole;

    @FXML
    private TableColumn<Employee, Void> colActions;

    private HomeController mainController;

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));

        colFirstName.setCellValueFactory(
                new PropertyValueFactory<>("firstName"));

        colLastName.setCellValueFactory(
                new PropertyValueFactory<>("lastName"));

        colUsername.setCellValueFactory(
                new PropertyValueFactory<>("username"));

        colRole.setCellValueFactory(
                new PropertyValueFactory<>("role"));

        colActions.setCellFactory(column -> new javafx.scene.control.TableCell<>() {

            private final javafx.scene.shape.Rectangle box =
                    new javafx.scene.shape.Rectangle(12, 12);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Employee emp = getTableView().getItems().get(getIndex());

                if (emp.isActive()) {
                    box.setFill(javafx.scene.paint.Color.LIMEGREEN);
                } else {
                    box.setFill(javafx.scene.paint.Color.RED);
                }

                setGraphic(box);
            }
        });

        employeesTable.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {

            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);

                if (empty || employee == null) {
                    setStyle("");
                } else if (isSelected()) {
                    setStyle("-fx-background-color: #708090;"); // светло синьо
                } else {
                    setStyle("");
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);

                if (selected) {
                    setStyle("-fx-background-color: #b3d9ff;");
                } else {
                    setStyle("");
                }
            }
        });

        loadEmployees();
    }

    public void loadEmployees() {

        ObservableList<Employee> employees =
                FXCollections.observableArrayList();

        String sql = """
                SELECT id,
                       first_name,
                       last_name,
                       username,
                       role,
                       is_active
                FROM users
                ORDER BY id
                """;

        try {

            PreparedStatement ps =
                    DBConnection.getConnection()
                            .prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Employee employee = new Employee(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getBoolean("is_active")
                );

                employees.add(employee);
            }

            employeesTable.setItems(employees);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddEmployee() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/frontend/views/AddEmployeeDialog.fxml"));

            Parent root = loader.load();

            AddEmployeeController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Добавяне на служител");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadEmployees();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package backend.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;

public class MembersController {

    @FXML private TableView<MemberRow> membersTable;
    @FXML private TableColumn<MemberRow, String> colId;
    @FXML private TableColumn<MemberRow, String> colName;
    @FXML private TableColumn<MemberRow, String> colPhone;
    @FXML private TableColumn<MemberRow, String> colEmail;
    @FXML private TableColumn<MemberRow, String> colStatus;
    @FXML private TableColumn<MemberRow, String> colType;
    @FXML private TableColumn<MemberRow, String> colExpiry;
    @FXML private TableColumn<MemberRow, String> colActions;
    private HomeController mainController;
    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }
    @FXML
    public void navigateBackToDashboard() {
        if (mainController != null) {
            mainController.handleDashboardMenu();
        }
    }
    @FXML
    public void initialize() {
        // Свързване на колоните
        colId.setCellValueFactory(d -> d.getValue().id);
        colName.setCellValueFactory(d -> d.getValue().name);
        colPhone.setCellValueFactory(d -> d.getValue().phone);
        colEmail.setCellValueFactory(d -> d.getValue().email);
        colStatus.setCellValueFactory(d -> d.getValue().status);
        colType.setCellValueFactory(d -> d.getValue().type);
        colExpiry.setCellValueFactory(d -> d.getValue().expiryDate);

        // КРИТИЧНО ИЗИСКВАНЕ: Динамично оцветяване на редовете
        membersTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(MemberRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.rowType.equals("EXPIRING")) {
                    setStyle("-fx-background-color: #FEF3C7;"); // Меко жълто
                } else if (item.rowType.equals("EXPIRED")) {
                    setStyle("-fx-background-color: #FEE2E2;"); // Меко червено
                } else {
                    setStyle(""); // Нормален бял ред
                }
            }
        });

        // Добавяне на иконни бутони (Моливи / Power) в колоната Actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnToggle = new Button("⏻");
            private final HBox container = new HBox(10, btnEdit, btnToggle);
            {
                btnEdit.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-size: 14px;");
                btnToggle.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-font-size: 14px;");
                container.setStyle("-fx-alignment: CENTER;");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // Пълнене с трите изисквани примерни реда
        ObservableList<MemberRow> list = FXCollections.observableArrayList(
                new MemberRow("1", "Георги Петров", "0888123456", "g.petrov@mail.com", "Active", "Monthly Full", "25.06.2026", "NORMAL"),
                new MemberRow("2", "Иван Асенов", "0887998877", "ivan.as@gmail.com", "Active", "Тримесечен", "06.06.2026", "EXPIRING"),
                new MemberRow("3", "Мария Димитрова", "0895112233", "m.dimitrova@abv.bg", "Inactive", "Ката за 10 посещения", "20.05.2026", "EXPIRED")
        );
        membersTable.setItems(list);
    }

    // Помощен мини-клас за структурата на таблицата
    public static class MemberRow {
        public SimpleStringProperty id, name, phone, email, status, type, expiryDate;
        public String rowType; // NORMAL, EXPIRING, EXPIRED

        public MemberRow(String id, String name, String phone, String email, String status, String type, String expiry, String rowType) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.phone = new SimpleStringProperty(phone);
            this.email = new SimpleStringProperty(email);
            this.status = new SimpleStringProperty(status);
            this.type = new SimpleStringProperty(type);
            this.expiryDate = new SimpleStringProperty(expiry);
            this.rowType = rowType;
        }
    }

}
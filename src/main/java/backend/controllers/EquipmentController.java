package backend.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import java.util.Optional;

public class EquipmentController {

    // --- JavaFX Елементи от таблицата за инвентар ---
    @FXML private TableView<EquipmentRow> equipmentTable;
    @FXML private TableColumn<EquipmentRow, String> colId;
    @FXML private TableColumn<EquipmentRow, String> colName;
    @FXML private TableColumn<EquipmentRow, String> colType;
    @FXML private TableColumn<EquipmentRow, String> colStatus;

    // --- JavaFX Елементи от десния панел (Детайли за уреда) ---
    @FXML private Label lblPanelTitle;
    @FXML private Label lblPurchaseDate;
    @FXML private ComboBox<String> comboStatusEdit;
    @FXML private TextArea txtMaintenanceLogs;

    // Връзка с главния контролер за управление на навигацията
    private HomeController mainController;

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Мъпване (свързване) на колоните от таблицата със свойствата на модела EquipmentRow
        colId.setCellValueFactory(d -> d.getValue().id);
        colName.setCellValueFactory(d -> d.getValue().name);
        colType.setCellValueFactory(d -> d.getValue().type);
        colStatus.setCellValueFactory(d -> d.getValue().statusText);

        // Попълване на падащото меню за бърза смяна на статуса
        comboStatusEdit.setItems(FXCollections.observableArrayList("🟢 Operational", "🔧 Under Repair", "❌ Out of Service"));

        // Твърдо зададени (Mock) данни за демонстрация на инвентара
        ObservableList<EquipmentRow> list = FXCollections.observableArrayList(
                new EquipmentRow("EQ-001", "Treadmill Matrix X3", "Cardio", "🟢 Operational", "12 Яну 2024",
                        "05.06.2025 - Сменен ремък и смазан мотор.\n12.11.2025 - Обновен софтуер на екрана от техник."),
                new EquipmentRow("EQ-014", "Squat Rack Rogue", "Strength", "🔧 Under Repair", "20 Мар 2023",
                        "01.05.2026 - Забелязана пукнатина на предпазния щифт.\n03.06.2026 - Поръчан е нов оригинален щифт от Rogue."),
                new EquipmentRow("EQ-022", "Leg Press LifeFitness", "Strength", "❌ Out of Service", "15 Авг 2022",
                        "10.04.2026 - Скъсано стоманено въже.\n25.04.2026 - Доставчикът бави доставката на резервната част.")
        );
        equipmentTable.setItems(list);

        // СЛУШАТЕЛ (Listener): При клик на ред от таблицата, обновяваме инфото в десния панел
        equipmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                lblPanelTitle.setText("Детайли: " + newSelection.name.get());
                lblPurchaseDate.setText(newSelection.purchaseDate);
                comboStatusEdit.setValue(newSelection.statusText.get());
                txtMaintenanceLogs.setText(newSelection.maintenanceLogs);
            }
        });

        // Автоматично селектиране на първия уред при първоначално зареждане на страницата
        if (!list.isEmpty()) {
            equipmentTable.getSelectionModel().select(0);
        }
    }

    // Помощен POJO клас за структурата и данните на един ред в таблицата
    public static class EquipmentRow {
        // Използваме SimpleStringProperty за автоматичен синхрон (Data Binding) с JavaFX колоните
        public SimpleStringProperty id, name, type, statusText;
        public String purchaseDate;
        public String maintenanceLogs;

        public EquipmentRow(String id, String name, String type, String status, String purchaseDate, String logs) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.type = new SimpleStringProperty(type);
            this.statusText = new SimpleStringProperty(status);
            this.purchaseDate = purchaseDate;
            this.maintenanceLogs = logs;
        }
    }
    @FXML
    private void handleAddEquipment() {
        Dialog<EquipmentRow> dialog = new Dialog<>();
        dialog.setTitle("Добавяне на ново оборудване");
        dialog.setHeaderText("Въведи данните за новия уред");

        ButtonType addButtonType = new ButtonType("Добави", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField txtId = new TextField();
        txtId.setPromptText("Напр. EQ-050");

        TextField txtName = new TextField();
        txtName.setPromptText("Напр. Bench Press");

        TextField txtType = new TextField();
        txtType.setPromptText("Напр. Strength");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.setItems(FXCollections.observableArrayList(
                "🟢 Operational",
                "🔧 Under Repair",
                "❌ Out of Service"
        ));
        statusBox.setValue("🟢 Operational");

        TextField txtPurchaseDate = new TextField();
        txtPurchaseDate.setPromptText("Напр. 03 Юни 2026");

        TextArea txtLogs = new TextArea();
        txtLogs.setPromptText("Бележки за поддръжка...");
        txtLogs.setPrefRowCount(4);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Asset ID:"), 0, 0);
        grid.add(txtId, 1, 0);

        grid.add(new Label("Име:"), 0, 1);
        grid.add(txtName, 1, 1);

        grid.add(new Label("Тип:"), 0, 2);
        grid.add(txtType, 1, 2);

        grid.add(new Label("Статус:"), 0, 3);
        grid.add(statusBox, 1, 3);

        grid.add(new Label("Дата на закупуване:"), 0, 4);
        grid.add(txtPurchaseDate, 1, 4);

        grid.add(new Label("Бележки:"), 0, 5);
        grid.add(txtLogs, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (txtId.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Грешка", "Asset ID и име на уреда са задължителни.");
                    return null;
                }

                return new EquipmentRow(
                        txtId.getText().trim(),
                        txtName.getText().trim(),
                        txtType.getText().trim(),
                        statusBox.getValue(),
                        txtPurchaseDate.getText().trim(),
                        txtLogs.getText().trim()
                );
            }

            return null;
        });

        Optional<EquipmentRow> result = dialog.showAndWait();

        result.ifPresent(newEquipment -> {
            equipmentTable.getItems().add(newEquipment);
            equipmentTable.getSelectionModel().select(newEquipment);
            equipmentTable.scrollTo(newEquipment);
            showAlert(Alert.AlertType.INFORMATION, "Успешно", "Новото оборудване е добавено.");
        });
    }

    @FXML
    private void handleSaveChanges() {
        EquipmentRow selected = equipmentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Няма избран уред", "Моля, избери уред от таблицата.");
            return;
        }

        String newStatus = comboStatusEdit.getValue();

        if (newStatus == null || newStatus.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Грешка", "Моля, избери статус.");
            return;
        }

        selected.statusText.set(newStatus);
        selected.maintenanceLogs = txtMaintenanceLogs.getText();

        equipmentTable.refresh();

        showAlert(Alert.AlertType.INFORMATION, "Запазено", "Промените са запазени успешно.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
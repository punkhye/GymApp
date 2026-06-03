package backend.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
}
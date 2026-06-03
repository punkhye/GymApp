package backend.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class EquipmentController {

    @FXML private TableView<EquipmentRow> equipmentTable;
    @FXML private TableColumn<EquipmentRow, String> colId;
    @FXML private TableColumn<EquipmentRow, String> colName;
    @FXML private TableColumn<EquipmentRow, String> colType;
    @FXML private TableColumn<EquipmentRow, String> colStatus;

    @FXML private Label lblPanelTitle;
    @FXML private Label lblPurchaseDate;
    @FXML private ComboBox<String> comboStatusEdit;
    @FXML private TextArea txtMaintenanceLogs;

    private HomeController mainController;

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Свързване на колоните на таблицата
        colId.setCellValueFactory(d -> d.getValue().id);
        colName.setCellValueFactory(d -> d.getValue().name);
        colType.setCellValueFactory(d -> d.getValue().type);
        colStatus.setCellValueFactory(d -> d.getValue().statusText);

        // Настройки на падащото меню в панела за детайли
        comboStatusEdit.setItems(FXCollections.observableArrayList("🟢 Operational", "🔧 Under Repair", "❌ Out of Service"));

        // Примерни данни за инвентара
        ObservableList<EquipmentRow> list = FXCollections.observableArrayList(
                new EquipmentRow("EQ-001", "Treadmill Matrix X3", "Cardio", "🟢 Operational", "12 Яну 2024",
                        "05.06.2025 - Сменен ремък и смазан мотор.\n12.11.2025 - Обновен софтуер на екрана от техник."),
                new EquipmentRow("EQ-014", "Squat Rack Rogue", "Strength", "🔧 Under Repair", "20 Мар 2023",
                        "01.05.2026 - Забелязана пукнатина на предпазния щифт.\n03.06.2026 - Поръчан е нов оригинален щифт от Rogue."),
                new EquipmentRow("EQ-022", "Leg Press LifeFitness", "Strength", "❌ Out of Service", "15 Авг 2022",
                        "10.04.2026 - Скъсано стоманено въже.\n25.04.2026 - Доставчикът бави доставката на резервната част.")
        );
        equipmentTable.setItems(list);

        // ЛОГИКА ЗА ИЗБОР НА РЕД: Обновява десния панел при кликване върху уред
        equipmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                lblPanelTitle.setText("Детайли: " + newSelection.name.get());
                lblPurchaseDate.setText(newSelection.purchaseDate);
                comboStatusEdit.setValue(newSelection.statusText.get());
                txtMaintenanceLogs.setText(newSelection.maintenanceLogs);
            }
        });

        // По подразбиране избираме първия ред при стартиране
        if (!list.isEmpty()) {
            equipmentTable.getSelectionModel().select(0);
        }
    }

    // Помощен вътрешен клас за структурата на таблицата
    public static class EquipmentRow {
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
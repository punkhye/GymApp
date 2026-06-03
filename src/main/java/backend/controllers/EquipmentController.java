package backend.controllers;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EquipmentController {

    // --- TABLE ---
    @FXML private TableView<EquipmentRow> equipmentTable;
    @FXML private TableColumn<EquipmentRow, String> colId;
    @FXML private TableColumn<EquipmentRow, String> colName;
    @FXML private TableColumn<EquipmentRow, String> colType;
    @FXML private TableColumn<EquipmentRow, String> colStatus;

    // --- DETAILS PANEL ---
    @FXML private Label lblPanelTitle;
    @FXML private Label lblPurchaseDate;
    @FXML private ComboBox<String> comboStatusEdit;
    @FXML private TextArea txtMaintenanceLogs;

    private ObservableList<EquipmentRow> equipmentList;

    private HomeController mainController;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(d -> d.getValue().id);
        colName.setCellValueFactory(d -> d.getValue().name);
        colType.setCellValueFactory(d -> d.getValue().type);
        colStatus.setCellValueFactory(d -> d.getValue().statusText);

        comboStatusEdit.setItems(FXCollections.observableArrayList(
                "🟢 Operational",
                "🔧 Under Repair",
                "❌ Out of Service"
        ));

        loadEquipmentFromDB();

        equipmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                lblPanelTitle.setText("Детайли: " + newSel.name.get());
                lblPurchaseDate.setText(newSel.purchaseDate);
                comboStatusEdit.setValue(newSel.statusText.get());
                txtMaintenanceLogs.setText(newSel.maintenanceLogs);
            }
        });
    }


    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    // dialozi
    @FXML
    private void openCreateEquipmentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontend/views/CreateEquipmentDialog.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Добавяне на оборудване");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.showAndWait();

            CreateEquipmentDialogController controller = loader.getController();
            EquipmentRow result = controller.getResult();

            if (result != null) {
                addEquipment(result);
                saveToDatabase(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //tablica
    public void addEquipment(EquipmentRow row) {
        equipmentList.add(row);
    }

    //database
    private void saveToDatabase(EquipmentRow row) {

        String sql = """
        INSERT INTO equipment (name, type, status, notes)
        VALUES (?, ?, ?, ?)
    """;

        try (var conn = DBConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, row.name.get());
            stmt.setString(2, row.type.get());
            stmt.setString(3, row.statusText.get());
            stmt.setString(4, row.maintenanceLogs);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadEquipmentFromDB() {

        equipmentList = FXCollections.observableArrayList();

        String sql = "SELECT id, name, type, status, notes FROM equipment";

        try (var conn = DBConnection.getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {

                EquipmentRow row = new EquipmentRow(
                        "EQ-" + rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("status"),
                        "",
                        rs.getString("notes")
                );

                equipmentList.add(row);
            }

            equipmentTable.setItems(equipmentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------
    // MODEL
    // -------------------------
    public static class EquipmentRow {

        public final javafx.beans.property.SimpleStringProperty id;
        public final javafx.beans.property.SimpleStringProperty name;
        public final javafx.beans.property.SimpleStringProperty type;
        public final javafx.beans.property.SimpleStringProperty statusText;

        public String purchaseDate;
        public String maintenanceLogs;

        public EquipmentRow(String id, String name, String type, String status,
                            String purchaseDate, String logs) {

            this.id = new javafx.beans.property.SimpleStringProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
            this.statusText = new javafx.beans.property.SimpleStringProperty(status);

            this.purchaseDate = purchaseDate;
            this.maintenanceLogs = logs;
        }
    }
}
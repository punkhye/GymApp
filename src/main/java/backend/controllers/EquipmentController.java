package backend.controllers;

import database.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

public class EquipmentController {

    // tablica
    @FXML private TableView<EquipmentRow> equipmentTable;
    @FXML private TableColumn<EquipmentRow, String> colId;
    @FXML private TableColumn<EquipmentRow, String> colName;
    @FXML private TableColumn<EquipmentRow, String> colType;
    @FXML private TableColumn<EquipmentRow, String> colPurchaseDate;
    @FXML private TableColumn<EquipmentRow, String> colStatus;

    //za listener-a
    private EquipmentRow selectedRow = null;

    // detaili desniq panel
    @FXML private Label lblPanelTitle;
    @FXML private Label lblPurchaseDate;
    @FXML private ComboBox<String> comboStatusEdit;
    @FXML private TextArea txtMaintenanceLogs;
    @FXML private TextArea txtNewLogEntry;
    private String originalNotes = null;

    // statove
    @FXML private Label lblTotal;
    @FXML private Label lblRepair;
    @FXML private Label lblOut;

    // lista s uredi
    private final ObservableList<EquipmentRow> equipmentList =
            FXCollections.observableArrayList();

    private HomeController mainController;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(d -> d.getValue().id);
        colName.setCellValueFactory(d -> d.getValue().name);
        colType.setCellValueFactory(d -> d.getValue().type);
        colPurchaseDate.setCellValueFactory(d -> d.getValue().purchaseDate);
        colStatus.setCellValueFactory(d -> d.getValue().statusText);

        equipmentTable.setItems(equipmentList);

        comboStatusEdit.setItems(FXCollections.observableArrayList(
                "Operational",
                "Under Repair",
                "Out of Service"
        ));

        loadEquipmentFromDB();
        loadStatsFromDB();

        equipmentTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {

                    if (newSel == null) {
                        selectedRow = null;
                        originalNotes = null;

                        txtMaintenanceLogs.clear();
                        txtNewLogEntry.clear();

                        lblPanelTitle.setText("Детайли на активен уред");
                        lblPurchaseDate.setText("");
                        comboStatusEdit.setValue(null);
                        return;
                    }

                    selectedRow = newSel;
                    originalNotes = newSel.maintenanceLogs;

                    lblPanelTitle.setText("Детайли: " + newSel.name.get());
                    lblPurchaseDate.setText(newSel.purchaseDate.get());
                    comboStatusEdit.setValue(newSel.statusText.get());

                    txtMaintenanceLogs.setText(
                            newSel.maintenanceLogs == null ? "" : newSel.maintenanceLogs
                    );


                    txtNewLogEntry.clear();
                });

        equipmentTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(EquipmentRow row, boolean empty) {
                super.updateItem(row, empty);

                if (row == null || empty) {
                    setStyle("");
                } else if (isSelected()) {
                    setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    //buton // otvarqne na dialoga za suzdavane na ured
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
                saveToDatabase(result);

                //refreshvane
                loadEquipmentFromDB();
                loadStatsFromDB();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // zapazvane v database-a
    private void saveToDatabase(EquipmentRow row) {

        String sql = """
         INSERT INTO equipment (name, type, status, notes, purchase_date)
          VALUES (?, ?, ?, ?, ?)
        """;

        var conn = DBConnection.getConnection();

        try (var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, row.name.get());
            stmt.setString(2, row.type.get());
            stmt.setString(3, row.statusText.get());
            stmt.setString(4, row.maintenanceLogs);


            String dateStr = row.purchaseDate.get();

            LocalDate date = (dateStr == null || dateStr.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dateStr);

            stmt.setDate(5, java.sql.Date.valueOf(date));

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //updatevane na notes v bazata danni
    private void updateNotesInDB(String id, String notes) {

        String sql = "UPDATE equipment SET notes = ? WHERE id = ?";

        var conn = DBConnection.getConnection();

        try (var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notes);
            stmt.setInt(2, Integer.parseInt(id));

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //updatevane na notes i status v bazata danni
    private void updateStatusAndLog(String id, String newStatus, String oldStatus, String date) {

        String sql = """
        UPDATE equipment
        SET status = ?,
            notes = COALESCE(notes, '') || ?
        WHERE id = ?
    """;

        String logEntry = "\n" + oldStatus + " -> " + newStatus + " (" + date + ")";

        var conn = DBConnection.getConnection();

        try (var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, logEntry);
            stmt.setInt(3, Integer.parseInt(id));

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // zarejdane na tablica
    private void loadEquipmentFromDB() {

        equipmentList.clear();

        String sql = "SELECT id, name, type, status, notes, purchase_date FROM equipment";

        var conn = DBConnection.getConnection();

        try (var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {

                EquipmentRow row = new EquipmentRow(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("status"),
                        rs.getString("purchase_date"),
                        rs.getString("notes")
                );

                equipmentList.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // statove
    private void loadStatsFromDB() {

        String sql = """
            SELECT
                COUNT(*) AS total,
                COUNT(*) FILTER (WHERE status = 'Under Repair') AS repair,
                COUNT(*) FILTER (WHERE status = 'Out of Service') AS out
            FROM equipment
        """;

        var conn = DBConnection.getConnection();

        try (var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            if (rs.next()) {
                lblTotal.setText("Общо машини: " + rs.getInt("total"));
                lblRepair.setText("В ремонт: " + rs.getInt("repair"));
                lblOut.setText("Извън употреба: " + rs.getInt("out"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //updatevane na loga pri butona "dobavqne na zapis"
    @FXML
    private void onAddLog() {

        if (selectedRow == null) {
            return;
        }

        String newEntry = txtNewLogEntry.getText();

        if (newEntry == null || newEntry.isBlank()) {
            return;
        }

        String timestamp = LocalDate.now().toString();

        String existingLogs = selectedRow.maintenanceLogs;
        if (existingLogs == null) existingLogs = "";

        String updatedLogs;

        if (existingLogs.isBlank()) {
            updatedLogs = newEntry + " (" + timestamp + ")";
        } else {
            updatedLogs = existingLogs + "\n" + newEntry + " (" + timestamp + ")";
        }

        // update model
        selectedRow.maintenanceLogs = updatedLogs;

        // update DB
        updateNotesInDB(selectedRow.id.get(), updatedLogs);

        // update UI
        txtMaintenanceLogs.setText(updatedLogs);
        txtNewLogEntry.clear();
    }

    //updatevane na sustoqnieto na urodena pri butona "zapazvane na promenite"
    @FXML
    private void onSaveChanges() {

        if (selectedRow == null) return;

        String newStatus = comboStatusEdit.getValue();
        if (newStatus == null || newStatus.isBlank()) return;

        String oldStatus = selectedRow.statusText.get();

        if (oldStatus.equals(newStatus)) return; // няма промяна

        String date = LocalDate.now().toString();

        // 1. update model
        selectedRow.statusText.set(newStatus);

        // 2. update DB (status + log)
        updateStatusAndLog(selectedRow.id.get(), newStatus, oldStatus, date);

        // 3. update UI
        loadEquipmentFromDB();
        loadStatsFromDB();
    }

    // model
    public static class EquipmentRow {

        public final javafx.beans.property.SimpleStringProperty id;
        public final javafx.beans.property.SimpleStringProperty name;
        public final javafx.beans.property.SimpleStringProperty type;
        public final javafx.beans.property.SimpleStringProperty statusText;

        public final SimpleStringProperty purchaseDate;
        public String maintenanceLogs;

        public EquipmentRow(String id, String name, String type, String status,
                            String purchaseDate, String logs) {

            this.id = new javafx.beans.property.SimpleStringProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
            this.statusText = new javafx.beans.property.SimpleStringProperty(status);

            this.purchaseDate = new SimpleStringProperty(purchaseDate);
            this.maintenanceLogs = logs;
        }
    }
}
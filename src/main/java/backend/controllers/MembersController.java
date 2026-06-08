package backend.controllers;

import database.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public class MembersController {

    // --- JavaFX Компоненти на таблицата за клиенти ---
    @FXML private TableView<MemberRow> membersTable;
    @FXML private TableColumn<MemberRow, String> colId;
    @FXML private TableColumn<MemberRow, String> colName;
    @FXML private TableColumn<MemberRow, String> colPhone;
    @FXML private TableColumn<MemberRow, String> colEmail;
    @FXML private TableColumn<MemberRow, String> colStatus;
    @FXML private TableColumn<MemberRow, String> colType;
    @FXML private TableColumn<MemberRow, String> colExpiry;
    @FXML private TableColumn<MemberRow, String> colActions;
    @FXML private TextField txtSearchMember;
    @FXML private ComboBox<String> comboStatusFilter;
    @FXML private ComboBox<String> comboSubscriptionFilter;

    private final ObservableList<MemberRow> membersList = FXCollections.observableArrayList();
    private final DateTimeFormatter displayDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private FilteredList<MemberRow> filteredMembersList;

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
        colId.setCellValueFactory(d -> d.getValue().id);
        colName.setCellValueFactory(d -> d.getValue().name);
        colPhone.setCellValueFactory(d -> d.getValue().phone);
        colEmail.setCellValueFactory(d -> d.getValue().email);
        colStatus.setCellValueFactory(d -> d.getValue().status);
        colType.setCellValueFactory(d -> d.getValue().type);
        colExpiry.setCellValueFactory(d -> d.getValue().expiryDate);

        membersTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(MemberRow item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else if ("EXPIRING".equals(item.rowType)) {
                    setStyle("-fx-background-color: #FEF3C7;");
                } else if ("EXPIRED".equals(item.rowType)) {
                    setStyle("-fx-background-color: #FEE2E2;");
                } else {
                    setStyle("");
                }
            }
        });

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

        filteredMembersList = new FilteredList<>(membersList, row -> true);

        SortedList<MemberRow> sortedMembersList = new SortedList<>(filteredMembersList);
        sortedMembersList.comparatorProperty().bind(membersTable.comparatorProperty());

        membersTable.setItems(sortedMembersList);

        setupMemberFilters();

        loadMembersFromDB();
    }

    private void loadMembersFromDB() {
        membersList.clear();

        String sql = """
                SELECT
                    m.id,
                    m.first_name,
                    m.last_name,
                    m.egn_or_dob,
                    m.phone,
                    m.is_active,
                    st.name AS subscription_name,
                    ms.end_date
                FROM members m
                LEFT JOIN LATERAL (
                    SELECT *
                    FROM member_subscriptions ms
                    WHERE ms.member_id = m.id
                    ORDER BY ms.end_date DESC
                    LIMIT 1
                ) ms ON true
                LEFT JOIN subscription_types st ON st.id = ms.subscription_type_id
                ORDER BY m.id
                """;

        var conn = DBConnection.getConnection();

        try (var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDate endDate = null;

                if (rs.getDate("end_date") != null) {
                    endDate = rs.getDate("end_date").toLocalDate();
                }

                boolean isActive = rs.getBoolean("is_active");
                String status = isActive ? "Active" : "Inactive";
                String rowType = calculateRowType(isActive, endDate);

                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                String expiryText = endDate == null ? "-" : endDate.format(displayDateFormatter);
                String subscriptionName = rs.getString("subscription_name") == null ? "-" : rs.getString("subscription_name");

                MemberRow row = new MemberRow(
                        String.valueOf(rs.getInt("id")),
                        fullName,
                        rs.getString("phone"),
                        rs.getString("egn_or_dob"),
                        status,
                        subscriptionName,
                        expiryText,
                        rowType
                );

                membersList.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Грешка с базата", "Членовете не можаха да се заредят от базата.");
        }
    }

    private String calculateRowType(boolean isActive, LocalDate endDate) {
        if (!isActive) {
            return "EXPIRED";
        }

        if (endDate == null) {
            return "NORMAL";
        }

        if (endDate.isBefore(LocalDate.now())) {
            return "EXPIRED";
        }

        if (!endDate.isAfter(LocalDate.now().plusDays(14))) {
            return "EXPIRING";
        }

        return "NORMAL";
    }

    private void ensureDefaultSubscriptionTypesExist() {
        String countSql = "SELECT COUNT(*) FROM subscription_types";

        String insertSql = """
                INSERT INTO subscription_types (name, price, duration_days, total_visits)
                VALUES
                ('Monthly Full', 60.00, 30, 999),
                ('Тримесечен', 150.00, 90, 999),
                ('Карта за 10 посещения', 50.00, 60, 10)
                """;

        var conn = DBConnection.getConnection();

        try (var countStmt = conn.prepareStatement(countSql);
             var rs = countStmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {
                try (var insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.executeUpdate();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Грешка с базата", "Абонаментните типове не можаха да се създадат.");
        }
    }

    private ObservableList<SubscriptionType> loadSubscriptionTypesFromDB() {
        ObservableList<SubscriptionType> types = FXCollections.observableArrayList();

        String sql = """
                SELECT id, name, price, duration_days, total_visits
                FROM subscription_types
                ORDER BY id
                """;

        var conn = DBConnection.getConnection();

        try (var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                types.add(new SubscriptionType(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("duration_days"),
                        rs.getInt("total_visits")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Грешка с базата", "Типовете абонаменти не можаха да се заредят.");
        }

        return types;
    }

    @FXML
    private void handleRegisterNewMember() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Регистрация на нов член");
        dialog.setHeaderText("Въведи данните за новия член");

        ButtonType saveButton = new ButtonType("Запази", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField txtFirstName = new TextField();
        txtFirstName.setPromptText("Име");

        TextField txtLastName = new TextField();
        txtLastName.setPromptText("Фамилия");

        TextField txtEgnOrDob = new TextField();
        txtEgnOrDob.setPromptText("ЕГН или дата на раждане");

        ComboBox<String> comboGender = new ComboBox<>();
        comboGender.getItems().addAll("Male", "Female", "Other");
        comboGender.setValue("Male");

        TextField txtPhone = new TextField();
        txtPhone.setPromptText("Телефон");

        TextArea txtHealthNotes = new TextArea();
        txtHealthNotes.setPromptText("Здравни бележки");
        txtHealthNotes.setPrefRowCount(3);

        ensureDefaultSubscriptionTypesExist();

        ComboBox<SubscriptionType> comboSubscription = new ComboBox<>();
        comboSubscription.setItems(loadSubscriptionTypesFromDB());

        if (!comboSubscription.getItems().isEmpty()) {
            comboSubscription.getSelectionModel().selectFirst();
        }

        DatePicker dateEnd = new DatePicker();

        TextField txtRemainingVisits = new TextField();
        txtRemainingVisits.setPromptText("Оставащи посещения");

        TextField txtAmountPaid = new TextField();
        txtAmountPaid.setPromptText("Платена сума");

        ComboBox<String> comboPaymentMethod = new ComboBox<>();
        comboPaymentMethod.getItems().addAll("Cash", "Card", "Bank Transfer");
        comboPaymentMethod.setValue("Cash");

        CheckBox chkActive = new CheckBox("Активен член");
        chkActive.setSelected(true);

        comboSubscription.valueProperty().addListener((obs, oldType, newType) -> {
            if (newType != null) {
                dateEnd.setValue(LocalDate.now().plusDays(newType.getDurationDays()));
                txtRemainingVisits.setText(String.valueOf(newType.getTotalVisits()));
                txtAmountPaid.setText(newType.getPrice().toString());
            }
        });

        if (comboSubscription.getValue() != null) {
            SubscriptionType selectedType = comboSubscription.getValue();
            dateEnd.setValue(LocalDate.now().plusDays(selectedType.getDurationDays()));
            txtRemainingVisits.setText(String.valueOf(selectedType.getTotalVisits()));
            txtAmountPaid.setText(selectedType.getPrice().toString());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Име:"), 0, 0);
        grid.add(txtFirstName, 1, 0);

        grid.add(new Label("Фамилия:"), 0, 1);
        grid.add(txtLastName, 1, 1);

        grid.add(new Label("ЕГН/Дата:"), 0, 2);
        grid.add(txtEgnOrDob, 1, 2);

        grid.add(new Label("Пол:"), 0, 3);
        grid.add(comboGender, 1, 3);

        grid.add(new Label("Телефон:"), 0, 4);
        grid.add(txtPhone, 1, 4);

        grid.add(new Label("Здравни бележки:"), 0, 5);
        grid.add(txtHealthNotes, 1, 5);

        grid.add(new Label("Абонамент:"), 0, 6);
        grid.add(comboSubscription, 1, 6);

        grid.add(new Label("Изтича на:"), 0, 7);
        grid.add(dateEnd, 1, 7);

        grid.add(new Label("Оставащи посещения:"), 0, 8);
        grid.add(txtRemainingVisits, 1, 8);

        grid.add(new Label("Платена сума:"), 0, 9);
        grid.add(txtAmountPaid, 1, 9);

        grid.add(new Label("Начин на плащане:"), 0, 10);
        grid.add(comboPaymentMethod, 1, 10);

        grid.add(chkActive, 1, 11);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButton) {
                if (txtFirstName.getText().isBlank() || txtLastName.getText().isBlank()) {
                    showAlert(Alert.AlertType.WARNING, "Липсващи данни", "Име и фамилия са задължителни.");
                    return;
                }

                SubscriptionType selectedSubscriptionType = comboSubscription.getValue();

                if (selectedSubscriptionType == null) {
                    showAlert(Alert.AlertType.WARNING, "Грешка", "Моля, избери тип абонамент.");
                    return;
                }

                int remainingVisits;

                try {
                    remainingVisits = Integer.parseInt(txtRemainingVisits.getText().trim());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.WARNING, "Грешка", "Оставащи посещения трябва да е число.");
                    return;
                }

                BigDecimal amountPaid;

                try {
                    amountPaid = new BigDecimal(txtAmountPaid.getText().trim());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.WARNING, "Грешка", "Платена сума трябва да е число.");
                    return;
                }

                saveMemberAndSubscriptionToDB(
                        txtFirstName.getText().trim(),
                        txtLastName.getText().trim(),
                        txtEgnOrDob.getText().trim(),
                        comboGender.getValue(),
                        txtPhone.getText().trim(),
                        txtHealthNotes.getText().trim(),
                        chkActive.isSelected(),
                        selectedSubscriptionType.getId(),
                        dateEnd.getValue(),
                        remainingVisits,
                        amountPaid,
                        comboPaymentMethod.getValue()
                );

                loadMembersFromDB();
                applyMemberFilters();
            }
        });
    }

    private void saveMemberAndSubscriptionToDB(
            String firstName,
            String lastName,
            String egnOrDob,
            String gender,
            String phone,
            String healthNotes,
            boolean isActive,
            int subscriptionTypeId,
            LocalDate endDate,
            int remainingVisits,
            BigDecimal amountPaid,
            String paymentMethod
    ) {
        String insertMemberSql = """
                INSERT INTO members (first_name, last_name, egn_or_dob, gender, phone, health_notes, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String insertSubscriptionSql = """
                INSERT INTO member_subscriptions
                (member_id, subscription_type_id, purchase_date, end_date, remaining_visits, amount_paid, payment_method, processed_by_user_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        var conn = DBConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            int newMemberId;

            try (var stmt = conn.prepareStatement(insertMemberSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, egnOrDob);
                stmt.setString(4, gender);
                stmt.setString(5, phone);
                stmt.setString(6, healthNotes);
                stmt.setBoolean(7, isActive);

                stmt.executeUpdate();

                try (var keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        newMemberId = keys.getInt(1);
                    } else {
                        throw new RuntimeException("Не може да се вземе ID на новия член.");
                    }
                }
            }

            try (var stmt = conn.prepareStatement(insertSubscriptionSql)) {
                stmt.setInt(1, newMemberId);
                stmt.setInt(2, subscriptionTypeId);
                stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                stmt.setDate(4, java.sql.Date.valueOf(endDate));
                stmt.setInt(5, remainingVisits);
                stmt.setBigDecimal(6, amountPaid);
                stmt.setString(7, paymentMethod);

                // временно 1; ако има реален logged-in user id, после го сменяме
                stmt.setInt(8, 1);

                stmt.executeUpdate();
            }

            conn.commit();

            showAlert(Alert.AlertType.INFORMATION, "Успешно", "Новият член е записан успешно.");

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception rollbackError) {
                rollbackError.printStackTrace();
            }

            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Грешка с базата", "Новият член не беше записан.");

        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupMemberFilters() {
        comboStatusFilter.setItems(FXCollections.observableArrayList(
                "Всички",
                "Active",
                "Inactive"
        ));
        comboStatusFilter.setValue("Всички");

        comboSubscriptionFilter.setItems(FXCollections.observableArrayList("Всички"));
        comboSubscriptionFilter.setValue("Всички");

        loadSubscriptionFilterOptions();

        txtSearchMember.textProperty().addListener((obs, oldValue, newValue) -> applyMemberFilters());
        comboStatusFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyMemberFilters());
        comboSubscriptionFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyMemberFilters());
    }

    private void loadSubscriptionFilterOptions() {
        String sql = """
            SELECT name
            FROM subscription_types
            ORDER BY id
            """;

        var conn = DBConnection.getConnection();

        try (var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                comboSubscriptionFilter.getItems().add(rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyMemberFilters() {
        String searchText = txtSearchMember.getText();
        String selectedStatus = comboStatusFilter.getValue();
        String selectedSubscription = comboSubscriptionFilter.getValue();

        filteredMembersList.setPredicate(member -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;
            boolean matchesSubscription = true;

            if (searchText != null && !searchText.isBlank()) {
                String lowerSearch = searchText.toLowerCase();

                matchesSearch =
                        member.id.get().toLowerCase().contains(lowerSearch)
                                || member.name.get().toLowerCase().contains(lowerSearch)
                                || member.phone.get().toLowerCase().contains(lowerSearch)
                                || member.email.get().toLowerCase().contains(lowerSearch);
            }

            if (selectedStatus != null && !selectedStatus.equals("Всички")) {
                matchesStatus = member.status.get().equals(selectedStatus);
            }

            if (selectedSubscription != null && !selectedSubscription.equals("Всички")) {
                matchesSubscription = member.type.get().equals(selectedSubscription);
            }

            return matchesSearch && matchesStatus && matchesSubscription;
        });
    }

    public static class SubscriptionType {
        private final int id;
        private final String name;
        private final BigDecimal price;
        private final int durationDays;
        private final int totalVisits;

        public SubscriptionType(int id, String name, BigDecimal price, int durationDays, int totalVisits) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.durationDays = durationDays;
            this.totalVisits = totalVisits;
        }

        public int getId() {
            return id;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public int getDurationDays() {
            return durationDays;
        }

        public int getTotalVisits() {
            return totalVisits;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class MemberRow {
        public SimpleStringProperty id, name, phone, email, status, type, expiryDate;
        public String rowType;

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
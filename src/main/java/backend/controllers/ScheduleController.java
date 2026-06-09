package backend.controllers;

import database.DBConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.scene.control.TextInputDialog;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;

public class ScheduleController {

    @FXML
    private GridPane calendarGrid;
    @FXML
    private Label lblWeekRange;
    @FXML
    private Button btnViewDay;
    @FXML
    private Button btnViewWeek;
    @FXML
    private Button btnViewMonth;

    private HomeController mainController;
    private LocalDate currentAnchorDate;
    private String currentViewMode = "WEEK";

    public static class TrainingRow {
        private String id, type, trainer, hall, date, time;
        private int capacity;


        public TrainingRow(String id, String type, String trainer, String hall, String date, String time, int capacity) {
            this.id = id;
            this.type = type;
            this.trainer = trainer;
            this.hall = hall;
            this.date = date;
            this.time = time;
            this.capacity = capacity;
        }

        public String getId() {
            return id;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getType() {
            return type;
        }

        public String getTrainer() {
            return trainer;
        }

        public String getHall() {
            return hall;
        }

        public Integer getCapacity() {
            return capacity;
        }
    }

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        currentAnchorDate = LocalDate.now();
        refreshCalendar();
    }

    private void refreshCalendar() {
        calendarGrid
                .getChildren()
                .clear();

        updateLabelText();
        switch (currentViewMode
        ) {
            case "DAY":
                buildDayStructure();
                loadDayData();
                break;

            case "WEEK":
                buildWeekStructure();
                loadWeekData();
                break;

            case "MONTH":
                buildMonthStructure();
                loadMonthData();
                break;

        }

    }

    private void buildDayStructure() {
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        Label hTime = new Label("Час");
        hTime.getStyleClass().add("calendar-header-cell");
        hTime.setStyle("-fx-font-weight: bold; -fx-padding: 10;");
        calendarGrid.add(hTime, 0, 0);

        Label hDay = new Label(currentAnchorDate.format(DateTimeFormatter.ofPattern("EEEE (dd.MM)")));
        hDay.getStyleClass().add("calendar-header-cell");
        hDay.setStyle("-fx-font-weight: bold; -fx-padding: 10;");
        calendarGrid.add(hDay, 1, 0);

        int rowIdx = 1;
        for (int hour = 8; hour <= 21; hour++) {
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.getStyleClass().add("calendar-time-cell");
            timeLabel.setStyle("-fx-font-weight: bold; -fx-padding: 15;");
            calendarGrid.add(timeLabel, 0, rowIdx++);
        }
    }

    private void loadDayData() {
        String query = getBaseSQL() + " WHERE s.start_time::date = ? ORDER BY s.start_time ASC";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setObject(1, currentAnchorDate);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalDateTime startDateTime = rs.getTimestamp("start_time").toLocalDateTime();
                        int rowIdx = startDateTime.getHour() - 8 + 1;
                        if (rowIdx >= 1 && rowIdx <= 14) {
                            javafx.scene.layout.HBox slot = getOrCreateDaySlot(rowIdx);
                            slot.getChildren().add(extractCard(rs));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildWeekStructure() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        for (int i = 0; i < 8; i++) {
            javafx.scene.layout.ColumnConstraints col =
                    new javafx.scene.layout.ColumnConstraints();
            if (i == 0) {
                col.setPercentWidth(8);
            }

            else {
                col.setPercentWidth(13.14);
            }

            calendarGrid.getColumnConstraints().add(col);

        }

        LocalDate startOfWeek = currentAnchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        String[] days = {"Час", "Понеделник", "Вторник", "Сряда", "Четвъртък", "Петък", "Събота", "Неделя"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = (i == 0) ? new Label(days[i]) : new Label(days[i] + "\n(" + startOfWeek.plusDays(i - 1)
                                    .format(DateTimeFormatter.ofPattern("dd.MM")) + ")");

            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setMinHeight(60);
            dayLabel.setStyle(
                    "-fx-font-weight:bold;"
                            + "-fx-padding:16;"
                            + "-fx-alignment:center;"
                            + "-fx-border-color:#E2E8F0;"
                            + "-fx-background-color:#F8FAFC;"
            );

            calendarGrid.add(dayLabel, i, 0);
        }

        int row = 1;
        for (int hour = 8; hour <= 21; hour++) {
            Label time = new Label(String.format("%02d:00", hour));
            time.setMinHeight(90);

            time.setMaxWidth(Double.MAX_VALUE);

            time.setStyle(
                    "-fx-font-weight:bold;"
                            + "-fx-alignment:center;"
                            + "-fx-border-color:#E2E8F0;"
                            + "-fx-padding:14;"
            );

            calendarGrid.add(time, 0, row++);

        }
        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);

    }
    private void loadWeekData() {
        LocalDate startOfWeek = currentAnchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        String query = getBaseSQL() + " WHERE s.start_time::date BETWEEN ? AND ? ORDER BY s.start_time ASC";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setObject(1, startOfWeek);
                stmt.setObject(2, endOfWeek);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalDateTime startDateTime = rs.getTimestamp("start_time").toLocalDateTime();
                        int columnIdx = startDateTime.getDayOfWeek().getValue();
                        int rowIdx = startDateTime.getHour() - 8 + 1;
                        if (rowIdx >= 1 && rowIdx <= 14) {
                            VBox slot = getOrCreateSlot(
                                    columnIdx,
                                    rowIdx
                            );

                            slot.getChildren().add(
                                    extractCard(rs)
                            );
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private VBox getOrCreateSlot(
            int col,
            int row
    ) {

        for (javafx.scene.Node node : calendarGrid.getChildren()
        ) {
            Integer c = GridPane.getColumnIndex(node);
            Integer r = GridPane.getRowIndex(node);
            if (c != null && r != null && c == col && r == row && node instanceof VBox
            ) {
                return (VBox) node;
            }
        }

        VBox box = new VBox(6
                );

        box.setPadding(new Insets(4)
        );
        calendarGrid.add(box, col, row);

        return box;

    }

    private javafx.scene.layout.HBox getOrCreateDaySlot(
            int row
    ) {
        for (
                javafx.scene.Node node : calendarGrid.getChildren()
        ) {
            Integer r = GridPane.getRowIndex(node);
            Integer c = GridPane.getColumnIndex(node);
            if (r != null && c != null && r == row && c == 1 && node instanceof javafx.scene.layout.HBox
            ) {
                return (javafx.scene.layout.HBox) node;
            }

        }

        javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(10);
        box.setPadding(new Insets(6)
        );
        box.setMaxWidth(Double.MAX_VALUE
        );
        calendarGrid.add(box, 1, row
        );

        return box;

    }

    private void buildMonthStructure() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();
        calendarGrid.getColumnConstraints().clear();

        for (int i = 0; i < 7; i++) {
            javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
            col.setPercentWidth(14.28);
            calendarGrid.getColumnConstraints().add(col);
        }

        String[] days = {"Понеделник", "Вторник", "Сряда", "Четвъртък", "Петък", "Събота", "Неделя"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.getStyleClass().add("calendar-header-cell");
            dayLabel.setStyle("-fx-font-weight: bold; -fx-padding: 12; -fx-alignment: center; -fx-max-width: 5000;");
            calendarGrid.add(dayLabel, i, 0);
        }
    }

    // 🌟 ОПРАВЕНО: Връзката не се затваря автоматично и дизайнът е изчистен
    private void loadMonthData() {
        YearMonth yearMonth = YearMonth.from(currentAnchorDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        LocalDate lastOfMonth = yearMonth.atEndOfMonth();

        int leadDays = firstOfMonth.getDayOfWeek().getValue() - 1;

        Map<LocalDate, List<String>> monthlyTrainings = new HashMap<>();
        String query = getBaseSQL() + " WHERE s.start_time::date BETWEEN ? AND ? ORDER BY s.start_time ASC";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setObject(1, firstOfMonth);
                stmt.setObject(2, lastOfMonth);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalDate date = rs.getTimestamp("start_time").toLocalDateTime().toLocalDate();
                        String info = rs.getString("workout_name") + " (" + rs.getTimestamp("start_time").toLocalDateTime().getHour() + ":00)";
                        monthlyTrainings.computeIfAbsent(date, k -> new ArrayList<>()).add(info);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            int calcIdx = leadDays + day - 1;
            int col = calcIdx % 7;
            int row = (calcIdx / 7) + 1;

            VBox dayBox = new VBox(4);
            dayBox.setPadding(new Insets(6));
            dayBox.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0.5; -fx-background-color: #FFFFFF; -fx-min-height: 100px; -fx-min-width: 135px;");

            Label lblDayNum = new Label(String.valueOf(day));
            lblDayNum.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 13px;");
            dayBox.getChildren().add(lblDayNum);

            LocalDate targetDate = yearMonth.atDay(day);
            if (monthlyTrainings.containsKey(targetDate)) {
                for (String trainingText : monthlyTrainings.get(targetDate)) {
                    Label lblShort = new Label(trainingText);

                    lblShort.setMaxWidth(Double.MAX_VALUE);

                    lblShort.setStyle("-fx-font-size: 10px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #1E293B; " +
                            "-fx-background-color: #F1F5F9; " +
                            "-fx-border-color: #CBD5E1; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 3 6 3 6; " +
                            "-fx-max-width: 5000;");

                    lblShort.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
                    dayBox.getChildren().add(lblShort);
                }
            }

            calendarGrid.add(dayBox, col, row);
        }
    }

    private String getBaseSQL() {
        return "SELECT s.id, wt.name AS workout_name, (c.first_name || ' ' || c.last_name) AS coach_name, s.hall_name, s.start_time " +
                "FROM schedules s " +
                "JOIN workout_types wt ON s.workout_type_id = wt.id " +
                "JOIN coaches c ON s.coach_id = c.id";
    }

    private VBox extractCard(ResultSet rs)
            throws Exception {VBox card = createTrainingCard(
                rs.getString("workout_name"),
                rs.getString("coach_name"),
                rs.getString("hall_name")
                );

        Integer scheduleId = rs.getInt("id");
        card.setOnMouseClicked(
        e -> {
                    System.out.println(
                            "CLICK " + scheduleId
                    );
                    openEditDialog(
                            scheduleId
                    );

                }

        );

        return card;

    }

    private VBox createTrainingCard(String title, String coach, String room) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6; -fx-border-color: #E2E8F0; -fx-border-width: 1;");
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F172A; -fx-font-size: 12px;");
        Label lblCoach = new Label("👤 " + coach);
        lblCoach.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");
        Label lblRoom = new Label("📍 Зала " + room);
        lblRoom.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        card.getChildren().addAll(lblTitle, lblCoach, lblRoom);
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        return card;
    }

    private void updateLabelText() {
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        if (currentViewMode.equals("DAY")) {
            lblWeekRange.setText(currentAnchorDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        } else if (currentViewMode.equals("WEEK")) {
            LocalDate start = currentAnchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate end = start.plusDays(6);
            lblWeekRange.setText(start.format(DateTimeFormatter.ofPattern("dd MMM")) + " - " + end.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        } else {
            lblWeekRange.setText(currentAnchorDate.format(monthYearFormatter).toUpperCase());
        }
    }

    @FXML
    private void handleNextWeek() {
        if (currentViewMode.equals("DAY")) currentAnchorDate = currentAnchorDate.plusDays(1);
        else if (currentViewMode.equals("WEEK")) currentAnchorDate = currentAnchorDate.plusWeeks(1);
        else currentAnchorDate = currentAnchorDate.plusMonths(1);
        refreshCalendar();
    }

    @FXML
    private void handlePreviousWeek() {
        if (currentViewMode.equals("DAY")) currentAnchorDate = currentAnchorDate.minusDays(1);
        else if (currentViewMode.equals("WEEK")) currentAnchorDate = currentAnchorDate.minusWeeks(1);
        else currentAnchorDate = currentAnchorDate.minusMonths(1);
        refreshCalendar();
    }

    @FXML
    private void handleCurrentWeek() {
        currentAnchorDate = LocalDate.now();
        refreshCalendar();
    }

    @FXML
    private void handleViewDay() {
        currentViewMode = "DAY";
        setSegmentedActive(btnViewDay);
        refreshCalendar();
    }

    @FXML
    private void handleViewWeek() {
        currentViewMode = "WEEK";
        setSegmentedActive(btnViewWeek);
        refreshCalendar();
    }

    @FXML
    private void handleViewMonth() {
        currentViewMode = "MONTH";
        setSegmentedActive(btnViewMonth);
        refreshCalendar();
    }

    private void setSegmentedActive(Button activeBtn) {
        btnViewDay.getStyleClass().remove("segmented-btn-active");
        btnViewWeek.getStyleClass().remove("segmented-btn-active");
        btnViewMonth.getStyleClass().remove("segmented-btn-active");
        activeBtn.getStyleClass().add("segmented-btn-active");
    }

    @FXML
    private void onCreateTraining() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/CreateTrainingDialog.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            CreateTrainingDialogController controller = loader.getController();
            if (
                    controller.getResult()
                            != null

            ) {


                refreshCalendar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void openEditDialog(Integer id) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement selectStmt = conn.prepareStatement(
               """
               SELECT
               wt.name,
               c.first_name,
               c.last_name,
               s.hall_name,
               s.start_time
               FROM schedules s
               JOIN workout_types wt
               ON s.workout_type_id = wt.id
               JOIN coaches c
               ON s.coach_id = c.id  
               WHERE s.id = ?
               """

                    );
            selectStmt.setInt(
                    1,
                    id
            );

            ResultSet rs = selectStmt.executeQuery();
            if (
                    !rs.next()
            ) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/CreateTrainingDialog.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            CreateTrainingDialogController controller = loader.getController();
            controller.setEditMode(id);
            controller.loadTraining(rs.getString("name"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("hall_name"),
                    rs.getTimestamp("start_time")
                            .toLocalDateTime()
                            .toLocalDate()
                            .toString(),
                    rs.getTimestamp("start_time")
                            .toLocalDateTime()
                            .toLocalTime()
                            .toString()
                            .substring(0, 5),
                    20
            );
            stage.showAndWait();
            refreshCalendar();

            if (
                    controller.getResult() == null
            ) {
                return;
            }
            TrainingRow updated = controller.getResult();
            PreparedStatement stmt = conn.prepareStatement(
                    """
                    UPDATE schedules
                    SET
                    workout_type_id=
                    (
                    SELECT id
                    FROM workout_types
                    WHERE name=?
                    LIMIT 1
                    ),
                    coach_id=
                    (
                    SELECT id
                    FROM coaches
                    WHERE TRIM(first_name || ' ' || last_name)=TRIM(?)
                    LIMIT 1
                    ),
                    hall_name=?,
                    start_time=?
                    WHERE id=?
                    
                    """

            );

            stmt.setString(1, updated.getType());
            stmt.setString(2, updated.getTrainer());
            stmt.setString(3, updated.getHall());
            stmt.setTimestamp(4,
            java.sql.Timestamp.valueOf(
                updated.getDate() + " " + updated.getTime() + ":00"));

            stmt.setInt(5, id);
            stmt.executeUpdate();
            refreshCalendar();
        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }
}

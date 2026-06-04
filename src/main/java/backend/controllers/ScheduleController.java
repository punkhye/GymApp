package backend.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ScheduleController {

    // Основната решетка (календар), в която разполагаме часовете и картите с тренировки
    @FXML private GridPane calendarGrid;

    // Референция към главния контролер за координация на навигацията
    private HomeController mainController;

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    private static final java.util.List<TrainingRow> trainings =
            new java.util.ArrayList<>();

    @FXML
    public void initialize() {
        // Генерираме динамично заглавния ред (дните) и левите колони (часовете)
        setupCalendarHeaders();
        setupTimeRows();

        // Карта 1: Кросфит (Понеделник -> Колона 1, часови слот 18:30 -> Ред 4)
        createClassCard("CrossFit", "Димитър", "Зала 1","18:30", "🔥 17/20 Записани", "#DC2626", 1, 4);

        // Карта 2: Йога (Вторник -> Колона 2, часови слот 19:30 -> Ред 5)
        createClassCard("Йога", "Елена", "Зала 2",    "19:30","🟢 8/15 Записани", "#84CC16", 2, 5);
    }

    /**
     * Отваря диалогов прозорец за добавяне на нова тренировка.
     * Използва отделен FXML екран.
     */
    @FXML
    private void onCreateTraining() {

        try {

            // Зареждаме екрана за създаване
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/CreateTrainingDialog.fxml"));
            // Създаваме отделен прозорец
            Stage stage = new Stage();

            // Зареждаме интерфейса
            Scene scene = new Scene(loader.load());

            stage.setScene(scene);

            // Заглавие
            stage.setTitle("Добавяне на тренировка");

            // Блокира основния прозорец
            stage.initModality(Modality.APPLICATION_MODAL);

            // Показва прозореца
            stage.showAndWait();

            // Вземаме контролера
            CreateTrainingDialogController controller = loader.getController();
            // Проверка дали е затворен със Save
            if (
                    controller.hasResult()
            ) {
                // Добавяме карта към календара
                TrainingRow row =
                        controller.getResult();

                trainings.add(row);

                int column =
                        java.time.LocalDate
                                .parse(
                                        row.getDate()
                                )
                                .getDayOfWeek()
                                .getValue();

                String time =
                        row.getTime();

                int calendarRow;

                switch (time) {
                    case "08:30":
                        calendarRow = 1;
                        break;
                    case "10:00":
                        calendarRow = 2;
                        break;
                    case "12:00":
                        calendarRow = 3;
                        break;
                    case "17:00":
                        calendarRow = 4;
                        break;
                    case "18:30":
                        calendarRow = 5;
                        break;
                    case "19:30":
                        calendarRow = 6;
                        break;
                    default:
                        calendarRow = 5;
                }

                createClassCard(
                        row.getType(),
                        row.getTrainer(),
                        row.getHall(),
                        row.getTime(),
                        "🟢 0/" + row.getCapacity() + " Записани", "#2563EB", column, calendarRow
                );
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Динамично добавяне на дните от седмицата в най-горния ред (Ред 0) на GridPane
     */
    private void setupCalendarHeaders() {
        String[] days = {"Час", "Пон (1.06)", "Вто (2.06)", "Сря (3.06)", "Чет (4.06)", "Пет (5.06)", "Съб (6.06)", "Нед (7.06)"};
        for (int i = 0; i < days.length; i++) {
            Label label = new Label(days[i]);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B5563; -fx-padding: 10px; -fx-font-size: 13px;");
            // Методът .add(node, column, row) позиционира елемента в решетката
            calendarGrid.add(label, i, 0);
        }
    }

    /**
     * Динамично добавяне на часовите слотове по вертикала (Колона 0) и чертане на празни фонови клетки с тънки сиви линии
     */
    private void setupTimeRows() {
        String[] hours = {"08:30", "10:00", "12:00", "17:00", "18:30", "19:30"};
        for (int i = 0; i < hours.length; i++) {
            // Поставяме етикета за час в първата колона (col: 0, row: i + 1)
            Label hourLabel = new Label(hours[i]);
            hourLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-padding: 20px 0;");
            calendarGrid.add(hourLabel, 0, i + 1);

            // Обхождаме останалите 7 колони (за всеки ден), за да нарисуваме празни фонови кутийки (граници)
            for (int col = 1; col <= 7; col++) {
                Pane gridCell = new Pane();
                gridCell.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 0 1 1 0; -fx-min-height: 90px;");
                calendarGrid.add(gridCell, col, i + 1);
            }
        }
    }

    /**
     * Помощен метод за софтуерно сглобяване и стилизиране на заоблена карта за тренировка (VBox).
     * Разполага я на точно зададени координати (col, row) в календара.
     */
    private void createClassCard(
            String title,
            String coach,
            String room,
            String time,
            String badgeText,
            String accentColor,
            int col,
            int row
    ) {
        VBox card = new VBox(5);
        // Задаваме бял фон, сенки и дебела лява цветна линия (-fx-border-color) за визуален акцент
        card.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 6px; " +
                "-fx-padding: 12px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2); " +
                "-fx-border-color: transparent transparent transparent " + accentColor + "; " +
                "-fx-border-width: 0 0 0 5px; " +
                "-fx-border-radius: 6px; " +
                "-fx-cursor: hand;");

        // Добавяме вътрешен марж (отстъп), за да не се допира картата до сивите линии на календара
        GridPane.setMargin(card, new Insets(4));

        // Изграждане на текстовите етикети вътре в картата
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #1A1D20; -fx-font-size: 14px;");

        Label lblCoach = new Label("Треньор: " + coach);
        lblCoach.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 12px;");

        Label lblRoom = new Label("Зала: " + room);
        lblRoom.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        Label lblTime = new Label("Час: " + time);
        lblTime.setStyle("-fx-text-fill:#6B7280; -fx-font-size:12px;");

        Label lblBadge = new Label(badgeText);
        // Сменяме цвета на текста на баджа на червено при запълнен капацитет, иначе е зелен
        lblBadge.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " +
                (accentColor.equals("#DC2626") ? "#DC2626" : "#4F8A10") + ";");

        // Набиваме всички контроли като деца на вертикалния контейнер (VBox)
        card.getChildren().addAll(lblTitle, lblCoach, lblRoom, lblTime, lblBadge);

        // КРИТИЧНО ИЗИСКВАНЕ: Създаване и инсталиране на Tooltip (Попъп), който изскача при задържане на мишката
        Tooltip actionTooltip = new Tooltip();
        actionTooltip.setText("[📝 Запиши член]   [👁 Виж присъствени]");
        actionTooltip.setStyle("-fx-background-color: #1A1D20; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8px;");
        // Обвързваме направения Tooltip с конкретната VBox карта
        Tooltip.install(card, actionTooltip);

        // Поставяме готовата карта на точната й софтуерна позиция в решетката
        calendarGrid.add(card, col, row);
    }
    /**
     * Временен обект за прехвърляне на данните
     * от диалога към календара.
     */
    public static class TrainingRow {

        private String id;

        private String type;

        private String trainer;

        private String hall;

        private String date;

        private String time;

        private Integer capacity;

        public TrainingRow(
                String id,
                String type,
                String trainer,
                String hall,
                String date,
                String time,
                Integer capacity
        ) {

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

        public String getType() {
            return type;
        }

        public String getTrainer() {
            return trainer;
        }

        public String getHall() {
            return hall;
        }

        public String getDate() {
            return date;
        }

        public String getTime() { return time; }

        public Integer getCapacity() {
            return capacity;
        }

    }
}
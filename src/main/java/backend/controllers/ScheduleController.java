package backend.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

public class ScheduleController {

    @FXML private GridPane calendarGrid;
    private HomeController mainController;

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        setupCalendarHeaders();
        setupTimeRows();

        // Карта 1: Кросфит (Понеделник - Колона 1, 18:30 - Ред 4)
        createClassCard("CrossFit", "Димитър", "Зала 1", "🔥 17/20 Записани", "#DC2626", 1, 4);

        // Карта 2: Йога (Вторник - Колона 2, 19:30 - Ред 5)
        createClassCard("Йога", "Елена", "Зала 2", "🟢 8/15 Записани", "#84CC16", 2, 5);
    }

    private void setupCalendarHeaders() {
        String[] days = {"Час", "Пон (1.06)", "Вто (2.06)", "Сря (3.06)", "Чет (4.06)", "Пет (5.06)", "Съб (6.06)", "Нед (7.06)"};
        for (int i = 0; i < days.length; i++) {
            Label label = new Label(days[i]);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B5563; -fx-padding: 10px; -fx-font-size: 13px;");
            calendarGrid.add(label, i, 0);
        }
    }

    private void setupTimeRows() {
        String[] hours = {"08:30", "10:00", "12:00", "17:00", "18:30", "19:30"};
        for (int i = 0; i < hours.length; i++) {
            Label hourLabel = new Label(hours[i]);
            hourLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-padding: 20px 0;");
            calendarGrid.add(hourLabel, 0, i + 1);

            // Слагаме тънки сиви линии като фон за решетката
            for (int col = 1; col <= 7; col++) {
                Pane gridCell = new Pane();
                gridCell.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 0 1 1 0; -fx-min-height: 90px;");
                calendarGrid.add(gridCell, col, i + 1);
            }
        }
    }

    private void createClassCard(String title, String coach, String room, String badgeText, String accentColor, int col, int row) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 6px; " +
                "-fx-padding: 12px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2); " +
                "-fx-border-color: transparent transparent transparent " + accentColor + "; " +
                "-fx-border-width: 0 0 0 5px; " +
                "-fx-border-radius: 6px; " +
                "-fx-cursor: hand;");

        // Малко разстояние, за да не се застъпват с линиите на мрежата
        GridPane.setMargin(card, new Insets(4));

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #1A1D20; -fx-font-size: 14px;");

        Label lblCoach = new Label("Треньор: " + coach);
        lblCoach.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 12px;");

        Label lblRoom = new Label("Зала: " + room);
        lblRoom.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        Label lblBadge = new Label(badgeText);
        lblBadge.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " +
                (accentColor.equals("#DC2626") ? "#DC2626" : "#4F8A10") + ";");

        card.getChildren().addAll(lblTitle, lblCoach, lblRoom, lblBadge);

        // КРИТИЧНО ИЗИСКВАНЕ: Изскачащ Попъп (Tooltip) с бързи бутони при задържане
        Tooltip actionTooltip = new Tooltip();
        actionTooltip.setText("[📝 Запиши член]   [👁 Виж присъствени]");
        actionTooltip.setStyle("-fx-background-color: #1A1D20; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8px;");
        Tooltip.install(card, actionTooltip);

        calendarGrid.add(card, col, row);
    }
}
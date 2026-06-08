package backend.models;

import javafx.beans.property.SimpleStringProperty;

public class ScheduleItem {
    private final SimpleStringProperty time;
    private final SimpleStringProperty className;
    private final SimpleStringProperty coach;
    private final SimpleStringProperty hall;
    private final SimpleStringProperty slots;

    public ScheduleItem(String time, String className, String coach, String hall, String slots) {
        this.time = new SimpleStringProperty(time);
        this.className = new SimpleStringProperty(className);
        this.coach = new SimpleStringProperty(coach);
        this.hall = new SimpleStringProperty(hall);
        this.slots = new SimpleStringProperty(slots);
    }

    public String getTime() { return time.get(); }
    public String getClassName() { return className.get(); }
    public String getCoach() { return coach.get(); }
    public String getHall() { return hall.get(); }
    public String getSlots() { return slots.get(); }
}
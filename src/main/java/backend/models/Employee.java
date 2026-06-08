package backend.models;

public class Employee {

    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String role;
    private boolean active;

    public Employee(int id,
                    String firstName,
                    String lastName,
                    String username,
                    String role,
                    boolean active) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.role = role;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
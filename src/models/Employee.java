package models;

public class Employee {
    private final String email;
    private final String username;

    public Employee(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }
}

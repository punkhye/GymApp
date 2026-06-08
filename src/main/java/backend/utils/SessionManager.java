package backend.utils;

public class SessionManager {

    private static int userId = -1;
    private static String username;
    private static String role;

    // LOGIN SET
    public static void setUser(int id, String user, String userRole) {
        userId = id;
        username = user;
        role = userRole;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    // CHECKS
    public static boolean isLoggedIn() {
        return userId != -1;
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public static boolean isEmployee() {
        return "EMPLOYEE".equalsIgnoreCase(role);
    }

    // LOGOUT
    public static void clearSession() {
        userId = -1;
        username = null;
        role = null;
    }
}
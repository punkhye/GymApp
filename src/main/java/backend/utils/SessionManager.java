package backend.utils;

public class SessionManager {
    private static String loggedInUsername;

    public static void setLoggedInUsername(String username) {
        loggedInUsername = username;
    }

    public static String getLoggedInUsername() {
        return loggedInUsername != null ? loggedInUsername : "Системен администратор";
    }

    public static void clearSession() {
        loggedInUsername = null;
    }
}
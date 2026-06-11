package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.FileInputStream;

public class DBConnection {

    private static Connection connection;

    public static void init() {
        if (connection != null) return;

        try {
            Class.forName("org.postgresql.Driver");
            Properties props = new Properties();
            boolean loaded = false;

            // 1. НАЧИН (За колегите ти): Стандартният Java начин през Classloader (работи в ресурсите)
            try (var is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                    loaded = true;
                    System.out.println("DB properties loaded from Classpath (Resources).");
                }
            } catch (Exception ignored) {}

            // 2. НАЧИН (Спасителен пояс за теб): Ако IntelliJ е зациклила, търсим файла директно по диска
            if (!loaded) {
                java.io.File[] absolutePaths = {
                        new java.io.File("src/main/database/db.properties"),
                        new java.io.File("src/database/db.properties"),
                        new java.io.File("database/db.properties")
                };

                for (java.io.File file : absolutePaths) {
                    if (file.exists()) {
                        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                            props.load(fis);
                            loaded = true;
                            System.out.println("DB properties loaded from physical path: " + file.getPath());
                            break;
                        } catch (Exception ignored) {}
                    }
                }
            }

            // Ако и по двата начина нищо не се намери
            if (!loaded) {
                throw new java.io.FileNotFoundException(
                        "❌ Не намирам db.properties никъде! Сложи го в 'src/main/database/' или в корена на проекта."
                );
            }

            // Свързване
            connection = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );

            System.out.println("DB connected successfully!");

        } catch (Exception e) {
            System.err.println("❌ ГРЕШКА ПРИ СВЪРЗВАНЕ С БАЗАТА ДАННИ:");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                init();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("DB connection closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
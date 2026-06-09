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
            props.load(
                    DBConnection.class
                            .getClassLoader()
                            .getResourceAsStream("db.properties")
            );

            connection = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );

            System.out.println("DB connected!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                init();
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return connection;

    }

    public static void close() {

    }
}
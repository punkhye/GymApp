package database;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {
    private static Properties props;
    public static void init() {
        try {
            Class.forName("org.postgresql.Driver");
            props =new Properties();
            props.load(new FileInputStream("src/main/java/database/db.properties"));
            System.out.println("DB connected!");
        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Connection getConnection() {
        try {
            if (props == null) {
                init();
            }

            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );

        }

        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void close() {
        // вече не прави нищо

    }

}
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

public class DBConnection {

    private static Connection connection;

    public static Connection connect() {


        try {
            Class.forName("org.postgresql.Driver");

            Properties props = new Properties();


            InputStream input = new FileInputStream("src/main/java/database/db.properties");
            props.load(input);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            connection = DriverManager.getConnection(url, user, password);

            System.out.println("Connected successfully!");
            return connection;

        } catch (Exception e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
            return null;
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            return connect();




        }
        return connection;
    }


    public static void close() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
                System.out.println("Connection closed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

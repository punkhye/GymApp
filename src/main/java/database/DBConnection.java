package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
            "jdbc:postgresql://dpg-d8fdiki8qa3s738t8570-a.frankfurt-postgres.render.com/gym_app_database_efrh";

    private static final String USER =
            "db_admin";

    private static final String PASSWORD =
            "wU0taaRpBbxitxUh3VW5zFHjLRHbZiRv";

    public static void init() {

        try {
            Class.forName(
                    "org.postgresql.Driver"
            );
            System.out.println(
                    "DB connected!"
            );

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }


    public static Connection getConnection() {

        try {

            Class.forName(
                    "org.postgresql.Driver"
            );

            Connection conn =

                    DriverManager.getConnection(

                            URL,

                            USER,

                            PASSWORD

                    );

            return conn;

        }

        catch (Exception e) {

            e.printStackTrace();

            return null;

        }

    }

}
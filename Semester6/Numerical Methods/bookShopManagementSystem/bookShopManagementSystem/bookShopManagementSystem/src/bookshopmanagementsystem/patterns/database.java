package bookshopmanagementsystem.patterns;

import java.sql.Connection;
import java.sql.DriverManager;

public class database {

    private static database instance;

    private database() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static database getInstance() {
        if (instance == null) {
            instance = new database();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/book?useSSL=false&serverTimezone=UTC",
                    "root",
                    "Soso@2312"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

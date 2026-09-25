package Group6;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    // The database settings (including the password) are read from db.properties
    // in the project folder. GitHub ignores that file, so the password stays private (NFR-04).
    private static final String SETTINGS_FILE = "db.properties";

    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load the MySQL Driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in build path!");
            throw new SQLException(e);
        }

        Properties settings = new Properties();
        try (InputStream in = new FileInputStream(SETTINGS_FILE)) {
            settings.load(in);
        } catch (IOException e) {
            System.err.println("Could not read " + SETTINGS_FILE + ". Copy db.properties.example, "
                    + "rename the copy to db.properties, and fill in your MySQL password.");
            throw new SQLException("Missing " + SETTINGS_FILE, e);
        }

        String url = settings.getProperty("db.url");
        String user = settings.getProperty("db.user");
        String password = settings.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
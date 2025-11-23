package com.nppp2d.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnectionUtil
 *
 * Reads database connection configuration from classpath resource
 * `application.properties` (if present). Falls back to sensible defaults.
 *
 * Expected properties (optional):
 *   db.driver = com.mysql.cj.jdbc.Driver
 *   db.url    = jdbc:mysql://localhost:3306/nppp2d_db
 *   db.user   = root
 *   db.pass   =
 *
 * Note: This class intentionally keeps a simple DriverManager-based connection.
 * If you later switch to a connection pool (HikariCP), refactor this class
 * to expose a DataSource instead.
 */
public class DBConnectionUtil {

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL =
        "jdbc:mysql://localhost:3306/nppp2d_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static final String DB_DRIVER;
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASS;

    static {
        Properties props = new Properties();
        try (
            InputStream in =
                DBConnectionUtil.class.getClassLoader().getResourceAsStream(
                    "application.properties"
                )
        ) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Loading properties failed — proceed with defaults
            e.printStackTrace();
        }

        DB_DRIVER = props.getProperty("db.driver", DEFAULT_DRIVER);
        DB_URL = props.getProperty("db.url", DEFAULT_URL);
        DB_USER = props.getProperty("db.user", DEFAULT_USER);
        DB_PASS = props.getProperty("db.pass", DEFAULT_PASS);
    }

    /**
     * Obtain a new JDBC Connection using configured driver/url/user/pass.
     *
     * @return a new Connection
     * @throws SQLException if driver not found or connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Ensure the JDBC driver class is available
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("JDBC Driver not found: " + DB_DRIVER, e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Note: main() used for ad-hoc testing has been removed from production class.
}

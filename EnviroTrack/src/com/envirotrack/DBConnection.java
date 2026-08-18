package com.envirotrack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection factory.
 *
 * Credentials are never stored in this file. They are read, in order of priority:
 *   1. Environment variables  ENVIROTRACK_DB_URL / _USER / _PASSWORD
 *   2. A local db.properties file (git-ignored) sitting next to the project
 *
 * See db.properties.example for the file format, and SECRETS-SETUP.md for setup.
 */
public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/envirotrack_db";

    /** Places we look for db.properties, relative to wherever the app was launched. */
    private static final String[] SEARCH_PATHS = {
        "db.properties",
        "EnviroTrack/db.properties",
        "../db.properties"
    };

    private static Properties fileProps;   // cached after first load

    /**
     * Loads db.properties once, if it exists. Missing file is not an error —
     * environment variables may be supplying the values instead.
     */
    private static synchronized Properties fileProps() {
        if (fileProps != null) return fileProps;

        fileProps = new Properties();
        for (String candidate : SEARCH_PATHS) {
            Path path = Paths.get(candidate);
            if (Files.isReadable(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    fileProps.load(in);
                    return fileProps;
                } catch (IOException ignored) {
                    // fall through and try the next location
                }
            }
        }
        return fileProps;
    }

    /** Environment variable wins; otherwise db.properties; otherwise the fallback. */
    private static String value(String envKey, String propKey, String fallback) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv.trim();

        String fromFile = fileProps().getProperty(propKey);
        if (fromFile != null && !fromFile.isBlank()) return fromFile.trim();

        return fallback;
    }

    public static Connection getConnection() throws SQLException {
        String url  = value("ENVIROTRACK_DB_URL",      "db.url",      DEFAULT_URL);
        String user = value("ENVIROTRACK_DB_USER",     "db.user",     null);
        String pass = value("ENVIROTRACK_DB_PASSWORD", "db.password", null);

        if (user == null || pass == null) {
            throw new SQLException(
                "Database credentials not found.\n" +
                "Set the environment variables ENVIROTRACK_DB_USER and ENVIROTRACK_DB_PASSWORD,\n" +
                "or copy db.properties.example to db.properties and fill it in.\n" +
                "See SECRETS-SETUP.md for step-by-step instructions."
            );
        }

        return DriverManager.getConnection(url, user, pass);
    }

    /** Convenience for the Python side and for diagnostics — never prints the password. */
    public static String describeTarget() {
        return value("ENVIROTRACK_DB_URL", "db.url", DEFAULT_URL)
             + " as user " + value("ENVIROTRACK_DB_USER", "db.user", "<unset>");
    }
}
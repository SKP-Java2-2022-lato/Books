package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }


    /**
     * Ustanawia połączenie z bazą danych
     * @return zwraca obiekt klasy Connection
     * @throws IOException brak pliku
     * @throws SQLException brak połączenia
     */

    private static Connection getConnection() throws IOException, SQLException {
        Properties properties = new Properties();
        try(InputStream inputStream = Files.newInputStream(Paths.get("database.properties"))) {
            properties.load(inputStream);
        }
        String drivers = properties.getProperty("jdb.drivers");
        if(drivers != null) System.setProperty("jdbc.drivers", drivers);
        String url = properties.getProperty("jdbc.url");
        String username = properties.getProperty("jdbc.username");
        String password = properties.getProperty("jdbc.password");

        return DriverManager.getConnection(url, username, password);
    }
}
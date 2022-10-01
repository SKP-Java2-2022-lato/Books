package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    private static final String authorQuery = "SELECT Books.Title, Books.Price, Books.ISBN " +
            "FROM Books JOIN BooksAuthors ON Books.ISBN=BooksAuthors.ISBN " +
            "JOIN Authors ON Authors.Author_Id = BooksAuthors.Author_Id " +
            "WHERE Authors.Name = ?";

    private static final String publisherQuery = "SELECT Books.Title, Books.Price, Books.ISBN " +
            "FROM Books JOIN Publishers ON Publishers.Publisher_Id = Books.Publisher_Id " +
            "WHERE  Publishers.Name = ?";

    private static final String authorPublisherQuery = "SELECT Books.Title, Books.Price, Books.ISBN " +
            "FROM Books JOIN BooksAuthors ON Books.ISBN=BooksAuthors.ISBN " +
            "JOIN Authors ON Authors.Author_Id = BooksAuthors.Author_Id " +
            "JOIN Publishers ON Publishers.Publisher_Id = Books.Publisher_Id "+
            "WHERE Authors.Name = ? AND  Publishers.Name = ?";

    private static final String updatePrice = "UPDATE BOOKS SET Price = Price * (1+?) WHERE Publisher_Id IN " +
            "(SELECT Publisher_Id FROM Publishers WHERE Publishers.Name = ?)";

    private static final String allAuthors = "SELECT  Books.Title, Books.Price, Books.ISBN FROM Books";
    private static ArrayList<String> authors = new ArrayList<>();
    private static ArrayList<String> publishers = new ArrayList<>();

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try(Connection connection = getConnection()){
            // wypełniam zbiór autrów i wydawców
            authors.add("Dowolny");
            publishers.add("Dowolny");
            try(Statement statement = connection.createStatement()){
                String query = "SELECT Name FROM Authors";
                try(ResultSet resultSet = statement.executeQuery(query)){
                    while(resultSet.next()){
                        authors.add(resultSet.getString(1));
                    }
                }
                query = "SELECT Name FROM Publishers";
                try(ResultSet resultSet = statement.executeQuery(query)){
                    while(resultSet.next()){
                        publishers.add(resultSet.getString(1));
                    }
                }
                boolean done = false;
                while(!done){
                    System.out.println("Menu:");
                    System.out.println("1) zapytanie (kwerenda)");
                    System.out.println("2) aktualizacja cen");
                    System.out.println("3) koniec programu");
                    int input = scanner.nextInt();
                    if(input == 1)
                        executeQuery(connection);
                    else if (input == 2)
                        updatePrice(connection);
                    else done = true;
                }
            }

        }catch (SQLException e){
            for(Throwable t: e)
                t.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Wykonuje polecenie na bazie danych
     * @param connection połączenie z bazą danych
     */

    private static void executeQuery(Connection connection) throws SQLException {
        String author = select(authors, "Autorzy: ");
        String publisher = select(publishers, "Wydawcy");
        PreparedStatement statement;
        if(author.equals("Dowolny"))
        {
            if(publisher.equals("Dowolny")) {
                statement = connection.prepareStatement(allAuthors);
            }
            else{
                statement = connection.prepareStatement(publisherQuery);
                statement.setString(1, publisher);
            }
        } else if (publisher.equals("Dowolny")) {
            statement = connection.prepareStatement(authorQuery);
            statement.setString(1, author);
        } else{
            statement = connection.prepareStatement(authorPublisherQuery);
            statement.setString(1, author);
            statement.setString(2, publisher);
        }

        try(ResultSet rs = statement.executeQuery()){
            while (rs.next())
                System.out.println(rs.getString(1)+ " " + rs.getString(2) + " " + rs.getString(3));
        }
    }

    /**
     * Wybiera element z listy
     * @param options lista do wyświetlnia
     * @param prompt zachęta
     * @return wybrany element
     */
    private static String select(List<String> options, String prompt){
        while(true){
            System.out.println(prompt);
            for(int i=0; i<options.size(); i++){
                System.out.printf("%2d %s%n", i+1, options.get(i));
            }
            System.out.print("Wybór: ");
            int selected = scanner.nextInt();
            if(selected > 0 && selected <= options.size())
                return options.get(selected-1);
        }

    }

    /***
     * Zwiększa cenę książek o zadany procent
     * @param connection połączenie z bazą danych
     */

    private static void updatePrice(Connection connection) throws SQLException {
        String publisher = select(publishers.subList(1, publishers.size()), "Wydawcy: ");
        System.out.print("Zmiana cen o [%]: ");
        double priceChange = scanner.nextDouble();
        priceChange = priceChange/100.0;
        PreparedStatement statement = connection.prepareStatement(updatePrice);
        statement.setDouble(1, priceChange);
        statement.setString(2, publisher);
        int r = statement.executeUpdate();
        System.out.println("Zakutalizowana wierszy: "+r);
        statement.close();

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
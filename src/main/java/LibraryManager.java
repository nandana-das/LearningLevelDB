import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.Map;
import java.util.scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

//import org.iq80.leveldb.DB;
//import org.iq80.leveldb.Options;
//import org.iq80.leveldb.impl.Iq80DBFactory;

public class LibraryManager {

    private Connection connection;
    private LRUCache cache;
    
    public LibraryManager(String dbUrl, String dbUsername, String dbPassword) throws SQLException, IOException {
        // Connect to MySQL database
        connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

        // Create LRU cache instance
        cache = new LRUCache(cacheDir); // 5mins cache expiry

    }
    public void storeLibraryData() throws SQLException {
        Statement statement = connection.createStatement();

        // Store Books data in LevelDB (replace 'lib_data' with your actual schema name)
        String sql = "SELECT * FROM lib_data.Books"; // Adjust table name if needed
        ResultSet booksResult = statement.executeQuery(sql);

       while (booksResult.next()) {
            int bookId = booksResult.getInt("id");
            // Convert row data to HashMap
            Map<String, String> bookData = new HashMap<>();
            bookData.put("title", booksResult.getString("title"));
            bookData.put("author", booksResult.getString("author"));
            bookData.put("genre", booksResult.getString("genre"));
            // Add more key-value pairs as needed for other book attributes

            // Serialize data into JSON string (or any other format you prefer)
            String jsonData = convertMapToJson(bookData);

            // Add to LRU cache
            cache.put(String.valueOf(bookId), jsonData, System.currentTimeMillis() +5*60*1000);
        }

        // Repeat for Members and Loans tables with appropriate data formatting
        booksResult.close();
        statement.close();
    }

    public String getBookDetails(int bookId) throws Exception {
        // Check LRU cache first
        String cachedValue = cache.get(String.valueOf(bookId));
        if (cachedValue != null) {
            return cachedValue;
        }

        // If not found in cache, retrieve from database
        Statement statement = connection.createStatement();
        String sql = "SELECT * FROM lib_data.Books WHERE id = " + bookId;
        ResultSet bookResult = statement.executeQuery(sql);

        if (bookResult.next()) {
            Map<String, String> bookData = new HashMap<>();
            bookData.put("title", bookResult.getString("title"));
            bookData.put("author", bookResult.getString("author"));
            bookData.put("genre", bookResult.getString("genre"));
            // Add more key-value pairs as needed for other book attributes

            // Serialize data into JSON string (or any other format you prefer)
            String jsonData = convertMapToJson(bookData);

            // Add to LRU cache
            cache.put(String.valueOf(bookId), jsonData, System.currentTimeMillis() + 10000);

            return jsonData;
        } else {
            return null; // Book not found
        }
    }

    public String convertMapToJson(Map<String, String> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    public Map<String, String> convertJsonToMap(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
    }
    public void close() throws SQLException, IOException {
        connection.close();
        cache.close();
    }

    public static void main(String[] args) {
        // Replace with your details
        String dbUrl = "data/base/url"; // Replace with your actual database url
        String dbUsername = "root";//replace with your actual database username
        String dbPassword = "Password";///replace with your actual database password

        try {
            // Create LibraryManager instance in the try block
            LibraryManager manager = new LibraryManager(dbUrl, dbUsername, dbPassword);

            // Store data from database to LevelDB
            manager.storeLibraryData();

             // Display book names and numbers
            Statement statement = manager.connection.createStatement();
            ResultSet booksResult = statement.executeQuery("SELECT id, title FROM lib_data.Books");
            System.out.println("Book IDs and names:");
            while (booksResult.next()) {
                System.out.println("ID: " + booksResult.getInt("id") + ", Name: " + booksResult.getString("title"));
            }
            booksResult.close();
            statement.close();

            // Let the user choose which book they want to see the details of
            System.out.print("Enter the ID of the book you want to see the details of: ");
            int bookId = new Scanner(System.in).nextInt();

            // Retrieve book details
            try {
                String bookData = manager.getBookDetails(bookId);
                //convert bookData from string to a map then print the values
                if (bookData != null) {
                    System.out.println("Book details for ID " + bookId + ": " + bookData);
                } else {
                    System.out.println("Book not found with ID " + bookId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error occurred: " + e.getMessage());
            }

            manager.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error occurred: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error occurred: " + e.getMessage());
        }
    }
}

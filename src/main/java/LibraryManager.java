import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

public class LibraryManager {

    private Connection connection;
    private DB levelDb;

    public LibraryManager(String dbUrl, String dbUsername, String dbPassword) throws SQLException, IOException {
        // Connect to MySQL database
        connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

        // Create a temporary directory for LevelDB
        Path tempDir = Files.createTempDirectory("leveldb");

        // Open LevelDB database
        Options options = new Options();
        options.createIfMissing(true);
        levelDb = Iq80DBFactory.factory.open(tempDir.toFile(), options);
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

            // Store JSON string in LevelDB
            byte[] data = jsonData.getBytes();
            levelDb.put(String.valueOf(bookId).getBytes(), data);
        }

        // Repeat for Members and Loans tables with appropriate data formatting
        booksResult.close();
        statement.close();
    }

    public String getBookDetails(int bookId) throws Exception {
        byte[] data = levelDb.get(String.valueOf(bookId).getBytes());
        if (data == null) {
            return null; // Book not found
        }
        // Deserialize data from byte array (adjust based on your data format)
       String jsonData = new String(data);
        Map<String, String> bookData = convertJsonToMap(jsonData);

        // Convert book data map to a formatted string for printing
        StringBuilder details = new StringBuilder();
        details.append("Book details for ID ").append(bookId).append(":\n");
        for (Map.Entry<String, String> entry : bookData.entrySet()) {
            details.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return details.toString();
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
        levelDb.close();
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

            // Example: Retrieve book details
            int bookId = 1;
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

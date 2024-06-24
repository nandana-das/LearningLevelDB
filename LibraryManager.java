import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
            String bookData = "title:" + booksResult.getString("title") + ",author:" + booksResult.getString("author") + ",genre:" + booksResult.getString("genre") + "..."; // Customize data format

            // Serialize data into byte array (adjust based on your data format)
            byte[] data = bookData.getBytes();
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
        return new String(data);
    }

    public void close() throws SQLException, IOException {
        connection.close();
        levelDb.close();
    }

    public static void main(String[] args) {
        // Replace with your details
        String dbUrl = "jdbc:mysql://localhost:3306/lib_data"; // Replace 'lib_data' with your actual database name
        String dbUsername = "root";
        String dbPassword = "Paru007@kunjan";

        try {
            // Create LibraryManager instance in the try block
            LibraryManager manager = new LibraryManager(dbUrl, dbUsername, dbPassword);

            // Store data from database to LevelDB
            manager.storeLibraryData();

            // Example: Retrieve book details
            int bookId = 1;
            try {
                String bookData = manager.getBookDetails(bookId);
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

import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

public class levelDBExample {
    public static void main(String[] args) {
        try ( // Assign DB object to a variable
            DB db = openDatabase("example") // Call the method
        ) {
            // Put some data into the database
            db.put("Hello".getBytes(), "World!".getBytes());

            // Get the data back out
            byte[] value = db.get("Hello".getBytes());
            System.out.println(new String(value));
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static DB openDatabase(String databaseName) throws IOException {
        File databaseFile = new File(databaseName);
        Options databaseOptions = new Options();
        databaseOptions.createIfMissing(true);
        return Iq80DBFactory.factory.open(databaseFile, databaseOptions);
    }
}

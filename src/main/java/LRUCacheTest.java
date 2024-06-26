import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

public class LRUCacheTest {

    public static void main(String[] args) throws IOException {
        // Create a new LevelDB instance with a small LRU cache
        Options options = new Options();
        options.cacheSize(1 * 1024 * 1024); // 1MB cache size
        options.maxOpenFiles(256);
        DB db = Iq80DBFactory.factory.open(new File("testdb"), options);

        CacheCalculator cacheCalculator = new CacheCalculator(options);
        
        // Insert some data into the database
        for (int i = 0; i < 100; i++) {
            byte[] key = ("key" + i).getBytes();
            byte[] value = ("value" + i).getBytes();
            db.put(key, value);
            cacheCalculator.updateCacheSize(key.length + value.length);
        }

        // Get some data from the database to test the cache
        long startTime = System.currentTimeMillis() / 1000; // epoch time in seconds
        for (int i = 0; i < 100; i++) {
            byte[] key = ("key" + i).getBytes();
            byte[] value = db.get(key);
            System.out.println("Value for key" + i + ": " + new String(value));
            cacheCalculator.updateCacheSize(-key.length - value.length); // subtract the size of the retrieved data
        }
        
        long endTime = System.currentTimeMillis() / 1000; // epoch time in seconds
        System.out.println("Time taken: " + (endTime - startTime) + " seconds");

        System.out.println("Remaining cache size: " + cacheCalculator.getRemainingCacheSize() + " bytes");
 
        // Access the first value in the cache
        DBIterator iterator = db.iterator();
        iterator.seekToFirst();
        if (iterator.hasNext()) {
            byte[] firstKey = iterator.next().getKey();
            byte[] firstValue = iterator.peekNext().getValue();
            System.out.println("First key: " + new String(firstKey));
            System.out.println("First value: " + new String(firstValue));
        }
// Insert some data into the database
        for (int i = 100; i < 200; i++) {
            byte[] key = ("key" + i).getBytes();
            byte[] value = ("value" + i).getBytes();
            db.put(key, value);
            cacheCalculator.updateCacheSize(key.length + value.length);
        }

        // Get some data from the database to test the cache
        startTime = System.currentTimeMillis() / 1000; // epoch time in seconds
        for (int i = 0; i < 200; i++) {
            byte[] key = ("key" + i).getBytes();
            byte[] value = db.get(key);
            System.out.println("Value for key" + i + ": " + new String(value));
            cacheCalculator.updateCacheSize(-key.length - value.length); // subtract the size of the retrieved data
        }
        endTime = System.currentTimeMillis() / 1000; // epoch time in seconds
        System.out.println("Time taken: " + (endTime - startTime) + " seconds");

        System.out.println("Remaining cache size: " + cacheCalculator.getRemainingCacheSize() + " bytes");

        // Access the first value in the cache again
        iterator = db.iterator();
        iterator.seekToFirst();
        if (iterator.hasNext()) {
            byte[] firstKey = iterator.next().getKey();
            byte[] firstValue = iterator.peekNext().getValue();
            System.out.println("First key: " + new String(firstKey));
            System.out.println("First value: " + new String(firstValue));
        }

        
        iterator.close();

        // Close the database
        db.close();
    }
}

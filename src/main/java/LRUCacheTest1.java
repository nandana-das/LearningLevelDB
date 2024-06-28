import static org.fusesource.leveldbjni.JniDBFactory.factory;

import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class LRUCacheTest1 {

	public static void main(String[] args) throws IOException {
		// Create a new LevelDB instance with a small LRU cache
		Options options = new Options();
		options.cacheSize(5 * 1024); // 128bytes cache size
		// options.maxOpenFiles(1);
		DB db = factory.open(new File("testdb"), options);
		//System.out.println(new String(db.get("key0".getBytes())));
		//System.out.println(new String(db.get("key10".getBytes())));
  	//System.out.println(new String(db.get("key500500".getBytes())));
		//System.out.println(new String(db.get("key100000".getBytes())));
		System.out.println(db.getProperty("levelDB.stats"));
		
		for(int i=0;i<100000;i++) {
		  db.delete(("key" + i).getBytes());
		}
		 
		System.out.println(db.getProperty("levelDB.stats"));
		// Insert some data into the database 
		for (int i = 0; i < 1000; i++) {
		  byte[] key = ("key" + i).getBytes(); byte[] value = ("value" + i).getBytes();
		  db.put(key, value); } 
		System.out.println(db.getProperty("leveldb.stats"));
		
		/**for (int i = 1000; i < 100000; i++) {
			  byte[] key = ("key" + i).getBytes(); byte[] value = ("value" + i).getBytes();
			  db.put(key, value); } 
			System.out.println(db.getProperty("leveldb.stats"));
	   /*	  
		 * // Get some data from the database to test the cache long startTime =
		 * System.currentTimeMillis() / 1000; // epoch time in seconds for (int i = 0; i
		 * < 1000000000; i++) { byte[] key = ("key" + i).getBytes(); byte[] value =
		 * db.get(key); System.out.println("Value for key" + i + ": " + new
		 * String(value)); } long endTime = System.currentTimeMillis() / 1000; // epoch
		 * time in seconds System.out.println("Time taken: " + (endTime - startTime) + "
		 * seconds");
		 **/
		// Close the database
		db.close();
	}
}

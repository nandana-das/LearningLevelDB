import java.io.File;
import java.io.IOException;
import java.time.Instant;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An LRU cache implementation using LevelDB.
 */
public class LRUCache {
    private DB db;

    /**
     * Constructs a new LRU cache instance with the given cache directory.
     *
     * @param cacheDir the cache directory
     * @throws IOException if there is an error opening the LevelDB instance
     */
    public LRUCache(String cacheDir) throws IOException {
        Options options = new Options();
        File file = new File(cacheDir);
        db = Iq80DBFactory.factory.open(file, options);
    }

    /**
     * Adds a key-value pair to the cache with an expiration time.
     *
     * @param key   the key
     * @param value the value
     * @param expiryTime the expiration time in milliseconds
     */
    public void put(String key, String value, long expiryTime) {
        db.put(key.getBytes(), (value + "#" + expiryTime).getBytes());
    }

    /**
     * Retrieves the value associated with the given key from the cache.
     *
     * @param key the key
     * @return the value associated with the key, or null if the key is not present or the cache entry has expired
     */
    public String get(String key) {
        byte[] valueBytes = db.get(key.getBytes());
        if (valueBytes != null) {
            String[] valueExpiry = new String(valueBytes).split("#");
            if (valueExpiry.length == 2) {
                long expiryTime = Long.parseLong(valueExpiry[1]);
                if (Instant.now().toEpochMilli() < expiryTime) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        JsonNode node = mapper.readTree(valueExpiry[0]);
                        StringBuilder result = new StringBuilder();
                        result.append("author:").append(node.get("author").asText()).append("\n");
                        result.append("genre:").append(node.get("genre").asText()).append("\n");
                        result.append("title:").append(node.get("title").asText()).append("\n");
                        return result.toString();
                    } catch (IOException e) {
                        // Handle JSON parsing exception
                        e.printStackTrace();
                    }
                } else {
                    // Cache has expired, remove it
                    db.delete(key.getBytes());
                }
            }
        }
        return null;
    }

    /**
     * Removes the key-value pair associated with the given key from the cache.
     *
     * @param key the key
     */
    public void remove(String key) {
        db.delete(key.getBytes());
    }

    /**
     * Closes the LevelDB instance and releases any associated resources.
     *
     * @throws IOException if there is an error closing the LevelDB instance
     */
    public void close() throws IOException {
        db.close();
    }
}

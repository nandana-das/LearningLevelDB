import java.io.File;
import java.io.IOException;
import java.time.Instant;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LRUCache {
    private DB db;

    public LRUCache(String cacheDir) throws IOException {
        Options options = new Options();
        File file = new File(cacheDir);
        db = Iq80DBFactory.factory.open(file, options);
    }

    public void put(String key, String value, long expiryTime) {
        db.put(key.getBytes(), (value + "#" + expiryTime).getBytes());
    }

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

    public void remove(String key) {
        db.delete(key.getBytes());
    }

    public void close() throws IOException {
        db.close();
    }
}

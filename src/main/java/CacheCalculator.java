import org.iq80.leveldb.Options;

public class CacheCalculator {
    private final Options options;
    private long cacheSize;

    public CacheCalculator(Options options) {
        this.options = options;
        this.cacheSize = options.cacheSize();
    }

    public void updateCacheSize(long dataSize) {
        this.cacheSize -= dataSize;
        if (this.cacheSize < 0) {
            this.cacheSize = 0;
        }
    }

    public long getRemainingCacheSize() {
        return this.cacheSize;
    }
}

package de.hotkeyyy.clansystem.data.cache;

import java.util.Map;
import java.util.Optional;

public interface Cache<K, V> {
    /**
     * Retrieves a value from the cache based on the provided key.
     * @param key The key associated with the value.
     * @return The value associated with the key, or null if not found.
     */
    Optional<V> get(K key);

    /**
     * Stores a key-value pair in the cache.
     * @param key The key associated with the value.
     * @param value The value to be stored.
     */
    void put(K key, V value);

    /**
     * Removes the entry associated with the provided key from the cache.
     * @param key The key of the entry to be removed.
     */
    void remove(K key);

    /**
     * Clears all entries from the cache.
     */
    void clear();
    /**
     * Retrieves the entire cache as a map.
     * @return The cache as a map.
     */
    Map<K, V> getMap();
    /**
     * Updates the cache with the provided value.
     * @param value The value to be updated.
     */
    void update(V value);
    /**
     * Checks if the cache contains a value associated with the provided key.
     * @param key The key to check for.
     * @return True if the cache contains the key, false otherwise.
     */
    boolean contains(K key);


}

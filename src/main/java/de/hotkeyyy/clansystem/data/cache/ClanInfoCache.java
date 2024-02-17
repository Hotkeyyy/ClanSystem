package de.hotkeyyy.clansystem.data.cache;

import de.hotkeyyy.clansystem.data.ClanInfo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ClanInfoCache implements Cache<Integer, ClanInfo> {
    private final Map<Integer, ClanInfo> cache;

    public ClanInfoCache() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<ClanInfo> get(Integer key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public synchronized void put(Integer key, ClanInfo value) {
        cache.put(key, value);
    }

    @Override
    public synchronized void remove(Integer key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public Map<Integer, ClanInfo> getMap() {
        return cache;
    }

    @Override
    public synchronized void update(ClanInfo value) {
        cache.put(value.id, value);
    }

    @Override
    public boolean contains(Integer key) {
        return cache.containsKey(key);
    }
}

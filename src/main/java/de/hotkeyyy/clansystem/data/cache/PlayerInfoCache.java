package de.hotkeyyy.clansystem.data.cache;

import de.hotkeyyy.clansystem.data.ClanPlayerInfo;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerInfoCache implements Cache<UUID, ClanPlayerInfo> {
    private final Map<UUID, ClanPlayerInfo> cache;

    public PlayerInfoCache() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<ClanPlayerInfo> get(UUID key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public synchronized void put(UUID key, ClanPlayerInfo value) {
        cache.put(key, value);
    }

    @Override
    public synchronized void remove(UUID key) {
        cache.remove(key);
    }

    @Override
    public synchronized void clear() {
        cache.clear();
    }

    @Override
    public Map<UUID, ClanPlayerInfo> getMap() {
        return cache;
    }

    @Override
    public synchronized void update(ClanPlayerInfo value) {
        cache.put(value.id, value);
    }

    @Override
    public boolean contains(UUID key) {
        return cache.containsKey(key);
    }
}


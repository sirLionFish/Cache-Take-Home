package com.example.cache;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

public class UserCache {

    private static final long DEFAULT_MAXIMUM_SIZE = 10_000;
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final Cache<String, User> userCache;

    public UserCache() {
        this(
                DEFAULT_MAXIMUM_SIZE,
                DEFAULT_TTL,
                Ticker.systemTicker()
        );
    }

    UserCache(long maximumSize, Duration ttl, Ticker ticker) {
        this.userCache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(ttl)
                .ticker(ticker)
                .build();
    }

    public void addUser(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        userCache.put(user.getId(), user);
    }

    public User getUser(String id) {
        if (id == null) {
            return null;
        }

        return userCache.getIfPresent(id);
    }

    public void removeUser(String id) {
        if (id != null) {
            userCache.invalidate(id);
        }
    }
}
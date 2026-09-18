package com.example.cache;

import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.jupiter.api.Test;

import com.example.cache.UserCache;
import com.example.cache.User;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class UserCacheTest {

    @Test
    void shouldAddAndRetrieveUser() {
        UserCache cache = new UserCache();
        User user = new User("1", "Alice");

        cache.addUser(user);

        assertSame(user, cache.getUser("1"));
    }

    @Test
    void shouldReturnNullWhenUserIsNotCached() {
        UserCache cache = new UserCache();

        assertNull(cache.getUser("missing"));
    }

    @Test
    void shouldIgnoreNullUserAndNullId() {
        UserCache cache = new UserCache();

        assertDoesNotThrow(() -> cache.addUser(null));
        assertDoesNotThrow(() -> cache.addUser(new User(null, "Alice")));

        assertNull(cache.getUser(null));
    }

    @Test
    void shouldInvalidateUser() {
        UserCache cache = new UserCache();
        User user = new User("1", "Alice");

        cache.addUser(user);
        cache.removeUser("1");

        assertNull(cache.getUser("1"));
    }

    @Test
    void shouldExpireUserAfterTtl() {
        AtomicLong time = new AtomicLong();

        Ticker ticker = time::get;

        UserCache cache =
                new UserCache(
                        100,
                        Duration.ofMinutes(10),
                        ticker
                );

        User user = new User("1", "Alice");

        cache.addUser(user);

        assertSame(user, cache.getUser("1"));

        time.addAndGet(Duration.ofMinutes(11).toNanos());

        assertNull(cache.getUser("1"));
    }

    @Test
    void shouldSupportConcurrentAccess() throws InterruptedException {
        UserCache cache = new UserCache();

        int userCount = 1_000;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {

            IntStream.range(0, userCount)
                    .forEach(i ->
                            executor.submit(() ->
                                    cache.addUser(
                                            new User(
                                                    String.valueOf(i),
                                                    "User-" + i
                                            )
                                    )
                            )
                    );

            executor.shutdown();

            assertTrue(
                    executor.awaitTermination(5, TimeUnit.SECONDS)
            );
        } finally {
            executor.shutdownNow();
        }

        for (int i = 0; i < userCount; i++) {
            assertNotNull(cache.getUser(String.valueOf(i)));
        }
    }
}
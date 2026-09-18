# User Cache Review

A small Java implementation that expands on the cache review discussed during the technical interview.

## Original Exercise

The original exercise was:

> Objective: Identify the problems with the cache.

```java
public class UserCache {

    private static final Map<String, User> userCache = new HashMap<>();

    public void addUser(User user) {
        userCache.put(user.getId(), user);
    }

    public User getUser(String id) {
        return userCache.get(id);
    }
}
```

The exercise did not specify cache capacity, expiration policy, persistence behavior, or an underlying source of truth.

This repository expands the original review into a small runnable implementation and documents the assumptions made where requirements were not provided.

## Identified Concerns

The original implementation has several potential issues:

- `HashMap` is not suitable for unsynchronized concurrent access.
- Cache growth is unbounded.
- Cached entries never expire.
- There is no explicit invalidation mechanism.
- Null input behavior is not defined.

## Approach

The implementation uses Caffeine instead of managing cache behavior directly with `ConcurrentHashMap`.

Caffeine provides:

- thread-safe cache access,
- bounded capacity,
- time-based expiration,
- explicit invalidation.

The implementation also defines explicit handling for null users and IDs.

## Assumptions and Requirements

Because the original exercise did not define detailed functional or non-functional requirements, the following requirements are assumptions made for this implementation.

### Functional Requirements

- A user can be added to the cache using the user's ID as the key.
- A cached user can be retrieved by ID.
- A cached user can be explicitly invalidated by ID.
- Invalid or null input should not cause the cache implementation to fail.

### Non-Functional Requirements

- The cache should support concurrent access safely.
- Cache growth should be bounded to reduce the risk of uncontrolled memory usage.
- Cached entries should expire after a defined period to limit stale data.
- Cache operations should remain simple and efficient for normal application use.

## Cache Policy

For demonstration purposes, the cache uses:

- Maximum size: `10,000` entries
- Expiration: `10 minutes` after write

```java
Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();
```

These values are illustrative defaults rather than production requirements.

In a production system, the appropriate cache size and expiration policy would depend on factors such as:

- expected working-set size,
- approximate object memory footprint,
- available JVM heap,
- traffic patterns,
- update frequency,
- acceptable data staleness.

## Scope

This implementation intentionally does not introduce database loading, distributed caching, persistence, or synchronization with an external source of truth because those behaviors were not specified in the original exercise.

If the cache were placed in front of a database or remote service, additional concerns could include:

- cache-aside loading,
- write-through or write-behind behavior,
- invalidation when source data changes,
- distributed-cache consistency,
- handling cache stampedes or repeated misses.

## Cache Semantics

`getUser` returns `null` when an entry is not present.

The implementation does not automatically load missing users from another source because no repository or source-of-truth behavior was specified.

## Concurrency

Caffeine provides thread-safe cache operations.

This makes access to the cache itself safe for concurrent use. It does not automatically make mutable objects stored inside the cache thread-safe, so cached values should ideally be immutable or have their mutation controlled separately.

## Build and Test

Requirements:

- Java 17+
- Maven

Run Test:

```bash
mvn clean test
```

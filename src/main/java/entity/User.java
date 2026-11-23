package entity;

import java.util.Objects;

/** Immutable account identity. The stored passwordHash includes a per-user salt. */
public final class User {
    private final String id;
    private final String username;
    private final String passwordHash;

    public User(String id, String username, String passwordHash) {
        this.id = Objects.requireNonNull(id, "id");
        this.username = Objects.requireNonNull(username, "username");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}

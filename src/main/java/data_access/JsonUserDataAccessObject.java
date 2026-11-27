package data_access;

import entity.User;
import use_case.login.LoginUserDataAccessInterface;
import use_case.signup.SignupUserDataAccessInterface;

import java.nio.file.Path;
import java.util.*;

/**
 * Persists username-based accounts in data/users.json.
 * The JSON format is an array of objects containing id, username and passwordHash.
 */
public final class JsonUserDataAccessObject
        implements SignupUserDataAccessInterface, LoginUserDataAccessInterface {

    private final JsonStorage jsonStorage;
    private final Path usersFile;

    public JsonUserDataAccessObject(JsonStorage jsonStorage, Path usersFile) {
        this.jsonStorage = Objects.requireNonNull(jsonStorage, "jsonStorage");
        this.usersFile = Objects.requireNonNull(usersFile, "usersFile");
    }

    @Override
    public synchronized boolean existsByName(String username) {
        if (username == null) return false;
        String target = normalize(username);
        for (User user : readUsers()) {
            if (normalize(user.getUsername()).equals(target)) return true;
        }
        return false;
    }

    @Override
    public synchronized User get(String username) {
        if (username == null) return null;
        String target = normalize(username);
        for (User user : readUsers()) {
            if (normalize(user.getUsername()).equals(target)) return user;
        }
        return null;
    }

    @Override
    public synchronized void save(User user) {
        if (user == null) throw new IllegalArgumentException("user cannot be null");

        List<User> users = readUsers();
        for (User existing : users) {
            if (normalize(existing.getUsername()).equals(normalize(user.getUsername()))
                    && !existing.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Username already exists.");
            }
        }

        users.removeIf(existing -> existing.getId().equals(user.getId()));
        users.add(user);

        List<Map<String, Object>> jsonUsers = new ArrayList<>();
        for (User savedUser : users) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("id", savedUser.getId());
            record.put("username", savedUser.getUsername());
            record.put("passwordHash", savedUser.getPasswordHash());
            jsonUsers.add(record);
        }
        jsonStorage.save(usersFile, jsonUsers);
    }

    private List<User> readUsers() {
        Object data = jsonStorage.load(usersFile);
        if (!(data instanceof List<?>)) {
            throw new IllegalStateException("Expected a JSON array in " + usersFile);
        }

        List<User> users = new ArrayList<>();
        for (Object item : (List<?>) data) {
            if (!(item instanceof Map<?, ?>)) {
                throw new IllegalStateException("Invalid user record in " + usersFile);
            }

            Map<?, ?> record = (Map<?, ?>) item;
            Object id = record.get("id");
            Object username = record.get("username");
            Object passwordHash = record.get("passwordHash");

            if (!(id instanceof String) || !(username instanceof String)
                    || !(passwordHash instanceof String)) {
                throw new IllegalStateException("User record must contain string id, username, "
                        + "and passwordHash fields in " + usersFile);
            }

            users.add(new User((String) id, (String) username, (String) passwordHash));
        }
        return users;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}

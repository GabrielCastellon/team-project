package App.domain;

public class User {
    private final String username;
    private final String email;
    private final String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // Prototype only: production applications should store password hashes,
    // not plaintext passwords.
    public boolean passwordMatches(String candidate) {
        return password.equals(candidate);
    }
}

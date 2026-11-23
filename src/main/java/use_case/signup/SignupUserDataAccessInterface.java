package use_case.signup;

import entity.User;

public interface SignupUserDataAccessInterface {
    boolean existsByName(String username);
    void save(User user);
}

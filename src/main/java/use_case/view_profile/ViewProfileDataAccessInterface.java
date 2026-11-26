package use_case.view_profile;

import entity.UserProfile;

import java.io.IOException;
import java.util.Optional;

//read-only on purpose (Interface Segregation): viewing a profile never needs to save one
/** Reads stored user profiles. */
public interface ViewProfileDataAccessInterface {
    //empty when the user has never saved a profile before; throws if the stored data can't be read
    /** Looks up the stored profile for a user, if one exists. */
    Optional<UserProfile> findProfile(String userId) throws IOException;
}

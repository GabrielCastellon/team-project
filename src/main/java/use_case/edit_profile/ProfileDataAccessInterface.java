package use_case.edit_profile;

import entity.UserProfile;

import java.io.IOException;
import java.util.Optional;

/** Reads and writes stored user profiles. */
public interface ProfileDataAccessInterface {
    //empty when the user has never saved a profile before
    /** Looks up the stored profile for a user, if one exists. */
    Optional<UserProfile> findProfile(String userId);

    //BR6a: propagates to the interactor as a save failure
    /** Persists a profile, replacing any previously stored value. */
    void saveProfile(UserProfile profile) throws IOException;
}

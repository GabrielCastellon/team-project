package use_case.view_profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import entity.UserProfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class ViewProfileInteractorTest {

    private static final String USER_ID = "demo-user";

    private final Map<String, UserProfile> stored = new HashMap<>();

    private boolean failLoad;

    private final ViewProfileDataAccessInterface dataAccess = userId -> {
        if (failLoad) {
            throw new IOException("disk unreadable");
        }
        return Optional.ofNullable(stored.get(userId));
    };

    private RecordingOutputBoundary output;
    private ViewProfileInteractor interactor;

    @BeforeEach
    void setup() {
        output = new RecordingOutputBoundary();
        interactor = new ViewProfileInteractor(dataAccess, output);
    }

    //fake output boundary: records the last call made
    private static class RecordingOutputBoundary implements ViewProfileOutputBoundary {
        ViewProfileOutputData success;
        String failureMessage;

        @Override
        public void prepareSuccessView(ViewProfileOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareFailView(String message) {
            failureMessage = message;
        }
    }

    @Test
    void storedProfileIsShown() {
        stored.put(USER_ID, new UserProfile(USER_ID, "Kieran", "I like photos", "/tmp/me.png", null));

        interactor.execute(new ViewProfileInputData(USER_ID));

        assertEquals("Kieran", output.success.getDisplayName());
        assertEquals("I like photos", output.success.getBiography());
        assertEquals("/tmp/me.png", output.success.getAvatarPath());
        assertNull(output.failureMessage);
    }

    @Test
    void missingProfileShowsDefault() {
        interactor.execute(new ViewProfileInputData(USER_ID));

        assertEquals("New user", output.success.getDisplayName());
        assertEquals("", output.success.getBiography());
        assertEquals("", output.success.getAvatarPath());
        assertNull(output.failureMessage);
    }

    @Test
    void readFailureReportsFailView() {
        failLoad = true;

        interactor.execute(new ViewProfileInputData(USER_ID));

        assertEquals("Couldn't load your profile, try again", output.failureMessage);
        assertNull(output.success);
    }
}

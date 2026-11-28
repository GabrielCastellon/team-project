package interface_adapter.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import use_case.edit_profile.EditProfileInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

class EditProfileControllerTest {

    private final List<Runnable> queued = new ArrayList<>();
    private final Executor recordingExecutor = queued::add;
    private final List<EditProfileInputData> received = new ArrayList<>();
    private final AtomicReference<String> signedIn = new AtomicReference<>("alice");
    private final EditProfileController controller =
            new EditProfileController(received::add, signedIn::get, recordingExecutor);

    @Test
    void runsInteractorOnExecutorWithUserIdAndFormValues() {
        controller.execute("Kieran", "bio", "/tmp/me.png");

        //nothing runs on the caller's thread: the work is only queued
        assertEquals(0, received.size());
        assertEquals(1, queued.size());

        queued.get(0).run();

        EditProfileInputData input = received.get(0);
        assertEquals("alice", input.getUserId());
        assertEquals("Kieran", input.getDisplayName());
        assertEquals("bio", input.getBiography());
        assertEquals("/tmp/me.png", input.getAvatarPath());
    }

    @Test
    void readsTheSignedInUserAtExecuteTime() {
        signedIn.set("bob");

        controller.execute("Bob", "", "");
        queued.get(0).run();

        assertEquals("bob", received.get(0).getUserId());
    }
}

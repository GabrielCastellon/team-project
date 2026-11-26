package interface_adapter.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import use_case.edit_profile.EditProfileInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

class EditProfileControllerTest {

    @Test
    void runsInteractorOnExecutorWithUserIdAndFormValues() {
        List<Runnable> queued = new ArrayList<>();
        Executor recordingExecutor = queued::add;
        List<EditProfileInputData> received = new ArrayList<>();
        EditProfileController controller =
                new EditProfileController(received::add, "demo-user", recordingExecutor);

        controller.execute("Kieran", "bio", "/tmp/me.png");

        //nothing runs on the caller's thread: the work is only queued
        assertEquals(0, received.size());
        assertEquals(1, queued.size());

        queued.get(0).run();

        EditProfileInputData input = received.get(0);
        assertEquals("demo-user", input.getUserId());
        assertEquals("Kieran", input.getDisplayName());
        assertEquals("bio", input.getBiography());
        assertEquals("/tmp/me.png", input.getAvatarPath());
    }
}

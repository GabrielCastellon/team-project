package interface_adapter.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import use_case.view_profile.ViewProfileInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

class ViewProfileControllerTest {

    private final List<Runnable> queued = new ArrayList<>();
    private final Executor recordingExecutor = queued::add;
    private final List<ViewProfileInputData> received = new ArrayList<>();
    private final AtomicReference<String> signedIn = new AtomicReference<>("alice");
    private final ViewProfileController controller =
            new ViewProfileController(received::add, signedIn::get, recordingExecutor);

    @Test
    void runsInteractorOnExecutorWithUserId() {
        controller.execute();

        //nothing runs on the caller's thread: the work is only queued
        assertEquals(0, received.size());
        assertEquals(1, queued.size());

        queued.get(0).run();

        assertEquals(1, received.size());
        assertEquals("alice", received.get(0).getUserId());
    }

    //the tab is built once, so the user id must be read when the request is made, not at construction
    @Test
    void readsTheSignedInUserAtExecuteTime() {
        signedIn.set("bob");

        controller.execute();
        queued.get(0).run();

        assertEquals("bob", received.get(0).getUserId());
    }

    @Test
    void passesNullWhenNobodyIsSignedIn() {
        signedIn.set(null);

        controller.execute();
        queued.get(0).run();

        assertNull(received.get(0).getUserId());
    }
}

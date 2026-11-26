package interface_adapter.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import use_case.view_profile.ViewProfileInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

class ViewProfileControllerTest {

    @Test
    void runsInteractorOnExecutorWithUserId() {
        List<Runnable> queued = new ArrayList<>();
        Executor recordingExecutor = queued::add;
        List<ViewProfileInputData> received = new ArrayList<>();
        ViewProfileController controller =
                new ViewProfileController(received::add, "demo-user", recordingExecutor);

        controller.execute();

        //nothing runs on the caller's thread: the work is only queued
        assertEquals(0, received.size());
        assertEquals(1, queued.size());

        queued.get(0).run();

        assertEquals(1, received.size());
        assertEquals("demo-user", received.get(0).getUserId());
    }
}

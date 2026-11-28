package interface_adapter.profile;

import use_case.view_profile.ViewProfileInputBoundary;
import use_case.view_profile.ViewProfileInputData;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/** Turns "show my profile" into a view-profile request, run off the UI thread. */
public class ViewProfileController {
    private final ViewProfileInputBoundary inputBoundary;
    //asked for the signed-in user's id (null if nobody) on every request, because the tab outlives
    //sign-ins and account switches. The interactor still only sees the id, never the session
    private final Supplier<String> currentUserId;
    //runs the interactor (which reads the disk) in the background so the UI stays responsive
    private final Executor executor;

    public ViewProfileController(ViewProfileInputBoundary inputBoundary, Supplier<String> currentUserId, Executor executor) {
        this.inputBoundary = inputBoundary;
        this.currentUserId = currentUserId;
        this.executor = executor;
    }

    /** Asks the interactor to load the current user's profile. */
    public void execute() {
        ViewProfileInputData inputData = new ViewProfileInputData(currentUserId.get());
        executor.execute(() -> inputBoundary.execute(inputData));
    }
}

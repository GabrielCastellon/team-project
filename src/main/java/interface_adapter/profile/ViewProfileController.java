package interface_adapter.profile;

import use_case.view_profile.ViewProfileInputBoundary;
import use_case.view_profile.ViewProfileInputData;

import java.util.concurrent.Executor;

/** Turns "show my profile" into a view-profile request, run off the UI thread. */
public class ViewProfileController {
    private final ViewProfileInputBoundary inputBoundary;
    //whose profile: passed in once from ProfileModule, so the interactor never reads a global session
    private final String userId;
    //runs the interactor (which reads the disk) in the background so the UI stays responsive
    private final Executor executor;

    public ViewProfileController(ViewProfileInputBoundary inputBoundary, String userId, Executor executor) {
        this.inputBoundary = inputBoundary;
        this.userId = userId;
        this.executor = executor;
    }

    /** Asks the interactor to load the current user's profile. */
    public void execute() {
        ViewProfileInputData inputData = new ViewProfileInputData(userId);
        executor.execute(() -> inputBoundary.execute(inputData));
    }
}

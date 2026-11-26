package interface_adapter.profile;

import use_case.edit_profile.EditProfileInputBoundary;
import use_case.edit_profile.EditProfileInputData;

import java.util.concurrent.Executor;

/** Turns the edit form's Save into an edit-profile request, run off the UI thread. */
public class EditProfileController {
    private final EditProfileInputBoundary inputBoundary;
    private final String userId;
    private final Executor executor;

    public EditProfileController(EditProfileInputBoundary inputBoundary, String userId, Executor executor) {
        this.inputBoundary = inputBoundary;
        this.userId = userId;
        this.executor = executor;
    }

    /** Submits the form's values; validation happens in the interactor, not here. */
    public void execute(String displayName, String biography, String avatarPath) {
        EditProfileInputData inputData = new EditProfileInputData(userId, displayName, biography, avatarPath);
        executor.execute(() -> inputBoundary.execute(inputData));
    }
}

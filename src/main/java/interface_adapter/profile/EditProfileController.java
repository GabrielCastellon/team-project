package interface_adapter.profile;

import use_case.edit_profile.EditProfileInputBoundary;
import use_case.edit_profile.EditProfileInputData;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/** Turns the edit form's Save into an edit-profile request, run off the UI thread. */
public class EditProfileController {
    private final EditProfileInputBoundary inputBoundary;
    private final Supplier<String> currentUserId;
    private final Executor executor;

    public EditProfileController(EditProfileInputBoundary inputBoundary, Supplier<String> currentUserId, Executor executor) {
        this.inputBoundary = inputBoundary;
        this.currentUserId = currentUserId;
        this.executor = executor;
    }

    /** Submits the form's values; validation happens in the interactor, not here. */
    public void execute(String displayName, String biography, String avatarPath) {
        EditProfileInputData inputData = new EditProfileInputData(currentUserId.get(), displayName, biography, avatarPath);
        executor.execute(() -> inputBoundary.execute(inputData));
    }
}

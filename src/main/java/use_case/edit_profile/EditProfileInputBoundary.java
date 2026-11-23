package use_case.edit_profile;

//interface gives controller way to submit request to interactor
/** Boundary the controller calls to submit an edit-profile request. */
public interface EditProfileInputBoundary {
    /** Validates the submitted data and saves it, or reports why it couldn't. */
    void execute(EditProfileInputData inputData);
}

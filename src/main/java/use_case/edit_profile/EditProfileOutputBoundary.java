package use_case.edit_profile;

/** Boundary the interactor calls to report how an edit-profile request finished. */
public interface EditProfileOutputBoundary {
    /** Called when the profile was validated and saved successfully. */
    void prepareSuccessView(EditProfileOutputData outputData);

    /** Called when one field failed validation; nothing was saved. */
    void prepareValidationFailView(ProfileField field, String message);

    /** Called when saving failed for a reason other than validation. */
    void prepareFailView(String message);
}

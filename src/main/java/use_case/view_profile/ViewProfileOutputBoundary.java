package use_case.view_profile;

/** Boundary the interactor calls to report how loading a profile finished. */
public interface ViewProfileOutputBoundary {
    /** Called with the profile to display. */
    void prepareSuccessView(ViewProfileOutputData outputData);

    /** Called when the stored profile couldn't be read. */
    void prepareFailView(String message);

    /** Called when nobody is signed in, so there is no profile to show. */
    void prepareSignInRequiredView();
}

package use_case.view_profile;

//interface gives the controller a way to ask the interactor for a profile
/** Boundary the controller calls to load a user's profile. */
public interface ViewProfileInputBoundary {
    /** Loads the stored profile (or a default one) and reports it to the output boundary. */
    void execute(ViewProfileInputData inputData);
}

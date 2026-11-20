package use_case.save_pin;

public interface SavePinOutputBoundary {
    void prepareSuccessView(SavePinOutputData outputData);
    void prepareAlreadySavedView(String photoId);
    void prepareFailView(String message);
}

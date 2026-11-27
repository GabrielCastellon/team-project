package use_case.view_pins;

import java.util.List;

//view saved pins --> interactor reports lookup results to presenter; similar to part 2D sequence
public interface ViewPinsOutputBoundary {
    //success presentation for empty/filled collection
    void presentPins(List<String> photoDescriptions);
    void presentError(String message); //if collection cannot be loaded
}

package use_case.view_pins;

import entity.Photo;
import entity.Pin;

import java.util.ArrayList;
import java.util.List;

/*view saved pins -> lookup pins by owner. execute() requests DA boundary for saved pins */
public class ViewPinsInteractor implements ViewPinsInputBoundary{
    private final ViewPinsDataAccessInterface storage;
    private final ViewPinsOutputBoundary output;

    public ViewPinsInteractor(ViewPinsDataAccessInterface storage, ViewPinsOutputBoundary output){
        this.output = output;
        this.storage = storage;
    }

    /*control flow: view saved pins -> interactor -> output boundary. Failed lookup calls
    presentError, while success calls presentPins
     */
    @Override
    public void execute(String ownerId){
        List<Pin> pins;
        try{
            pins = storage.findByOwnerId(ownerId);
        } catch (IllegalStateException ex) {
            output.presentError("Could not load saved pins");
            return;
        }
        List<ViewPinItem> items = new ArrayList<>();
        for (Pin pin : pins){
            Photo photo = pin.getPhoto();
            items.add(new ViewPinItem(photo.description(), photo.imageUrl(), photo.photographer()));
        }
        //use case output boundary
        output.presentPins(items);
    }
}

package interface_adapter.view_pins;

import use_case.view_pins.ViewPinsInputBoundary;

//controller sends request to interactor via input boundary to load signed-in user's saved pins
public class ViewPinsController {
    private final ViewPinsInputBoundary interactor;

    public ViewPinsController(ViewPinsInputBoundary interactor){
        this.interactor = interactor;
    }

    //takes ownerId to load specific owner's pins
    public void execute(String ownerId){
        interactor.execute(ownerId);
    }
}

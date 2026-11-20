package interface_adapter.save_pin;

import entity.Photo;
import use_case.save_pin.SavePinInputBoundary;
import use_case.save_pin.SavePinInputData;

public class SavePinController {
    private final SavePinInputBoundary inputBoundary;
    //construct controller that depends on inputBoundary
    public SavePinController(SavePinInputBoundary inputBoundary){
        this.inputBoundary = inputBoundary;
    }

    public void execute(String ownerId, Photo photo){
        //construct the inputData
        SavePinInputData inputData = new SavePinInputData(ownerId, photo);
        //call use case interactor via interface to perform savepin steps
        inputBoundary.execute(inputData);
    }

}

package use_case.save_pin;

import entity.Pin;

import java.util.UUID;

public class SavePinInteractor implements SavePinInputBoundary {
    private final PinDataAccessInterface pinDataAccess;
    private final SavePinOutputBoundary outputBoundary;

    public SavePinInteractor(PinDataAccessInterface pinDataAccess,
                             SavePinOutputBoundary outputBoundary){
        this.pinDataAccess = pinDataAccess;
        this.outputBoundary = outputBoundary;
    }
    //part 2: checks if pin already exists; if it does, then calls outputBoundary -> presenter
    //otherwise, construct new pin and prepare success view.
    @Override
    public void execute(SavePinInputData inputData){
        String ownerId = inputData.getOwnerId();
        String photoId = inputData.getPhoto().id();

        //2A: lookup for existing saved pin
        if (pinDataAccess.existsByOwnerAndPhotoId(ownerId, photoId)){
            outputBoundary.prepareAlreadySavedView(photoId);
            return;
        } //else, 2B: create a pin for the first time
        //generate pinId
        String pinId = UUID.randomUUID().toString();
        //construct a new pin
        Pin pin = new Pin(pinId, ownerId, inputData.getPhoto());
        //2C: interactor asks storage to save the newly created pin
        pinDataAccess.save(pin); //hands Pin object to DA interface

        //2D: after save returns, report success to the output boundary with packaged outputData
        SavePinOutputData outputData = new SavePinOutputData(pinId, photoId);
        outputBoundary.prepareSuccessView(outputData);
    }
}

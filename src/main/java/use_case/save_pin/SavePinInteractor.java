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

        //2A: check whether this owner already saved the photo
        boolean alreadySaved;
        try {
            alreadySaved = pinDataAccess.existsByOwnerAndPhotoId(ownerId, photoId);
        } catch (IllegalStateException ex) {
            outputBoundary.prepareFailView("Could not check whether the photo is already saved. " +
                    "Please try again"); //failure lookup case
            return;
        }
        if (alreadySaved) {
            outputBoundary.prepareAlreadySavedView(photoId);
            return;
        }
        //else, 2B: create a pin for the first time
        //generate pinId
        String pinId = UUID.randomUUID().toString();
        //construct a new pin
        Pin pin = new Pin(pinId, ownerId, inputData.getPhoto());
        //2C: interactor asks storage to save the newly created pin
        try{
            pinDataAccess.save(pin);
        } catch (IllegalStateException ex) {
            //4B: flag the failed storage attempt and finish the request
            outputBoundary.prepareFailView("Could not save the pin. Please try again.");
            return;
        }

        //2D: after save returns, report success to the output boundary with packaged outputData
        SavePinOutputData outputData = new SavePinOutputData(pinId, photoId);
        outputBoundary.prepareSuccessView(outputData);
    }
}

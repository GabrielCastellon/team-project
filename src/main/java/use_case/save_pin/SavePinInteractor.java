package use_case.save_pin;

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
        }
    }
}

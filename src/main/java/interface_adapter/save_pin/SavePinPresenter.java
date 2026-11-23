package interface_adapter.save_pin;

import use_case.save_pin.SavePinOutputBoundary;
import use_case.save_pin.SavePinOutputData;

//Parts 3, 4A, 4B
public class SavePinPresenter implements SavePinOutputBoundary{
    private final SavePinViewModel viewModel;

    public SavePinPresenter(SavePinViewModel viewModel) {
        this.viewModel = viewModel;
    }

    //part 2D -> 3. pin saved and interactor hands result to presenter, used to build pin state
    @Override
    public void prepareSuccessView(SavePinOutputData outputData){
        SavePinState state = new SavePinState(outputData.getPhotoId(), outputData.getPinId(),
                true, "Saved");
        viewModel.setState(state); //hands state to viewmodel instance
    }

    //part 4A photo is already saved; boundary only gives photo ID
    @Override
    public void prepareAlreadySavedView(String photoId){
        SavePinState state = new SavePinState(photoId, null, true,
                "Already saved");
        viewModel.setState(state);
    }

    //4B output boundary only gives failure message
    @Override
    public void prepareFailView(String message){
        SavePinState state = new SavePinState(null, null, false, message);
        viewModel.setState(state);
    }
}

package interface_adapter.save_pin;

import org.junit.jupiter.api.Test;
import use_case.save_pin.SavePinOutputData;

import static org.junit.jupiter.api.Assertions.*;

public class SavePinPresenterTest {

    @Test
    void testSuccessUpdateViewModel(){
        SavePinViewModel viewModel = new SavePinViewModel();
        SavePinPresenter presenter = new SavePinPresenter(viewModel);
        SavePinOutputData outputData = new SavePinOutputData("321", "121");

        //from part 2D to part 3; testing presenter directly
        presenter.prepareSuccessView(outputData);

        //checks what the view model holds after presenter passes outputData
        SavePinState state = viewModel.getState();
        assertNotNull(state);
        assertEquals("121", state.getPhotoId());
        assertEquals("321", state.getPinId());
        assertTrue(state.isSaved()); //checks what view will be able to read
        assertEquals("Saved", state.getMessage());
    }

    @Test
    void testAlreadySavedUpdateViewModel(){
        SavePinViewModel viewModel = new SavePinViewModel();
        SavePinPresenter presenter = new SavePinPresenter(viewModel);

        presenter.prepareAlreadySavedView("121");

        SavePinState state= viewModel.getState();
        assertEquals("121", state.getPhotoId());
        assertTrue(state.isSaved());
        assertNull(state.getPinId());
        assertEquals("Already saved", state.getMessage());
    }

    @Test
    void testFailureUpdateViewModel(){
        SavePinViewModel viewModel = new SavePinViewModel();
        SavePinPresenter presenter = new SavePinPresenter(viewModel);

        presenter.prepareFailView("Could not save this pin");

        //checking if viewModel receives appropriate state changes from presenter
        SavePinState state = viewModel.getState();
        assertFalse(state.isSaved());
        assertNull(state.getPhotoId());
        assertNull(state.getPinId());
        assertEquals("Could not save this pin", state.getMessage());
    }
}

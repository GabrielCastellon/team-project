package use_case.save_pin;

import entity.Photo;
import entity.Pin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SavePinInteractorTest {

    //unit test: checks behavior of interactor when user requests save for pin that is already saved.
    @Test
    void testRedundantSaveRequest() {
        Photo photo = new Photo("121", "Orange Cat",
                "image-url", "page-url",
                "photographer", "profile-url", "download-url");
        SavedPinDataAccess storage = new SavedPinDataAccess(); //test object
        RecordOutputBoundary presenter = new RecordOutputBoundary(); //test object
        SavePinInteractor interactor = new SavePinInteractor(storage, presenter); //real interactor

        //2A returns true as Interactor depends on fake DA interface implemented below
        //then output boundary implemented by fake presenter prepares the already saved view
        interactor.execute(new SavePinInputData("442", photo));

        assertEquals("442", storage.checkOwnerId); //checks if interactor asks about owner
        assertEquals("121", storage.checkPhotoId); //check if it asks about photo for owner
        //Check if interactor reports the same photo as the one already saved
        assertEquals("121", presenter.savedPhotoId);
        //Checks if interactor stops before writing a duplicate
        assertEquals(0, storage.saveCalls);
    }

    private static class SavedPinDataAccess implements PinDataAccessInterface {
        private String checkOwnerId;
        private String checkPhotoId;
        private int saveCalls;

        //overriding fake DA method that interactor depends on automatically returns true
        @Override
        public boolean existsByOwnerAndPhotoId(String ownerId, String photoId){
            checkOwnerId = ownerId;
            checkPhotoId = photoId;
            return true;
        }

        @Override
        public void save(Pin pin){
            saveCalls++;
        }
    }

    private static class RecordOutputBoundary implements SavePinOutputBoundary {
        private String savedPhotoId;

        @Override
        public void prepareAlreadySavedView(String photoId) {
            savedPhotoId = photoId;
        }

        @Override
        public void prepareSuccessView(SavePinOutputData outputData) {
            fail("A duplicate pin was reported as a new save");
        }

        @Override
        public void prepareFailView(String messsage) {
            fail("A duplicate pin was reported as a storage failure");
        }
    }
}

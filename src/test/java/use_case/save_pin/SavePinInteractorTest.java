package use_case.save_pin;

import entity.Photo;
import entity.Pin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SavePinInteractorTest {

    //unit test: checks behavior of interactor when user requests save for pin that is already saved.
    @Test
    void testRedundantSaveRequest() {
        Photo photo = new Photo("121", "Orange Cat",
                "image-url", "page-url",
                "photographer", "profile-url", "download-url");
        SavedPinDataAccess storage = new SavedPinDataAccess(); //test object
        storage.alreadySaved = true; //case where user already saved the photo before (2A)
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
        //Checks if presenter does not follow through with displaying successful save of duplicate
        assertNull(presenter.successData);
        //part 4B: should not report storage failure in case of duplicate save request
        assertNull(presenter.failureMessage);
    }

    //part 2B: testing first save request behavior
    @Test
    void testFirstSaveRequest() {
        Photo photo = new Photo("121", "Orange Cat",
                "image-url", "page-url",
                "photographer", "profile-url", "download-url");
        SavedPinDataAccess storage = new SavedPinDataAccess();
        storage.alreadySaved = false;
        RecordOutputBoundary presenter = new RecordOutputBoundary();
        SavePinInteractor interactor = new SavePinInteractor(storage, presenter);

        interactor.execute(new SavePinInputData("123", photo));

        //Part 2C: storage receives 1 pin containing the selected photo
        assertEquals(1, storage.saveCalls);
        assertSame(photo, storage.savedPin.getPhoto()); //checks if first save(pin) is successful

        //Part 2D: for first save, presenter success view should be called by interactor
        //presenter receives photo ID and ID of newly saved pin
        assertNotNull(presenter.successData);
        assertEquals(photo.id(), presenter.successData.getPhotoId());
        assertEquals(storage.savedPin.getPinId(), presenter.successData.getPinId());
        assertNull(presenter.savedPhotoId); //prepareAlreadySavedView was never called
        //part 4B: should not report storage failure in case of successful save
        assertNull(presenter.failureMessage);
    }

    //part 4B: test case of persistence failure
    @Test
    void testSaveFailureBehavior() {
        Photo photo = new Photo("121", "Orange Cat",
                "image-url", "page-url",
                "photographer", "profile-url", "download-url");
        SavedPinDataAccess storage = new SavedPinDataAccess();
        storage.alreadySaved = false;
        storage.failOnSave = true;
        RecordOutputBoundary presenter = new RecordOutputBoundary();
        SavePinInteractor interactor = new SavePinInteractor(storage, presenter);

        interactor.execute(new SavePinInputData("123", photo));

        assertEquals(1, storage.saveCalls);
        assertNull(storage.savedPin);
        assertNotNull(presenter.failureMessage);
        assertFalse(presenter.failureMessage.isBlank());
        assertNull(presenter.successData);
        assertNull(presenter.savedPhotoId);
    }


    //test storage lookup failure
    @Test
    void testLookupFailureSkipsSave() {
        Photo photo = new Photo("121", "Orange Cat",
                "image-url", "page-url",
                "photographer", "profile-url", "download-url");
        SavedPinDataAccess storage = new SavedPinDataAccess();
        storage.alreadySaved = false;
        storage.failOnLookup = true;
        RecordOutputBoundary presenter = new RecordOutputBoundary();
        SavePinInteractor interactor = new SavePinInteractor(storage, presenter);

        interactor.execute(new SavePinInputData("123", photo));

        assertEquals(0, storage.saveCalls);
        assertNull(storage.savedPin);
        assertNotNull(presenter.failureMessage);
        assertFalse(presenter.failureMessage.isBlank());
        assertNull(presenter.successData);
        assertNull(presenter.savedPhotoId);
    }

    private static class SavedPinDataAccess implements PinDataAccessInterface {
        private String checkOwnerId;
        private String checkPhotoId;
        private int saveCalls;
        //variables for testing save
        private boolean alreadySaved;
        private Pin savedPin;
        //part 4B: persistence failure
        private boolean failOnSave;
        //case of storage failure due to bad read of saved pins
        private boolean failOnLookup;


        //overriding fake DA method that interactor depends on automatically returns true
        @Override
        public boolean existsByOwnerAndPhotoId(String ownerId, String photoId){
            checkOwnerId = ownerId;
            checkPhotoId = photoId;
            if (failOnLookup) {
                throw new IllegalStateException("Simulated lookup failure");
            }
            return alreadySaved;
        }

        @Override
        public void save(Pin pin){
            saveCalls++; //record save attempt count
            if(failOnSave) {
                throw new IllegalStateException("Simulated write failure");
            }
            savedPin = pin;
        }
    }

    private static class RecordOutputBoundary implements SavePinOutputBoundary {
        private String savedPhotoId;
        //part 2D: test if fake presenter receives correct outputData from interactor
        private SavePinOutputData successData;
        //part 4B: persistence failure output behavior
        private String failureMessage;

        @Override
        public void prepareAlreadySavedView(String photoId) {
            savedPhotoId = photoId;
        }

        @Override
        public void prepareSuccessView(SavePinOutputData outputData) {
            successData = outputData;
        }

        @Override
        public void prepareFailView(String messsage) {
            failureMessage = messsage;
        }
    }
}

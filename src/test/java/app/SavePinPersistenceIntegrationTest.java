package app;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data_access.JsonPinDataAccessObject;
import entity.Photo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.save_pin.SavePinInputData;
import use_case.save_pin.SavePinInteractor;
import use_case.save_pin.SavePinOutputBoundary;
import use_case.save_pin.SavePinOutputData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

//integration test for pr 17, 18: testing if first save makes second lookup true for saved pin
//real interactor now working with real DAO (but still using fake implementation of output boundary
//2A -> 2B -> 2C -> 2D first request creates pin and sends success message to output boundary
//2A -> 4A second request lookup finds the first pin and interactor reports already saved
public class SavePinPersistenceIntegrationTest {
    //test a real temporary folder; both requests use same pins.json inside
    //creating second DAO simulates reopening storage; lookup finds pin first request wrote
    @TempDir
    Path directory;

    //integration test; IOException for case where JSON readString does not work
    @Test
    void testSecondSaveRequest() throws IOException{
        Path file = directory.resolve("pins.json"); //path that holds saved pins DAO will save

        Photo photo = new Photo("121", "Orange Cat", "image-url",
                "page-url", "photographer",
                "profile-url", "download-url");
        //supply input data in place of controller to focus on interactor with storage
        SavePinInputData request = new SavePinInputData("123", photo);
        //writes pins as JSON text
        JsonPinDataAccessObject storage = new JsonPinDataAccessObject(file);

        RecordOutputBoundary firstPresenter = new RecordOutputBoundary(); //fake presenter
        SavePinInteractor interactor = new SavePinInteractor(storage, firstPresenter);

        interactor.execute(request); //UML 2A successful first request --> pin creation -> display

        assertTrue(Files.exists(file));
        assertNotNull(firstPresenter.successData);
        assertEquals("121", firstPresenter.successData.getPhotoId());

        //next test 2A -> 4A; interactor and DAO behavior for case where pin already saved
        //file points to JSON object the first save wrote; request contains same owner & photo ID
        JsonPinDataAccessObject reopenedStorage = new JsonPinDataAccessObject(file);
        RecordOutputBoundary secondPresenter = new RecordOutputBoundary();
        SavePinInteractor secondInteractor = new
                SavePinInteractor(reopenedStorage, secondPresenter);

        secondInteractor.execute(request); //pin already saved in DAO

        assertEquals("121", secondPresenter.savedPhotoId); //alreadySaved view
        assertNull(secondPresenter.successData); //not successView
        assertNull(secondPresenter.failureMessage); //not failView

        //BR-8 after both requests pins.json should contain exactly one pin with the original pinID
        String json = Files.readString(file); //get JSON object
        JsonArray pins = JsonParser.parseString(json).getAsJsonArray();
        assertEquals(1, pins.size());//catches duplicates

        JsonObject savedPin = pins.get(0).getAsJsonObject();
        assertEquals(firstPresenter.successData.getPinId(), savedPin.get("pinId").getAsString());
        //tests if retrieved DAO is the same pin as what output boundary receives from interactor

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

        //stores output to inspect interactor behavior
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

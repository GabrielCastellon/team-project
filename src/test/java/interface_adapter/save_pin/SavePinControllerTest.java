package interface_adapter.save_pin;

import entity.Photo;
import org.junit.jupiter.api.Test;
import use_case.save_pin.SavePinInputBoundary;
import use_case.save_pin.SavePinInputData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

//unit test controller behavior
class SavePinControllerTest {
    private static class RecordedInputBoundary implements SavePinInputBoundary {
        final List<SavePinInputData> recordedInputs = new ArrayList<>();

        @Override
        public void execute(SavePinInputData inputData){
            recordedInputs.add(inputData);
        }
    }
    //checks if controller correctly passes inputData to the fake interactor implementation
    @Test
    void testOwnerIdAndPhotoRecord(){
        Photo photo = new Photo("123", "Orange Cat", "image-url", "page-url",
                "photographer", "profile-url", "download-url");
        RecordedInputBoundary boundary = new RecordedInputBoundary();
        SavePinController controller = new SavePinController(boundary);

        controller.execute("444", photo);

        assertEquals(1, boundary.recordedInputs.size());
        assertEquals("444", boundary.recordedInputs.get(0).getOwnerId());
        assertSame(photo, boundary.recordedInputs.get(0).getPhoto());
    }
}

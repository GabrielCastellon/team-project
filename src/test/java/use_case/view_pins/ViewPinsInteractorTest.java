package use_case.view_pins;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import entity.Photo;
import entity.Pin;

public class ViewPinsInteractorTest {

    @Test
    void testOwnerPhotoDescriptions(){
        Photo photo = new Photo(
                "121", "Orange Cat", "image-url", "page-url",
                "photographer", "profile-url", "download-url"
        );
        Pin pin = new Pin("pin-a", "alex", photo);
        //interactor asks fake storage for user's pins; fake implementation of findByOwnerId
        ViewPinsDataAccessInterface storage = ownerId -> {
            assertEquals("alex", ownerId);
            return List.of(pin);
        };
        RecordingOutput output = new RecordingOutput();

        //Act
        ViewPinsInteractor interactor = new ViewPinsInteractor(storage, output);
        interactor.execute("alex");

        //check what recordingoutput receives
        assertEquals(List.of("Orange Cat"), output.descriptions);

    }

    public static class RecordingOutput implements ViewPinsOutputBoundary{
        private List<String> descriptions;

        @Override
        public void presentError(String message) {
            fail(message);
        }

        @Override
        public void presentPins(List<String> descriptions) {
            this.descriptions = descriptions;
        }

    }
}

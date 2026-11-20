package use_case.save_pin;
import entity.Photo;

public class SavePinInputData {
    private final String ownerId;
    private final Photo photo;

    public SavePinInputData(String ownerId, Photo photo) {
        this.ownerId = ownerId;
        this.photo = photo;
    }

    public String getOwnerId(){ return this.ownerId; }
    public Photo getPhoto(){ return this.photo; }
}



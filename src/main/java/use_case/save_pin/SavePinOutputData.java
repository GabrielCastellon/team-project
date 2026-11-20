package use_case.save_pin;

public class SavePinOutputData {
    private final String pinId;
    private final String photoId;
    public SavePinOutputData(String pinId, String photoId){
        this.pinId = pinId; //identifies the saved record BR7
        this.photoId = photoId; //identifies discover card to mark as saved
    }

    public String getPinId() {
        return pinId;
    }

    public String getPhotoId() {
        return photoId;
    }
}

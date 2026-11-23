package interface_adapter.save_pin;

//Part 3: describes the outcome state of one pin that is being observed
//presenter's job was to change the pin data by giving view model a new state
public class SavePinState {
    private final String photoId;
    private final String pinId;
    private final boolean saved;
    private final String message;

    public SavePinState(String photoId, String pinId, boolean saved, String message){
        this.photoId = photoId;
        this.pinId = pinId;
        this.saved = saved;
        this.message = message;
    }

    public String getPhotoId(){
        return photoId;
    }
    public String getPinId() {
        return pinId;
    }

    public boolean isSaved() {
        return saved;
    }

    public String getMessage() {
        return message;
    }

}

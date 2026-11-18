package entity;

import java.util.List;

public class Pin {
    private String description;
    private Photo photo;
    private String pinId;
    private String ownerId;
    private String title;
    private List<String> tags;

    public Pin(String pinId, String ownerId, Photo photo){
        this.pinId = pinId;
        this.ownerId = ownerId;
        this.photo = photo;
        this.title = "";
        this.description = "";
        this.tags = List.of();
    }

    public Photo getPhoto(){ return this.photo; }
    public String getDescription(){ return this.description; }
}

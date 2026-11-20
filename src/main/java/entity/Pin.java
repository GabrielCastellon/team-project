package entity;

import java.util.List;

import java.util.Objects;

public class Pin {
    private String description;
    private final Photo photo;
    private final String pinId;
    private final String ownerId;
    private String title;
    private List<String> tags;

    public Pin(String pinId, String ownerId, Photo photo){
        this.pinId = Objects.requireNonNull(pinId, "pinId is required.");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId is required.");
        this.photo = Objects.requireNonNull(photo, "photo is required.");
        this.title = "";
        this.description = "";
        this.tags = List.of();
    }

    public Photo getPhoto(){ return this.photo; }
    public String getDescription(){ return this.description; }
}

package entity;

import java.util.Objects;

public final class Photo {

    private final String id;

    private final String description;

    private final String imageUrl;

    private final String pageUrl;

    private final String photographer;

    private final String photographerUrl;

    private final String downloadUrl;

    public Photo(
            String id,
            String description,
            String imageUrl,
            String pageUrl,
            String photographer,
            String photographerUrl,
            String downloadUrl) {
        this.id = Objects.requireNonNull(id);
        this.description = description == null ? "Untitled photo" : description;
        this.imageUrl = Objects.requireNonNull(imageUrl);
        this.pageUrl = Objects.requireNonNull(pageUrl);
        this.photographer = Objects.requireNonNull(photographer);
        this.photographerUrl = Objects.requireNonNull(photographerUrl);
        this.downloadUrl = Objects.requireNonNull(downloadUrl);
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    public String imageUrl() {
        return imageUrl;
    }

    public String pageUrl() {
        return pageUrl;
    }

    public String photographer() {
        return photographer;
    }

    public String photographerUrl() {
        return photographerUrl;
    }

    public String downloadUrl() {
        return downloadUrl;
    }
}

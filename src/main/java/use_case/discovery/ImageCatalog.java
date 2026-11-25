package use_case.discovery;

import entity.Photo;

import java.io.IOException;
import java.util.List;

public interface ImageCatalog {

    List<Photo> search(String query, int page, String orientation, String order) throws IOException;

    void trackSelection(Photo photo) throws IOException;
}

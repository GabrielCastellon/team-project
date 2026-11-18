package use_case.discovery;

import entity.Photo;

import java.io.IOException;
import java.util.List;

public interface PreferenceStore {

    List<Photo> load(String profile) throws IOException;

    void save(String profile, List<Photo> photos) throws IOException;
}

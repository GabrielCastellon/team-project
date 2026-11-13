package use_case.discovery;

import entity.Photo;
import entity.ScoredPhoto;

import java.io.IOException;
import java.util.List;

public interface DiscoveryInputBoundary {
    List<Photo> search(String query, int page, String orientation, String order) throws IOException;

    List<Photo> samples(String profile) throws IOException;

    void like(String profile, Photo photo) throws IOException;

    void remove(String profile, String photoId) throws IOException;

    List<ScoredPhoto> recommend(List<Photo> candidates, String profile) throws IOException;

    List<ScoredPhoto> mix(List<Photo> candidates, String a, String b, double weight)
            throws IOException;

    List<ScoredPhoto> group(List<Photo> candidates, List<String> profiles) throws IOException;

    List<ScoredPhoto> analyze(String profile) throws IOException;
}

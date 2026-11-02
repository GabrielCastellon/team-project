package use_case.discovery;

import entity.FeatureVector;
import entity.Photo;
import entity.ScoredPhoto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DiscoveryService {

    private final ImageCatalog catalog;

    private final EmbeddingProvider embeddings;

    private final PreferenceStore preferences;

    public DiscoveryService(
            ImageCatalog catalog, EmbeddingProvider embeddings, PreferenceStore preferences) {
        this.catalog = catalog;
        this.embeddings = embeddings;
        this.preferences = preferences;
    }

    public List<Photo> search(String query, int page, String orientation, String order)
            throws IOException {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Enter a search term");
        }
        if (page < 1) {
            throw new IllegalArgumentException("Page must be positive");
        }
        if (!List.of("", "landscape", "portrait", "squarish").contains(orientation)) {
            throw new IllegalArgumentException("Unknown orientation");
        }
        if (!List.of("relevant", "latest").contains(order)) {
            throw new IllegalArgumentException("Unknown sort order");
        }
        return List.copyOf(catalog.search(query.trim(), page, orientation, order));
    }

    public List<Photo> samples(String profile) throws IOException {
        checkProfile(profile);
        return List.copyOf(preferences.load(profile.trim()));
    }

    public void like(String profile, Photo photo) throws IOException {
        List<Photo> photos = new ArrayList<>(samples(profile));
        for (Photo savedPhoto : photos) {
            if (savedPhoto.id().equals(photo.id())) {
                return;
            }
        }
        embeddings.embed(photo);
        catalog.trackSelection(photo);
        photos.add(photo);
        preferences.save(profile.trim(), photos);
    }

    public void remove(String profile, String photoId) throws IOException {
        List<Photo> photos = new ArrayList<>(samples(profile));
        photos.removeIf(p -> p.id().equals(photoId));
        preferences.save(profile.trim(), photos);
    }

    public FeatureVector preference(String profile) throws IOException {
        List<FeatureVector> vectors = new ArrayList<>();
        for (Photo photo : samples(profile)) {
            vectors.add(embeddings.embed(photo));
        }
        return FeatureVector.average(vectors);
    }

    public List<ScoredPhoto> recommend(List<Photo> candidates, String profile) throws IOException {
        return rank(candidates, List.of(preference(profile)), excluded(List.of(profile)));
    }

    public List<ScoredPhoto> mix(List<Photo> candidates, String a, String b, double weight)
            throws IOException {
        if (!Double.isFinite(weight) || weight < 0 || weight > 1) {
            throw new IllegalArgumentException("Invalid mix weight");
        }
        if (weight == 1) {
            return recommend(candidates, a);
        }
        if (weight == 0) {
            return recommend(candidates, b);
        }
        return rank(
                candidates,
                List.of(preference(a).mix(preference(b), weight)),
                excluded(List.of(a, b)));
    }

    public List<ScoredPhoto> group(List<Photo> candidates, List<String> profiles)
            throws IOException {
        if (profiles.isEmpty()) {
            throw new IllegalArgumentException("Choose at least one group member");
        }
        List<FeatureVector> targets = new ArrayList<>();
        for (String profile : profiles) {
            targets.add(preference(profile));
        }
        return rank(candidates, targets, excluded(profiles));
    }

    public List<ScoredPhoto> analyze(String profile) throws IOException {
        List<Photo> photos = samples(profile);
        if (photos.size() < 2) {
            throw new IllegalArgumentException("Board analysis needs at least two images");
        }
        FeatureVector centroid = preference(profile);
        List<ScoredPhoto> scores = new ArrayList<>();
        for (Photo photo : photos) {
            scores.add(new ScoredPhoto(photo, embeddings.embed(photo).cosine(centroid)));
        }
        scores.sort(
                Comparator.comparingDouble(ScoredPhoto::score).thenComparing(s -> s.photo().id()));
        return List.copyOf(scores);
    }

    private List<ScoredPhoto> rank(
            List<Photo> candidates, List<FeatureVector> targets, Set<String> excluded)
            throws IOException {
        List<ScoredPhoto> scores = new ArrayList<>();
        Set<String> seen = new HashSet<>(excluded);
        for (Photo photo : candidates) {
            if (!seen.add(photo.id())) {
                continue;
            }
            FeatureVector vector = embeddings.embed(photo);
            double score = 1;
            for (FeatureVector target : targets) {
                score = Math.min(score, vector.cosine(target));
            }
            scores.add(new ScoredPhoto(photo, score));
        }
        scores.sort(
                Comparator.comparingDouble(ScoredPhoto::score)
                        .reversed()
                        .thenComparing(s -> s.photo().id()));
        return List.copyOf(scores);
    }

    private Set<String> excluded(List<String> profiles) throws IOException {
        Set<String> ids = new HashSet<>();
        for (String profile : profiles) {
            for (Photo photo : samples(profile)) {
                ids.add(photo.id());
            }
        }
        return ids;
    }

    private static void checkProfile(String profile) {
        if (profile == null || profile.isBlank()) {
            throw new IllegalArgumentException("Enter a board/user preference ID");
        }
    }
}

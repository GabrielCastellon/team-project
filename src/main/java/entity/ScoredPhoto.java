package entity;

public final class ScoredPhoto {

    private final Photo photo;

    private final double score;

    public ScoredPhoto(Photo photo, double score) {
        this.photo = photo;
        this.score = score;
    }

    public Photo photo() {
        return photo;
    }

    public double score() {
        return score;
    }
}

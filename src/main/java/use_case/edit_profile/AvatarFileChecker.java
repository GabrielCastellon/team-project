package use_case.edit_profile;

//narrow seam so the interactor can be unit tested without touching the real filesystem
/** Checks whether a path points to a file the app can read. */
public interface AvatarFileChecker {
    /** Returns true if the path is an existing, readable file. */
    boolean isReadableFile(String path);
}

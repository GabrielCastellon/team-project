package data_access;

import use_case.edit_profile.AvatarFileChecker;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/** Checks the real filesystem for a readable avatar file. */
public final class FileSystemAvatarFileChecker implements AvatarFileChecker {

    @Override
    public boolean isReadableFile(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        try {
            Path candidate = Path.of(path);
            return Files.isRegularFile(candidate) && Files.isReadable(candidate);
        } catch (InvalidPathException ex) {
            return false;
        }
    }
}

package data_access;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileSystemAvatarFileCheckerTest {

    @TempDir Path directory;

    private final FileSystemAvatarFileChecker checker = new FileSystemAvatarFileChecker();

    @Test
    void existingReadableFileIsTrue() throws IOException {
        Path file = directory.resolve("avatar.png");
        Files.writeString(file, "fake image bytes");

        assertTrue(checker.isReadableFile(file.toString()));
    }

    @Test
    void missingFileIsFalse() {
        Path file = directory.resolve("missing.png");

        assertFalse(checker.isReadableFile(file.toString()));
    }

    //BR: a directory is not a readable avatar file, even though it exists
    @Test
    void directoryIsFalse() {
        assertFalse(checker.isReadableFile(directory.toString()));
    }

    //BR: no path means no avatar to check, not an error
    @Test
    void blankPathIsFalse() {
        assertFalse(checker.isReadableFile(""));
        assertFalse(checker.isReadableFile("   "));
    }
}

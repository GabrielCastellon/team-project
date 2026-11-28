package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileAvatarImageLoaderTest {

    @TempDir
    Path directory;

    private final FileAvatarImageLoader loader = new FileAvatarImageLoader();

    @Test
    void loadsAPngFromDisk() throws IOException {
        Path png = directory.resolve("me.png");
        ImageIO.write(new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB), "png", png.toFile());

        BufferedImage image = loader.load(png.toString());

        assertEquals(40, image.getWidth());
        assertEquals(20, image.getHeight());
    }

    @Test
    void missingFileThrowsIOException() {
        assertThrows(IOException.class, () -> loader.load(directory.resolve("gone.png").toString()));
    }

    @Test
    void fileThatIsNotAnImageThrowsIOException() throws IOException {
        Path fake = directory.resolve("fake.png");
        Files.writeString(fake, "not an image");

        assertThrows(IOException.class, () -> loader.load(fake.toString()));
    }
}

package data_access;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Reads avatar images from the local disk. Passed to the view as {@code avatarImages::load}
 * (like Kevin's ImageLoader), so data_access never depends on interface_adapter.
 */
public final class FileAvatarImageLoader {

    public BufferedImage load(String path) throws IOException {
        // ImageIO returns null (not an exception) for a file that isn't an image it understands
        BufferedImage image = ImageIO.read(new File(path));
        if (image == null) {
            throw new IOException("Not a readable image: " + path);
        }
        return image;
    }
}

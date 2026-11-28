package interface_adapter.profile;

import java.awt.image.BufferedImage;
import java.io.IOException;

//the view asks this for the picture instead of reading files itself; ProfileModule supplies the real one
/** Reads the image stored at an avatar path so the view can show the profile picture. */
@FunctionalInterface
public interface AvatarImageLoader {
    /** Returns the image at {@code path}, or throws if it's missing or not a readable image. */
    BufferedImage load(String path) throws IOException;
}

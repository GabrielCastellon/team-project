package use_case.view_pins;

import entity.Pin;
import java.util.List;

public interface ViewPinsDataAccessInterface {
    List<Pin> findByOwnerId(String ownerId);
}

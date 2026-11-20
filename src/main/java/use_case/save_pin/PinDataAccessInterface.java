package use_case.save_pin;

import entity.Pin;

public interface PinDataAccessInterface {
    //BR4: duplicate pin
    boolean existsByOwnerAndPhotoId(String ownerId, String photoId);

    //BR7: store newly created pin before success
    void save(Pin pin);
}

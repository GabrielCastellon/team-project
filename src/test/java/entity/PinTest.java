package entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


public class PinTest {
    //tests two behaviors of pin identity: personal annotations upon pin creation is "" (empty);
    //original source photo description still available through Photo object
    @Test
    void PinDescription() {
        Photo catPhoto = new Photo("123", "Orange Cat",
                "https://cat.com/images.cat.jpg", "https://cat.com",
                "Lebron", "https://lebron_James.com",
                "https://lebron_download/cat");
        Pin catPin = new Pin("321", "444", catPhoto);
        assertEquals("", catPin.getDescription());
        assertEquals("Orange Cat", catPin.getPhoto().description());
    }
}

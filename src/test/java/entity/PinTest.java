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

    //BR1: Expect nullPointerException when trying to create a pin with a null owner
    @Test
    void rejectNullOwnerId(){
        Photo catPhoto = new Photo("123", "Orange Cat",
                "https://cat.com/images.cat.jpg", "https://cat.com",
                "Lebron", "https://lebron_James.com",
                "https://lebron_download/cat");
        assertThrows(NullPointerException.class,
                () -> new Pin("32", null, catPhoto));
    }

    @Test
    //BR6: Tests Pin construction when source photo metadata is missing
    void rejectNullPhoto(){
        assertThrows(NullPointerException.class,
                () -> new Pin("321", "443", null));
    }

    @Test
    //BR3: Test Pin construction when Pin Id supplied is null
    void rejectNullPinId(){
        Photo catPhoto = new Photo("123", "Orange Cat",
                "https://cat.com/images.cat.jpg", "https://cat.com",
                "Lebron", "https://lebron_James.com",
                "https://lebron_download/cat");
        assertThrows(NullPointerException.class, () -> new Pin(null,"323", catPhoto));
    }
}



package data_access;

import com.google.gson.*;
import entity.Pin;
import use_case.save_pin.PinDataAccessInterface;
import use_case.view_pins.ViewPinsDataAccessInterface;
import java.nio.file.Path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;


public class JsonPinDataAccessObject implements PinDataAccessInterface, ViewPinsDataAccessInterface{
    private final Path file;
    private final Gson gson = new Gson();

    public JsonPinDataAccessObject(Path file) {
        this.file = file;
    }

    //Part 2A: DAO checking if photo already exists in storage
    @Override
    public boolean existsByOwnerAndPhotoId(String ownerId, String photoId) {
        JsonArray pins = readPins();

        for (JsonElement element: pins) { //loop over JSON array reading nested object
            JsonObject pin = element.getAsJsonObject();
            String savedOwnerId = pin.get("ownerId").getAsString();

            JsonObject photo = pin.getAsJsonObject("photo");
            String savedPhotoId = photo.get("id").getAsString();

            //checking if selected photo matches record in JSON storage
            if (ownerId.equals(savedOwnerId) && photoId.equals(savedPhotoId)) {
                return true;
            }
        }
        return false; //otherwise, doesn't exist in memory and proceed with save next steps
    }

    //part 2C: DAO for save(pin) implementation; makes pin available next time program opens file
    @Override
    public void save(Pin pin) {
        JsonArray pins = readPins(); // references array that is being added to
        JsonElement pinJson = gson.toJsonTree(pin); //Gson builds JSON values from pin object
        pins.add(pinJson);

        String json = pins.toString(); //produces text to save
        try {
            Files.writeString(file, json); //saves to disk
        } catch (IOException ex){ //4B persistence failure
            throw new IllegalStateException("Could not save pin", ex);
        }
    }

    @Override
    public List<Pin> findByOwnerId(String ownerId){
        List<Pin> match = new ArrayList<>();

        for (JsonElement element: readPins()){
            JsonObject storedPin = element.getAsJsonObject();
            if(ownerId.equals(storedPin.get("ownerId").getAsString())) {
                match.add(gson.fromJson(storedPin, Pin.class));
            }
        }
        return match;
    }

    /* part 2C storage setup: storage should return [] for first save attempt. Both the Save
    duplicate check and Pins tab call readPins()
     */
    private JsonArray readPins() {
        try {
            String json = Files.readString(file);
            JsonArray pins = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement element: pins) {
                /*JsonElement might represent a JSON object, array, number, string, boolean or null.
                So this checks object's runtime type before subclass functionality
                 */
                if (!(element instanceof JsonObject pin) ||
                    !hasString(pin, "pinId") ||
                    pin.get("pinId").getAsString().isBlank() ||
                    !hasString(pin, "ownerId") ||
                    !(pin.get("photo") instanceof JsonObject photo) ||
                    !hasString(photo, "id")) {
                    throw new IllegalStateException("Saved pin is " +
                            "missing a valid owner or photo ID");
                }
            }
            return pins;
        } catch (NoSuchFileException ex) { // First save: pins.json does not exist yet
            return new JsonArray();
        } //when existing file is damaged/unreadable go to 4B
        catch (IOException | JsonParseException | IllegalStateException ex) {
            throw new IllegalStateException("Could not read saved pins", ex);
        }
    }

    //helper that checks if JSON object contains a field thats a JSON string
    private static boolean hasString(JsonObject object, String field){
        return object.get(field) instanceof JsonPrimitive value && value.isString();
    }
}

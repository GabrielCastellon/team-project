# Alex's pin management use cases

## Problem statement and user story

## Scope and status

Status: Design stage/not implemented
MVP use case: Save Pins
Planned use cases: View saved pins, edit pin details, seach/filter saved pins

Save Pins (Issue #) - 11/14/2026

- User selects an image in Discover and saves it to their personal collection
- Discover → Save pin → Persist → Confirm
- Saved image is retrievable, including after program restart
- Saved image must preserve original image metadata, require no annotations, and prevent duplicates
  for the same user and source
- Assigning a saved pin to a board, annotation editing, filtering are planned steps

## Demo workflow (MVP1)

1. A Photofy user accesses interface to discover images using keyword "Cat"
2. The user clicks 'save' on a discovered photo
3. Photofy checks if the user had already saved that source photo
4. If the user has already saved the selected source photograph,
   Photofy informs them that it is already saved and does not create a duplicate Pin
5. Otherwise, Photofy creates a saved pin that contains the original source metadata and associates
   the saved pin with the user
6. Saved pin is stored so that it can be accessed later
7. Photofy confirms that the image was successfully saved and updates the interface to reflect the
   saved status

## Use Case: Save Pin

Primary Actor: Photofy User 

Goal: Save a discovered photograph in the user's personal collection so that it can be retrieved 
at a later time 

Preconditions: 
- The user is identified as a Photofy user 
- The user has discovered a photograph available to save 

Trigger: The user presses a save button/icon, triggering request to save a selected photo 

Success Outcome: The photo is successfully created and persisted as a pin associated
with the user. The pin contains original source metadata. The user receives confirmation that the 
photo was saved successfully. 

### Main success scenario: 
1. User requests to save a discovered photo 
2. Photofy checks if the user had already saved this photo from this particular source before
3. Photofy creates a new Pin object associated with the user that contains original source 
metadata, and doesn't require the user to provide annotations 
4. Photofy stores the Pin in persistent storage 
5. Photofy displays confirmation and updated discovery interface that shows saved status of photo

Extensions:

2a.The user already saved the photo 
- Photofy informs the user that the photo is already saved 
- The existing pin remains unchanged and no duplicate is created 

4a. Photofy cannot persist the pin 
- Photofy reports that saving was unsuccessful 
- The photo is not confirmed saved 
- The user may attempt to save the photo again

## Enterprise/Core Business rules (BR)

### Pin identity 
1. Each saved pin belongs to one user 
2. Different users may save the same photograph/source independently 
3. Saved pins should have distinct ID from source photo 
4. A user cannot have two saved pins representing the same source/photo 

### Persistence
5. Saving a photo does not require personal annotations 
6. A successful save must contain original source metadata 
7. A pin is successfully saved when it has been persistently stored 
8. Repeatedly saving a photo that is already saved does not create duplicate pins 

### Personalize 
9. Users may customize title, description and tags associated with a pin 
10. Editing a pin must not overwrite original source photo or another user's pin

### Board relationship 
11. A saved pin may exist without a board 
12. A saved pin may belong to multiple boards 
13. An image that is added to board must be a saved pin 
14. Removing a pin from a board does not delete the pin from the user's saved collection 
15. Personal annotations belong to a pin, and not associated with a particular board 

### Feature boundaries 
16. Saving a pin is different from adding an image to recommendation preferences (Kevin)
17. Board organization is different from pin creation and personalization 

## Noun–verb analysis

| Nouns — candidate concepts | Verbs — candidate responsibilities |
|---|---|
| User | Saves |
| Photograph | Retains |
| Pin | Retrieves |
| Personal collection | Personalizes |
| Source information | — |


## Clean Architecture Design 

### Enterprise vs Application Business Rules

An individual `Pin` entity represents a user's saved photo, containing ID, ownerID, source photo, 
and personal annotations (BR-1, BR-3, BR-6, BR-9, BR-10). 

`SavePin` use case manages the process of saving a photo with the following application business 
rules: 
- **ABR-1 (BR-4, BR-8):** Check whether user already
  saved the source photo. Do not create a duplicate
- **ABR-2 (BR-2, BR-3):** If not previously saved,
  create a distinct user-owned Pin
- **ABR-3 (BR-5, BR-6):** Store source metadata
  without requiring personal annotations
- **ABR-4 (BR-7):** Request persistent storage and
  report success only after storage succeeds
- **ABR-5 (BR-16):** Saving a Pin does not automatically
  update recommendation preferences



### Responsibility Allocation

| Responsibility | Component | Reason |
|---|---|---|
| Represent a valid Pin and its attributes | `Pin` entity | Concerns an individual Pin |
| Check whether a user already saved a photograph | `SavePinInteractor`|Requires examining existing Pins (BR-4, BR-8) |
| Retrieve and store Pins | `PinRepository` interface | Abstracts persistence operations required by the use case |
| Read and write JSON files | `JsonPinDataAccessObject` | Concrete persistence implementation |

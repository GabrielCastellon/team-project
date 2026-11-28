# Alex's pin management use cases

## Problem statement and user story

## Scope and status

Saving photos from Discovery and viewing pins are implemented on main. A signed-in user can save a photo from Discovery. 
After logging out and signing back in, (or restarting app), saved pins are preserved in the user's collection in the Pins tab. 
Each saved photo appears with its thumbnail, caption and photographer credit. Switching accounts would show the new user's 
saved collection. 

- Save from Discovery and view the signed-in user's pins (#13, #32)
- Display thumbnails on saved pin cards (#36, #37)
- Planned use cases: edit pin details, seach/filter saved pins

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
8. User opens the Pins tab to view their personal collection. The saved cat photo appears as a thumbnail
   with a caption and photographer credit. After the duplicate save attempt, the cat still appears only once.
9. Switching to a second account that never saved the cat photo would not display the photo in the Pins tab.

### Demo screenshots

<p>
  <a href="https://github.com/user-attachments/assets/5914cace-efb3-482b-b588-73c7cbc866ee"><img src="https://github.com/user-attachments/assets/5914cace-efb3-482b-b588-73c7cbc866ee" width="220" alt="Demo screenshot 1"></a>
  <a href="https://github.com/user-attachments/assets/ea2ccd68-3e60-4e93-90a9-0348f774024a"><img src="https://github.com/user-attachments/assets/ea2ccd68-3e60-4e93-90a9-0348f774024a" width="220" alt="Demo screenshot 2"></a>
  <a href="https://github.com/user-attachments/assets/bf4a804e-0e02-4ec4-a2bb-608e2281be46"><img src="https://github.com/user-attachments/assets/bf4a804e-0e02-4ec4-a2bb-608e2281be46" width="220" alt="Demo screenshot 3"></a>
</p>

<p>
  <a href="https://github.com/user-attachments/assets/8892f2b2-0c4b-4688-bfbe-2659885b2fc9"><img src="https://github.com/user-attachments/assets/8892f2b2-0c4b-4688-bfbe-2659885b2fc9" width="220" alt="Demo screenshot 4"></a>
  <a href="https://github.com/user-attachments/assets/19f8a508-21b2-450e-84e2-e93da4588a9e"><img src="https://github.com/user-attachments/assets/19f8a508-21b2-450e-84e2-e93da4588a9e" width="220" alt="Demo screenshot 5"></a>
  <a href="https://github.com/user-attachments/assets/f5034581-311b-4611-9746-8908a4dd06dc"><img src="https://github.com/user-attachments/assets/f5034581-311b-4611-9746-8908a4dd06dc" width="220" alt="Demo screenshot 6"></a>
</p>

<p>
  <a href="https://github.com/user-attachments/assets/ec41fc78-8781-4b97-9793-28fee30455df"><img src="https://github.com/user-attachments/assets/ec41fc78-8781-4b97-9793-28fee30455df" width="220" alt="Demo screenshot 7"></a>
</p>

## Use Case: Save Pin
**Status:** Implemented. A signed-in user can save a photo from Discovery and view it in Pins. [PR #37](https://github.com/GabrielCastellon/team-project/pull/37) added thumbnails to the Pins tab.

Primary Actor: Photofy User 

Goal: Save a discovered photograph in the user's personal collection so that it can be retrieved 
at a later time 

Preconditions: 
- The user is signed in to Photofy
- The Discovery tab displays the photo the user wants to save

Trigger: The user presses a save button/icon, triggering request to save a selected photo 

Success Outcome: The photo is successfully created and persisted as a pin associated
with the user. The pin contains original source metadata. The user receives confirmation that the 
photo was saved successfully. 

### Main success scenario: 
1. The user selects **Save** on a photo in Discovery
2. Photofy checks if the user had already saved this photo from this particular source before
3. Photofy creates a new Pin object associated with the user that contains original source 
metadata, and doesn't require the user to provide annotations 
4. Photofy stores the Pin in persistent storage 
5. Photofy displays confirmation and updated discovery interface that shows saved status of photo

### Alternative Flows

**2a. The user has already saved this photo.**
- Photofy informs the user that the photo is already saved 
- The existing pin remains unchanged and no duplicate is created 

**4a. Photofy cannot store the pin.**
- Photofy reports that saving was unsuccessful 
- The photo is not confirmed saved 
- The user may attempt to save the photo again

## Enterprise/Core Business rules (BR)

### Pin identity 
1. Each saved pin belongs to one user 
2. Different users may save the same photograph/source independently 
3. Each saved pin has its own ID, separate from the source photo’s ID
4. Each user can have at most one pin for a given source photo

### Saving a pin
5. Saving a photo does not require personal annotations 
6. A successful save must contain original source metadata 
7. A pin is successfully saved when it has been persistently stored 
8. A repeat save preserves the existing pin and its ID

### Board relationship 
9. A saved pin may exist without a board 
10. A saved pin may belong to multiple boards 
11. An image that is added to board must be a saved pin 
12. Removing a pin from a board does not delete the pin from the user's saved collection 
13. Personal annotations belong to a pin, and not associated with a particular board 

### Feature boundaries 
14. Saving a pin is different from adding an image to recommendation preferences
15. Board organization is different from pin creation and personalization 

## Noun–verb analysis

| Candidate concept | Role in Save Pin |
|---|---|
| User | Requests to save a discovered photo and later opens Pins to view it. |
| Photo | Provides the source photo ID, caption, and photographer credit. |
| Pin | Associates the saved photo with the user who saved it. |
| Personal collection | Groups the user’s saved pins for later viewing. |


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

# Pin Management Design

## Figure 1. Save Pin core

![Save Pin class diagram showing the interactor and its input, output, and storage interfaces](./save-pin-class-diagram.svg)

`SavePinInteractor` uses `PinDataAccessInterface` to check `existsByOwnerAndPhotoId(...)` before calling `save(pin)`. `JsonPinDataAccessObject` implements that interface, so the interactor can save a pin without depending on JSON storage.
The interactor reports success, a duplicate, or a failure through `SavePinOutputBoundary`, which `SavePinPresenter` implements. On a first save, the interactor reports success only after `save(pin)` returns successfully.
## Figure 2. Save Pin in Discovery

![Class diagram showing the Save callback and how Discovery observes the save result](./save-pin-in-discovery.svg)

The Save button invokes `SaveAction.save(photo)`. `DiscoveryModule` supplies the callback that checks the current user. When the user is signed in, the callback passes `user.getId()` and the selected photo to `SavePinController`.

**Observer pattern.** `SavePinViewModel` is the subject, and `DiscoveryView` registers itself as an observer with `addListener(this)`. When `SavePinPresenter` calls `setState(...)`, the view model calls `stateChanged()` on each listener. `DiscoveryView` then calls `getState()` to read the result. The view model depends only on `SavePinViewModelListener` to notify observers.

## Figure 3. View Pins

![View Pins class diagram showing how a user's saved pins reach the Pins tab](./view-pins-class-diagram.svg)

When the Pins tab becomes visible, a listener configured by `ApplicationFactory` reads the current user. If the user is signed in, it passes `user.getId()` to `ViewPinsController`. Reopening the tab after switching accounts therefore triggers a lookup for the newly signed-in user.

**Loading saved pins.** `ViewPinsInteractor` calls `findByOwnerId(...)` through `ViewPinsDataAccessInterface`. `JsonPinDataAccessObject` reads the saved pins for that owner from the JSON file. The interactor reads each pin’s `Photo` through `getPhoto()` and creates a `ViewPinItem`. The presenter converts those items into cards and calls `showPins(...)` to display them.

## Figure 4. Loading thumbnails in Pins

![PinsView uses a thumbnail loader and a Swing task runner](./pins-thumbnail-loading-cropped.svg)

`PinsView` displays the pin cards immediately and uses `SwingTaskRunner` to load and scale thumbnails in the background. It tracks in-flight image URLs so reopening the tab can reuse a pending download. When a load finishes, the view checks whether a visible card still uses that URL before repainting.

## Figure 5. Save Pin request (Part 1)

A signed-in user clicks **Save** on a photo in Discovery. `DiscoveryModule` supplies the callback that reads the current user and passes their ID and the selected photo to `SavePinController`.

```mermaid
sequenceDiagram
    actor User
    participant V as DiscoveryView
    participant A as SaveAction callback
    participant S as SessionManager
    participant C as SavePinController
    participant I as SavePinInteractor

    User->>V: Click Save on a photo
    V->>A: save(photo)
    A->>O: get()
    O->>S: getCurrentUser()
    S-->>O: Signed-in user
    O-->>A: ownerId
    A->>C: execute(ownerId, photo)
    Note over C: Creates SavePinInputData(ownerId, photo)
    C->>I: execute(inputData) via SavePinInputBoundary
```

## Figure 6. Saving a pin for the first time (Parts 2A–2D)

The interactor reads the owner ID and the selected photo’s ID from `inputData`. In this scenario, the owner has never saved this photo.

```mermaid
sequenceDiagram
    participant I as SavePinInteractor
    participant D as JsonPinDataAccessObject
    participant P as SavePinPresenter

    Note over I,D: 2A. Check for a duplicate
    I->>D: existsByOwnerAndPhotoId(ownerId, photoId)
    D-->>I: false

    Note over I: 2B. Generate a pinId and create a Pin<br/>with that ID, the ownerId, and the selected Photo

    Note over I,D: 2C. Save the new pin
    I->>D: save(pin)
    D-->>I: Write completes

    Note over I: 2D. Create SavePinOutputData(pinId, photoId)
    I->>P: prepareSuccessView(outputData)
```

The interactor calls the DAO through `PinDataAccessInterface` and the presenter through `SavePinOutputBoundary`. It reports success only after `save(pin)` returns.


## Figure 7. Discovery displays “Saved” (Part 3)

After the pin is saved, `SavePinPresenter` creates the state that Discovery will display. `DiscoveryView` registered itself earlier as a `SavePinViewModelListener` with `addListener(this)`.

```mermaid
sequenceDiagram
    participant I as SavePinInteractor
    participant P as SavePinPresenter
    participant M as SavePinViewModel
    participant V as DiscoveryView

    I->>P: prepareSuccessView(outputData)
    Note over P: Creates SavePinState with photoId, pinId,<br/>saved = true, and message = "Saved"
    P->>M: setState(state)
    Note over M: Stores the new state before notifying listeners
    M->>V: stateChanged()
    V->>M: getState()
    M-->>V: Current SavePinState
    V->>V: Display "Saved" in the status message
    V->>V: Find the card by photoId and set its label to "Saved"
```

This is the Observer pattern. `stateChanged()` takes no arguments. It tells Discovery when to read the view model. `getState()` supplies the message to display and the photo ID that identifies which card to update.

## Figure 8. Discovery reports an already-saved photo (Part 4A)

The signed-in user clicks **Save** on a photo they have already saved. The request reaches the interactor as in Figure 5.

```mermaid
sequenceDiagram
    participant I as SavePinInteractor
    participant D as JsonPinDataAccessObject
    participant P as SavePinPresenter
    participant M as SavePinViewModel
    participant V as DiscoveryView

    I->>D: existsByOwnerAndPhotoId(ownerId, photoId)
    D-->>I: true
    I->>P: prepareAlreadySavedView(photoId)
    Note over P: Creates SavePinState with photoId, pinId = null,<br/>saved = true, and message = "Already saved"
    P->>M: setState(state)
    M->>V: stateChanged()
    V->>M: getState()
    M-->>V: Current SavePinState
    V->>V: Display "Already saved" in the status message
    opt The matching photo card is on screen
        V->>V: Set its label to "Saved"
    end
```

The duplicate lookup returns only a boolean, so the presenter receives the photo ID but no existing pin ID. Discovery uses the photo ID to update the matching card. The interactor returns without creating another `Pin` or calling `save(pin)`.


## Figure 9. Saving the new pin fails (Part 4B)

The duplicate check at step 2A returns `false`. The interactor creates a `Pin` at 2B, but the attempt to write it to storage fails at 2C.

```mermaid
sequenceDiagram
    participant I as SavePinInteractor
    participant D as JsonPinDataAccessObject
    participant P as SavePinPresenter
    participant M as SavePinViewModel
    participant V as DiscoveryView

    I->>D: existsByOwnerAndPhotoId(ownerId, photoId)
    D-->>I: false
    Note over I: 2B. Generates a pin ID and creates the Pin
    I->>D: 2C. save(pin)
    D-->>I: throws IllegalStateException
    I->>P: prepareFailView(message)
    Note over P: Creates SavePinState with saved = false<br/>and the error message
    P->>M: setState(state)
    M->>V: stateChanged()
    V->>M: getState()
    M-->>V: Current SavePinState
    V->>V: Display the error in the status message
```

Discovery displays **“Could not save the pin. Please try again.”** The `Pin` exists in memory, but the failed write prevents the interactor from reaching step 2D. Since the failure state has `saved = false`, Discovery leaves the card’s label unchanged.

## Figure 10. The duplicate check fails (step 2A)

The user clicks **Save**, but the storage lookup fails before the interactor can determine whether this owner already saved the photo.

```mermaid
sequenceDiagram
    participant I as SavePinInteractor
    participant D as JsonPinDataAccessObject
    participant P as SavePinPresenter
    participant M as SavePinViewModel
    participant V as DiscoveryView

    I->>D: 2A. existsByOwnerAndPhotoId(ownerId, photoId)
    D-->>I: throws IllegalStateException
    I->>P: prepareFailView(message)
    Note over P: Creates SavePinState with saved = false<br/>and the lookup error message
    P->>M: setState(state)
    M->>V: stateChanged()
    V->>M: getState()
    M-->>V: Current SavePinState
    V->>V: Display the lookup error in the status message
```

Discovery displays **“Could not check whether the photo is already saved. Please try again”** and leaves the card’s label unchanged. The interactor returns at 2A, before it creates a `Pin` at 2B or calls `save(pin)` at 2C.

## Figure 11. Viewing saved pins after restarting Photofy

**Precondition.** The user previously saved a pin, restarted Photofy, and signed back into the same account.

```mermaid
sequenceDiagram
    actor User
    participant V as PinsView
    participant L as Tab visibility listener
    participant S as SessionManager
    participant C as ViewPinsController
    participant I as ViewPinsInteractor
    participant D as JsonPinDataAccessObject
    participant P as ViewPinsPresenter

    User->>V: Open Pins
    V-->>L: Swing signals that the tab is visible
    L->>S: getCurrentUser()
    S-->>L: Signed-in user
    L->>C: execute(user.getId())
    C->>I: execute(ownerId)
    I->>D: findByOwnerId(ownerId)
    Note over D: Reads the JSON file and selects pins for this owner
    D-->>I: List of matching Pin objects
    Note over I: Reads each pin's Photo and creates a ViewPinItem
    I->>P: presentPins(items)
    Note over P: Converts each ViewPinItem into a PinCard
    P->>V: showPins(cards)
    V-->>User: Display the saved collection
```

`ApplicationFactory` registers the visibility listener when it builds the Pins panel. Each time the tab becomes visible, the listener reads the current user. After a restart, that user's ID lets the DAO retrieve the pin written in Figure 6 from the JSON file.

# Profile customization (User Story 6): design and UML

Owner: Kieran Murphy. User story 6: *As a user, I want to customize my profile and appearance settings so that I can personalize my experience.*

This document covers the two implemented use cases, **View Profile** (load and show the stored profile) and **Edit Profile** (the MVP use case). The stretch use case **Change Appearance** (theme, font size) is not implemented yet. `AppearanceSettings` already exists on the entity and is carried forward unchanged by every edit.

## What each diagram covers

The **class diagram** shows every application type in the feature, grouped by Clean Architecture layer, and the source-code dependencies between them. The **sequence diagram** shows one interaction, Save in the edit form, in time order, with an `alt` block for the success and validation-failure branches. JDK Swing internals and Gson are omitted for readability.

## 1. Edit Profile use case

**Actor:** a user (until Matthew's login exists, the fixed placeholder user `demo-user`, see [Integration](#5-integration-and-running)).
**Precondition:** the Profile tab is open (View Profile has loaded the stored profile, or a default one).

### Main flow

1. The user opens the Profile tab. The system shows the stored display name, biography and avatar file name.
2. The user clicks **Edit Profile**.
3. The system shows a form pre-filled with the current display name, biography and avatar path.
4. The user edits any of the fields, optionally choosing an avatar with **Browse…** (only `.png`, `.jpg` and `.jpeg` files are offered).
5. The user clicks **Save**. The system validates the values.
6. The system saves the profile and returns to display mode, showing the updated profile and the message **"Profile updated"**.

### Validation rules (checked in `EditProfileInteractor`)

| Field | Rule | Error shown under the field |
|---|---|---|
| Display name | required, trimmed, at most 30 characters | "Display name is required." / "Display name must be 30 characters or fewer." |
| Biography | at most 300 characters, may be empty | "Biography must be 300 characters or fewer." |
| Avatar | empty, or an existing readable file ending in `.png`, `.jpg` or `.jpeg` | "Avatar must be a .png, .jpg, or .jpeg file." / "Avatar file could not be found." |

### Alternate flows

- **1a. First-time user (no stored profile):** the system shows a default profile (display name "New user", empty biography, no avatar). The first save creates the stored profile, nothing errors.
- **1b. Stored profile can't be read:** the system shows "Couldn't load your profile, try again".
- **4a. Cancel:** the user clicks **Cancel**. The edits are discarded, nothing is saved, and the view returns to the stored values.
- **5a. Invalid display name:** the error appears next to the name field. The form stays open with all of the user's edits. Nothing is saved.
- **5b. Biography too long:** the error appears next to the biography. Nothing is saved.
- **5c. Bad avatar (wrong extension or missing file):** the error appears next to the avatar field. The old avatar is kept. Nothing is saved.
- **6a. Save fails (`IOException`):** the system shows "Couldn't save your changes, try again". The form stays filled so the user can retry.

**Postconditions:** on success, the stored profile equals the new values. On any failure, the stored profile is unchanged.

## 2. Class diagram (all layers)

```mermaid
classDiagram
    direction LR

    namespace view {
        class ProfileView {
            +ProfileView(ViewProfileController, EditProfileController, ProfileViewModel)
            +stateChanged()
            -render(ProfileState state)
            -startEditing()
            -cancelEditing()
            -save()
            -chooseAvatar()
        }
    }

    namespace interface_adapter {
        class ViewProfileController {
            -String userId
            -Executor executor
            +execute()
        }
        class EditProfileController {
            -String userId
            -Executor executor
            +execute(String displayName, String biography, String avatarPath)
        }
        class ViewProfilePresenter
        class EditProfilePresenter {
            +prepareValidationFailView(ProfileField field, String message)
        }
        class ProfileViewModel {
            -ProfileState state
            +getState() ProfileState
            +setState(ProfileState state)
            +addListener(ProfileViewModelListener listener)
        }
        class ProfileViewModelListener {
            <<interface>>
            +stateChanged()
        }
        class ProfileState {
            -String displayName
            -String biography
            -String avatarPath
            -String nameError
            -String bioError
            -String avatarError
            -String statusMessage
            -boolean saved
        }
    }

    namespace use_case {
        class ViewProfileInputBoundary {
            <<interface>>
            +execute(ViewProfileInputData inputData)
        }
        class ViewProfileInteractor
        class ViewProfileOutputBoundary {
            <<interface>>
            +prepareSuccessView(ViewProfileOutputData outputData)
            +prepareFailView(String message)
        }
        class ViewProfileDataAccessInterface {
            <<interface>>
            +findProfile(String userId) Optional~UserProfile~
        }
        class EditProfileInputBoundary {
            <<interface>>
            +execute(EditProfileInputData inputData)
        }
        class EditProfileInteractor
        class EditProfileOutputBoundary {
            <<interface>>
            +prepareSuccessView(EditProfileOutputData outputData)
            +prepareValidationFailView(ProfileField field, String message)
            +prepareFailView(String message)
        }
        class ProfileDataAccessInterface {
            <<interface>>
            +findProfile(String userId) Optional~UserProfile~
            +saveProfile(UserProfile profile)
        }
        class AvatarFileChecker {
            <<interface>>
            +isReadableFile(String path) boolean
        }
        class ProfileField {
            <<enumeration>>
            DISPLAY_NAME
            BIOGRAPHY
            AVATAR
        }
    }

    namespace entity {
        class UserProfile {
            -String userId
            -String displayName
            -String biography
            -String avatarPath
            -AppearanceSettings appearance
            +defaultFor(String userId) UserProfile$
        }
        class AppearanceSettings {
            -Theme theme
            -String fontFamily
            -int fontSize
            -boolean highContrast
            +defaults() AppearanceSettings$
        }
        class Theme {
            <<enumeration>>
            LIGHT
            DARK
        }
    }

    namespace data_access {
        class JsonProfileDataAccessObject {
            -Path file
        }
        class FileSystemAvatarFileChecker
    }

    namespace app {
        class ProfileModule {
            ~String USER_ID$
            ~Path PROFILE_FILE$
            +createPanel() JPanel$
        }
    }

    ProfileView ..|> ProfileViewModelListener
    ProfileView --> ViewProfileController : load on open
    ProfileView --> EditProfileController : Save
    ProfileView --> ProfileViewModel : reads state
    ProfileViewModel --> ProfileViewModelListener : notifies
    ProfileViewModel --> ProfileState

    ViewProfileController --> ViewProfileInputBoundary
    ViewProfileInteractor ..|> ViewProfileInputBoundary
    ViewProfileInteractor --> ViewProfileDataAccessInterface
    ViewProfileInteractor --> ViewProfileOutputBoundary
    ViewProfilePresenter ..|> ViewProfileOutputBoundary
    ViewProfilePresenter --> ProfileViewModel

    EditProfileController --> EditProfileInputBoundary
    EditProfileInteractor ..|> EditProfileInputBoundary
    EditProfileInteractor --> ProfileDataAccessInterface
    EditProfileInteractor --> AvatarFileChecker
    EditProfileInteractor --> EditProfileOutputBoundary
    EditProfilePresenter ..|> EditProfileOutputBoundary
    EditProfilePresenter --> ProfileViewModel
    EditProfileOutputBoundary ..> ProfileField

    ViewProfileInteractor ..> UserProfile
    EditProfileInteractor ..> UserProfile
    UserProfile *-- AppearanceSettings
    AppearanceSettings --> Theme

    JsonProfileDataAccessObject ..|> ProfileDataAccessInterface
    JsonProfileDataAccessObject ..|> ViewProfileDataAccessInterface
    FileSystemAvatarFileChecker ..|> AvatarFileChecker

    ProfileModule ..> ProfileView : constructs
    ProfileModule ..> JsonProfileDataAccessObject : constructs
    ProfileModule ..> FileSystemAvatarFileChecker : constructs
    ProfileModule ..> ViewProfileInteractor : constructs
    ProfileModule ..> EditProfileInteractor : constructs
```

Input data and output data classes (`ViewProfileInputData`, `ViewProfileOutputData`, `EditProfileInputData`, `EditProfileOutputData`) are omitted as boxes to keep the diagram readable. They are plain carriers of the fields named in the boundary methods. Every arrow points inward or across a boundary interface: the view knows only the controllers and the view model, the interactors know only interfaces and entities, and only `ProfileModule` knows the concrete classes.

## 3. Save sequence

Precondition: the user is in edit mode. Everything up to the background-thread note runs on Swing's event dispatch thread (EDT). The controller hands the interactor call to the single `profile-io` executor thread, the view switches back to the EDT with `SwingUtilities.invokeLater` before touching any component.

```mermaid
sequenceDiagram
    actor User
    participant V as view:ProfileView
    participant C as controller:EditProfileController
    participant I as interactor:EditProfileInteractor
    participant D as dao:JsonProfileDataAccessObject
    participant F as checker:FileSystemAvatarFileChecker
    participant P as presenter:EditProfilePresenter
    participant M as viewModel:ProfileViewModel

    User->>V: Click Save
    V->>V: Disable Save, status Saving
    V->>C: execute(name, bio, avatarPath)
    C->>C: Build EditProfileInputData with userId
    Note over C,M: executor.execute runs the rest on the profile-io thread
    C->>I: execute(inputData) through EditProfileInputBoundary
    I->>D: findProfile(userId)
    D-->>I: stored profile, or empty for a first-time user
    alt All fields valid
        I->>F: isReadableFile(avatarPath) if an avatar is set
        F-->>I: true
        I->>D: saveProfile(updated profile)
        D->>D: Write temp file, then atomic move over profiles.json
        I->>P: prepareSuccessView(outputData)
        P->>M: setState(new values, no errors, Profile updated, saved=true)
        M->>V: stateChanged()
        V->>V: invokeLater(render): show display mode with new values
    else A field fails validation
        I->>P: prepareValidationFailView(field, message)
        Note over I,D: saveProfile is never called, stored profile unchanged
        P->>M: setState(stored values, error on that one field, saved=false)
        M->>V: stateChanged()
        V->>V: invokeLater(render): stay in edit mode, form keeps the user's edits
    end
```

The save-failure branch (6a) follows the success branch up to `saveProfile`, which throws `IOException`. The interactor then calls `prepareFailView("Couldn't save your changes, try again")` and the view stays in edit mode exactly as in the validation branch. View Profile follows the same path with `ViewProfileController`, `ViewProfileInteractor` and `ViewProfilePresenter`, minus validation and saving.

## 4. Design decisions

- **Validation lives in the interactor, not the entity.** Entity constructors only do non-null checks, trimming and safe defaults. Rules that the user should see as a message next to a field (required, maximum length, avatar format and existence) are application rules, so `EditProfileInteractor` checks them and reports a `ProfileField`. `AppearanceSettings` does keep its font-size bound (10 to 24), because that is a rendering invariant rather than a form error.
- **Two data-access interfaces, one DAO (Interface Segregation).** View Profile depends on the read-only `ViewProfileDataAccessInterface`, Edit Profile on `ProfileDataAccessInterface` (read and save). `JsonProfileDataAccessObject` implements both with one `findProfile` method. Neither use case package depends on the other.
- **`AvatarFileChecker` (Dependency Inversion).** The interactor asks an interface whether the avatar file exists, so its tests use an in-memory fake and never touch the real filesystem. `FileSystemAvatarFileChecker` is the real implementation in `data_access`.
- **`ProfileState` holds the stored values.** The name, biography and avatar in the state are always what is saved. The edit form's text belongs to the view. That is what makes Cancel (4a) restore the stored values and what lets a failed save keep the user's edits on screen.
- **The `saved` flag.** After Save, the view must decide whether to leave edit mode. A successful save and a failed save both have no field errors, so the presenter sets `saved=true` only on success instead of the view comparing message text.
- **Threading.** Controllers run the interactor on an `Executor` because `interface_adapter` may not depend on Swing (enforced by `DependencyRuleTest`). The executor is a single daemon thread: a load and a save never overlap, and the thread never keeps the app running after the window closes. The view model's state is `volatile` and its listener list is a `CopyOnWriteArrayList` because presenters update them from that thread.
- **Observer pattern.** `ProfileViewModel` notifies `ProfileViewModelListener`s, the same pattern as Alex's `SavePinViewModel`. The view model depends only on the listener interface, not on `ProfileView`.

## 5. Integration and running

- `ProfileModule.createPanel()` is the only wiring code. `ApplicationFactory` changed exactly one line: `features.put("Profile", ProfileModule::createPanel);`. The tab is created lazily by `LazyPanel` like every other tab.
- `ProfileModule.USER_ID = "demo-user"` is a placeholder until Matthew's login provides the signed-in user. It is the only place to change. The interactors never read a global session, the controllers pass the user id in through the input data.
- Profiles are stored in `data/profiles.json` (gitignored), one JSON object keyed by user id, written atomically (temp file, then move). A missing file means no profiles yet. A corrupt file is reported as "Couldn't load your profile, try again" instead of crashing.

```json
{
  "demo-user": {
    "displayName": "Kieran",
    "biography": "I like photos",
    "avatarPath": "/Users/me/Pictures/me.png",
    "theme": "LIGHT",
    "fontFamily": "SansSerif",
    "fontSize": 14,
    "highContrast": false
  }
}
```

**Demo:** `mvn compile exec:java`, open the Profile tab, click Edit Profile, change the name, Save, close the app, start it again, and the new name is still shown.

## 6. Tests

| Test class | What it shows |
|---|---|
| `entity.UserProfileTest`, `entity.AppearanceSettingsTest` | Entity defaults, trimming, null handling, font-size bounds, `defaultFor` |
| `use_case.edit_profile.EditProfileInteractorTest` | Valid save, blank name, name over 30, bio over 300, bad avatar extension, missing avatar file, failure leaves the stored profile unchanged, first edit with no stored profile, appearance carried forward unchanged, load and save `IOException` |
| `use_case.view_profile.ViewProfileInteractorTest` | Stored profile shown, missing profile shows the default, read failure message |
| `data_access.JsonProfileDataAccessObjectTest` | Save, then a new DAO on the same file loads it, missing file, corrupt file |
| `data_access.FileSystemAvatarFileCheckerTest` | Real file, missing file, directory, blank path |
| `interface_adapter.profile.*PresenterTest` | Success clears errors and sets "Profile updated" and `saved`, each `ProfileField` sets only its own error, failures keep the stored values |
| `interface_adapter.profile.*ControllerTest` | The interactor only runs on the executor, with the right user id and form values |
| `interface_adapter.profile.ProfileViewModelTest` | Listeners are notified on every state change |
| `app.ProfileModuleTest` | Real wiring on a `@TempDir` file: stored profile shown, missing file shows "New user", Edit then Save writes the file and a new panel reloads it, blank name shows the error and saves nothing |

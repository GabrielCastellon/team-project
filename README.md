# Pinterest-Inspired Image Discovery — Team Project

## Team and domain

Kevin Yamada, Gabriel Castellon, Alex Mack-Mumford, Charles Matthew Escober, and Kieran Murphy. Domain: image discovery and personal organization. See [team contract](TeamContract.md).

Photofy is a Java 17 desktop app for discovering and saving images.

## Current status

The app opens with five tabs. Accounts, Discovery and Pins share the signed-in account. Boards lets the signed-in account create a board and reload its saved board list with pin counts. Profile saves edits under a shared demo ID pending account integration.

## User stories and ownership

| ID | User Story | Responsible |
|---|---|---|
| 1 | As a user, I want to create an account and log in so that my boards and preferences are associated with my profile. | Matthew |
| 2 | As a user, I want to search and browse images so that I can discover inspiration for my projects. | Kevin |
| 3 | As a signed-in user, I want to find new images similar to the pins I've saved so I can discover more images I like. | Kevin |
| 4 | As a user, I want to create, view, and edit individual pins, including their titles, descriptions, images, and tags, so that I can manage saved content. | Alex |
| 5 | As a user, I want to create boards, view my board list, open a board to see its pins, edit its name or pins, and delete boards so that I can organize my saved images by topic. | Gabriel |
| 6 | As a user, I want to customize my profile and appearance settings so that I can personalize my experience. | Kieran & Matthew |

## Feature status

| Owner | Working in this version | Remaining work |
|---|---|---|
| Matthew | Register, sign in and sign out | Connect Profile to the session with Kieran |
| Kevin | Search and browse Unsplash. Recommend from My Pins ranks the current page using the signed-in account’s saved photos. | Both Discovery stories are implemented |
| Alex | Save photos, prevent duplicate saves and display the account’s pins with thumbnails | Assign pins to boards with Gabriel. Editing pin details and filtering are planned. |
| Gabriel | Create a named board and view the account’s saved boards after restarting. Sort boards by name or pin count. | Open boards to see their pins. Rename and delete are part of the full story. |
| Kieran | View and edit the display name, biography and avatar | Load and save the active account’s profile. Appearance settings are a stretch goal. |

See the [use-case descriptions](docs/USE_CASES.md) and [Discovery entity diagrams](docs/DISCOVERY_DESIGN.md).

## API and persistence

Discovery uses Unsplash for keyword search, result metadata, attribution and selection tracking. The live desktop check loaded 20 photos and ranked the 19 remaining after one was saved. [Unsplash documentation](https://unsplash.com/documentation)

Accounts, pins, boards and profiles are stored in JSON files under `data/`. The A/B sample collections use `data/discovery-preferences/`.

## How to run

Install JDK 17 and Maven 3.9+; open the repository root:

```sh
mvn --batch-mode --no-transfer-progress verify
mvn compile exec:java
```

Tests run headlessly; the interactive app requires a graphical desktop. The first build downloads Maven dependencies. Foundation tests need no credentials or live service.

## Design and teamwork

[Architecture and diagrams](docs/ARCHITECTURE.md) explain the Dependency Rule, SOLID, the Virtual Proxy pattern, and the per-feature integration checklist. [Contributing](CONTRIBUTING.md) describes issues, feature branches, reviews and Checkstyle. Preserve everyone's opportunities to implement and review their own use cases.

[Rubric evidence plan](docs/RUBRIC_EVIDENCE.md) maps the supplied November 15 rubric to evidence the team still needs. A skeleton is preparation, not proof that the whole MVP is complete.

## Kevin’s Discovery panel

Set `UNSPLASH_ACCESS_KEY` to your own Unsplash Access Key before starting, then open the Discovery tab. With no key, the shell still starts and that tab shows a configuration error; other tabs remain usable. Restart after setting environment variables. Never commit keys.

Sign in, search for images, and click **Save** on a result. Click **Recommend from My Pins** to see the other results ranked by similarity to the photos in your Pins tab. Signing out or switching accounts clears those recommendations and any Save feedback. You can start a new search while a previous account’s ranking is still finishing.

The separate A/B demo lets you like samples, mix preferences, rank for a group, and analyze a sample collection. It stores samples under `data/discovery-preferences/`. Set `DISCOVERY_DATA_DIR` to change that location. The default model compares RGB histograms. Optional CLIP support requires a separately exported model and `CLIP_MODEL_PATH`.

The two-account desktop check used the default colour model. See the [Discovery setup and model details](docs/KEVIN_DISCOVERY.md).

## Kieran’s Profile panel

Open the Profile tab to see the stored display name, biography and avatar. **Edit Profile** opens a form, **Save** validates (name required and at most 30 characters, biography at most 300, avatar empty or an existing .png/.jpg/.jpeg) and shows errors next to each field, **Cancel** discards edits. Profiles are saved to `data/profiles.json` (gitignored) and reload after a restart. Profile currently uses the shared `demo-user` ID in `app/ProfileModule`.

See [profile use case, class and sequence diagrams, and design decisions](docs/KIERAN_PROFILE.md). Change Appearance (theme, font size) is the planned stretch use case.

## Migration tracking

Original team main is preserved on the **kevin_working** branch. [Issue #9](https://github.com/GabrielCastellon/team-project/issues/9) tracks the foundation and the separate discovery review. Existing branches remain intact. Teammates should branch from the new shared main; Kevin's discovery code returns only through a teammate-reviewed PR.

## Integration verification

Run `mvn --batch-mode --no-transfer-progress verify` to execute the tests and Checkstyle. GitHub Actions runs the same command for pull requests and pushes to main. Earlier test results and design notes are recorded in the [verification guide](docs/VERIFICATION.md).

# Photofy

## Team and domain

Kevin Yamada, Gabriel Castellon, Alex Mack-Mumford, Charles Matthew Escober, and Kieran Murphy. Domain: image discovery and personal organization. See [team contract](TeamContract.md).

Photofy is a Java 17 desktop app for finding visual inspiration, saving photos, and organizing them into personal boards. Signed-in users can find similar images and edit their profiles.

## Current status

Accounts, Discovery, Pins, Boards, and Profile are integrated in the Swing app. It starts without an API key. Unsplash search requires one.

## User stories and ownership

| ID | User Story | Responsible |
|---|---|---|
| 1 | As a user, I want to create an account and log in so that my boards and preferences are associated with my profile. | Matthew |
| 2 | As a user, I want to search and browse images so that I can discover inspiration for my projects. | Kevin |
| 3 | As a signed-in user, I want to find new images similar to the pins I've saved so I can discover more images I like. | Kevin |
| 4 | As a signed-in user, I want to save photos and see their thumbnails and credits in my personal collection. | Alex |
| 5 | As a user, I want to create boards, view my board list, open a board to see its pins, edit its name or pins, and delete boards so that I can organize my saved images by topic. | Gabriel |
| 6 | As a signed-in user, I want to set my display name, biography, and profile photo. | Kieran |

## MVP use cases

| Lead | MVP use case | Observable goal |
|---|---|---|
| Matthew | Register / log in | Access the signed-in account's collections |
| Kevin | Search / browse images | Enter terms, view credited results, and browse pages |
| Kevin | Recommend from My Pins | Rank the current results by similarity to saved pins and exclude previously saved photos |
| Alex | Save / view pins | Save a Discovery result and see it in Pins after restarting |
| Gabriel | Create / view / open boards | Create a named board, find it in the list, and open its gallery |
| Gabriel | Edit / delete boards | Rename a board, update its pins, or delete it |
| Kieran | View / edit profile | Save a display name, biography, or profile photo and see it after signing back in |

## Entities

`User` provides a stable account ID. `Pin` links that ID to a photo. `Board` holds an owner ID and an ordered list of pin IDs. `UserProfile` stores the account's display name, bio, and avatar. The [Discovery diagrams](docs/DISCOVERY_DESIGN.md) cover the search and ranking types.

## API and persistence

Discovery uses Unsplash for keyword search, result metadata, and attribution. Liking an A/B preference sample calls the Unsplash tracking endpoint. Recommend from My Pins ranks the current results locally. [Unsplash documentation](https://unsplash.com/documentation)

The app uses local JSON files under `data/` for accounts, profiles, pins, and boards. The A/B preference sampler has its own storage. The [rubric evidence](docs/RUBRIC_EVIDENCE.md) includes a save-and-reload demo plan.

## How to run

Install JDK 17 and Maven 3.9+; open the repository root:

```sh
mvn --batch-mode --no-transfer-progress verify
mvn compile exec:java
```

Tests run headlessly; the interactive app requires a graphical desktop. The first build downloads Maven dependencies. Foundation tests need no credentials or live service.

## Design and teamwork

[Architecture and diagrams](docs/ARCHITECTURE.md) explain the Dependency Rule, SOLID, the Virtual Proxy pattern, and the per-feature integration checklist. [Contributing](CONTRIBUTING.md) describes issues, feature branches, reviews and Checkstyle. Preserve everyone's opportunities to implement and review their own use cases.

[Final rubric evidence](docs/RUBRIC_EVIDENCE.md) records the CAVE findings, ArchUnit and Checkstyle checks, and the presentation walkthrough.

## Kevin’s Discovery panel

Set `UNSPLASH_ACCESS_KEY` to your own Unsplash Access Key before starting, then open the Discovery tab. With no key, the shell still starts and that tab shows a configuration error; other tabs remain usable. Restart after setting environment variables. Never commit keys.

Sign in, search for images, and click **Save** on a result. Click **Recommend from My Pins** to see the other results ranked by similarity to the photos in your Pins tab. Signing out or switching accounts clears those recommendations.

The separate A/B demo lets you like samples, mix preferences, rank for a group, and analyze a sample collection. It stores samples under `data/discovery-preferences/`. Set `DISCOVERY_DATA_DIR` to change that location. The default model compares RGB histograms. Optional CLIP support requires a separately exported model and `CLIP_MODEL_PATH`.

The two-account desktop check used the default colour model. See the [Discovery setup and model details](docs/KEVIN_DISCOVERY.md).

## Kieran’s Profile panel

Open the Profile tab to see your display name, bio and round profile picture (a red circle with your initial until you choose a photo). **Edit profile** opens a form where **Choose photo…** picks a picture and shows a preview, **Save** validates (name required and at most 30 characters, biography at most 300, avatar empty or an existing .png/.jpg/.jpeg) and shows errors next to each field, **Cancel** discards edits. Profiles are saved to `data/profiles.json` (gitignored) and reload after a restart. The tab shows only the signed-in user's profile: signed out, it shows "Sign in to view your profile", and it reloads after every sign-in, account switch or logout.

See [profile use case, class and sequence diagrams, and design decisions](docs/KIERAN_PROFILE.md). Change Appearance (theme, font size) is the planned stretch use case.

## Migration tracking

Original team main is preserved on the **kevin_working** branch. [Issue #9](https://github.com/GabrielCastellon/team-project/issues/9) tracks the foundation and the separate discovery review. Existing branches remain intact. Teammates should branch from the new shared main; Kevin's discovery code returns only through a teammate-reviewed PR.

## Integration verification

The [Discovery verification notes](docs/VERIFICATION.md) record the earlier audit. See the [final rubric evidence](docs/RUBRIC_EVIDENCE.md) for the current review.

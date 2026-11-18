# Pinterest-Inspired Image Discovery — Team Project

## Team and domain

Kevin Yamada, Gabriel Castellon, Alex Mack-Mumford, Charles Matthew Escober, and Kieran Murphy. Domain: image discovery and personal organization. See [team contract](TeamContract.md).

The planned application lets people discover images, save pins to boards, and get recommendations from their visual preferences. Accounts associate collections with their owners, and profile settings personalize the experience.

## Current status

The application uses the **shared foundation**: a runnable Java 17 Swing window with Accounts, Pins, Boards and Profile placeholders plus Kevin’s Discovery panel. Kevin’s discovery implementation is proposed here for teammate review; the other owners’ features remain pending. It starts without API keys or local data. Kevin’s previous discovery module is preserved in Git history and the migration backup; this feature branch restores it on the foundation. Discovery uses Unsplash and JSON preference snapshots; placeholders do not implement API or persistence behavior.

## User stories and ownership

| ID | User Story | Responsible |
|---|---|---|
| 1 | As a user, I want to create an account and log in so that my boards and preferences are associated with my profile. | Matthew |
| 2 | As a user, I want to search and browse images so that I can discover inspiration for my projects. | Kevin |
| 3 | As a user, I want personalized recommendations based on my board's preferences so that I can discover images relevant to my interests. | Kevin |
| 4 | As a user, I want to create, view, and edit individual pins, including their titles, descriptions, images, and tags, so that I can manage saved content. | Alex |
| 5 | As a user, I want to create, view, and edit boards so that I can organize images by topic. | Gabriel |
| 6 | As a user, I want to customize my profile and appearance settings so that I can personalize my experience. | Kieran & Matthew |

## MVP use cases

| Lead | MVP use case | Observable goal | Foundation status |
|---|---|---|---|
| Matthew | Register / log in | Access collections associated with the signed-in account | Placeholder |
| Kevin | Search / browse images | Enter terms, view credited results, browse pages | Implemented on this feature branch |
| Kevin | Recommend images | Rank candidates against saved preference samples | Implemented on this feature branch |
| Alex | Manage pins | Create, view and edit saved image details | Placeholder |
| Gabriel | Manage boards | Create, view, rename and delete image collections | Placeholder |
| Kieran, coordinating with Matthew | Customize profile | Edit personal display and appearance settings | Placeholder |

Kieran should own a distinct complete profile use case; coordinate the overlap with Matthew's account story so each member can demonstrate their own contribution. Detailed proposed flows and unresolved integration decisions are in [use cases](docs/USE_CASES.md). These are planning contracts for owners to confirm, not implemented teammate features.

## Entities

Planned shared concepts are User (stable identity), Profile (display/appearance), Pin (image metadata), and Board (owner and pin references). Their owners must agree on fields, invariants and relationships before adding shared Java classes. The foundation deliberately contains no invented implementations. Kevin's discovery PR supplies the existing Photo, FeatureVector and ScoredPhoto value types and documents their [use-case class diagrams, search sequences, skeleton startup and entities](docs/DISCOVERY_DESIGN.md).

## API and persistence

Planned external API: [Unsplash](https://unsplash.com/documentation), for keyword search, result metadata, attribution and selection tracking. The shell makes no API calls; opening Discovery constructs the Unsplash adapter. Search/selection actions make the requests. Live access has **not been verified in this migration**. A developer with an access key must record the manual check in the feature PR.

Planned local JSON storage: accounts (Matthew), profiles (Kieran), pins (Alex) and boards (Gabriel). Filenames and cross-feature contracts remain to be agreed. Kevin's separate discovery module persists preference samples; they are not authenticated boards or accounts. Demonstrate saving, restarting and loading for the final presentation.

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

Search, like samples into preference A/B, rank the current candidate page, mix preferences, rank for a group, or analyze a sample collection. These are demo preference IDs, not authenticated accounts or real team boards. JSON snapshots live under `data/discovery-preferences/`; `DISCOVERY_DATA_DIR` overrides this path. Default embeddings are RGB histograms; optional CLIP requires a separately exported compatible model and `CLIP_MODEL_PATH`.

See [discovery setup, limitations, math and integration](docs/KEVIN_DISCOVERY.md). Live Unsplash access and real optional CLIP inference remain unverified in this migration. A teammate must review and merge this PR.

## Migration tracking

Original team main is preserved on the **kevin_working** branch. [Issue #9](https://github.com/GabrielCastellon/team-project/issues/9) tracks the foundation and the separate discovery review. Existing branches remain intact. Teammates should branch from the new shared main; Kevin's discovery code returns only through a teammate-reviewed PR.

## Integration verification

[Verification and rubric evidence](docs/VERIFICATION.md) records the 32-test suite, real-module shell tests, dependency/style checks and remaining manual demo checks. The active feature is built on the skeleton through app.Main and ApplicationFactory; kevin_working is only the historical backup and is not used at runtime. [The UML guide](docs/DISCOVERY_DESIGN.md) separates per-use-case class diagrams from interaction-specific sequences.

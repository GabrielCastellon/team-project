# Pinterest-Inspired Image Discovery — Team Project

## Team and domain

Kevin Yamada, Gabriel Castellon, Alex Mack-Mumford, Charles Matthew Escober, and Kieran Murphy. Domain: image discovery and personal organization. See [team contract](TeamContract.md).

The planned application lets people discover images, save pins to boards, and get recommendations from their visual preferences. Accounts associate collections with their owners, and profile settings personalize the experience.

## Current status

This branch is the **shared foundation**: a runnable Java 17 Swing window with Accounts, Discovery, Pins, Boards and Profile placeholders. It implements navigation, not the team's business use cases. It starts without API keys or local data. Kevin's previous discovery module is preserved in Git history and the migration backup, and is to return through a separate teammate-reviewed PR after this foundation. No API or persistence requirement is satisfied by placeholders.

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
| Kevin | Search / browse images | Enter terms, view credited results, browse pages | Separate discovery PR |
| Kevin | Recommend images | Rank candidates against saved preference samples | Separate discovery PR |
| Alex | Manage pins | Create, view and edit saved image details | Placeholder |
| Gabriel | Manage boards | Create, view, rename and delete image collections | Placeholder |
| Kieran, coordinating with Matthew | Customize profile | Edit personal display and appearance settings | Placeholder |

Kieran should own a distinct complete profile use case; coordinate the overlap with Matthew's account story so each member can demonstrate their own contribution. Detailed proposed flows and unresolved integration decisions are in [use cases](docs/USE_CASES.md). These are planning contracts for owners to confirm, not implemented teammate features.

## Entities

Planned shared concepts are User (stable identity), Profile (display/appearance), Pin (image metadata), and Board (owner and pin references). Their owners must agree on fields, invariants and relationships before adding shared Java classes. The foundation deliberately contains no invented implementations. Kevin's discovery PR supplies the existing Photo, FeatureVector and ScoredPhoto value types and documents their actual fields/methods separately.

## API and persistence

Planned external API: [Unsplash](https://unsplash.com/documentation), for keyword search, result metadata, attribution and selection tracking. The foundation makes no API calls. Live access has **not been verified in this migration**. A developer with an access key must record the manual check in the feature PR.

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

## Migration tracking

Original team main is preserved on the **kevin_working** branch. [Issue #9](https://github.com/GabrielCastellon/team-project/issues/9) tracks the foundation and the separate discovery review. Existing branches remain intact. Teammates should branch from the new shared main; Kevin's discovery code returns only through a teammate-reviewed PR.

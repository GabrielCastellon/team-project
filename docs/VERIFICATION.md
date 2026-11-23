# Discovery verification

Verified November 21 on team main at `0171ba8`, after [Discovery integration](https://github.com/GabrielCastellon/team-project/pull/10), [Save Pin part 1](https://github.com/GabrielCastellon/team-project/pull/14) and [profile entities](https://github.com/GabrielCastellon/team-project/pull/15) merged.

## Automated checks

On a clean Windows checkout, Java 17 and `mvn --batch-mode --no-transfer-progress verify` ran 55 tests with no failures, errors or skips and zero violations under the configured Checkstyle rules. The [team CI run](https://github.com/GabrielCastellon/team-project/actions/runs/44958979289) also passed. This is a snapshot of that commit. The tests use fixtures for the API and exercise preference storage separately from team pin storage.

## CAVE and the dependency rule

Run `cave verify` at the project root. CAVE 1.1.3 reported missing directories `entities`, `views` and `database`. This repo uses `entity`, `view` and `data_access`. The analyzer did not give the project a clean report.

| Use case | Dependencies flagged by CAVE |
| --- | --- |
| Discovery | controller to presenter; controller, presenter and input boundary to entities; view to presenter |
| Save Pin | controller and input data to entities |

The ArchUnit dependency test passed on the same checkout. It checks allowed package dependencies, not whether each use case follows the sequence taught in class. Discovery's references to entities point inward. Its controller calls a presenter in the same adapter layer, and the Swing view implements a presenter interface. The interactor returns results through the input boundary instead of calling an output boundary. This is a documented difference from the course sequence; see the [class and sequence diagrams](DISCOVERY_DESIGN.md). The Save Pin findings are reported here for its owner.

A separate concrete issue on this main revision is that the Discovery controller imports SwingWorker. [PR 11](https://github.com/GabrielCastellon/team-project/pull/11) moves thread scheduling into the view layer and adds an architecture test. CAVE still flagged the same five Discovery dependencies on that PR at `50cf3bd`. Passing ArchUnit and addressing the Swing import do not clear the CAVE findings.

## Evidence for the demo

The November 15 project rubric gives 10 points each for Clean Architecture, overall design and code quality. Show the actual search sequence, explain the controller and presenter tradeoff, and use a reviewed PR and Checkstyle run as evidence. For design, explain dependency inversion using the catalog and embedding interfaces, single responsibility using the presenter, and why the embedding cache uses Decorator to avoid repeated inference.

The rubric scores the live demo separately from tests. On the presentation machine:

1. Start the shared app with Java 17 and a locally configured Unsplash access key. Search, check attribution, and show one empty or invalid search.
2. Save a preference, reopen the app and show that it loads. Rank a new page of candidates. Confirm the selected embedding model before describing the result as CLIP.
3. After the owners integrate accounts, pins and boards, demonstrate saving a pin for the signed in user and reopening it after a restart. A Discovery preference alone does not verify this flow.

No live search, model inference or full team demo was performed as part of this November 21 audit.

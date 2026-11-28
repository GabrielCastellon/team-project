# Final presentation evidence

The audit starts from main commit `0825d7d`, which includes Profile and Create, View, Open, Edit, and Delete Board. The [MVP in the README](../README.md#mvp-use-cases) lists what each teammate can demonstrate.

## Clean Architecture and CAVE

From the project root, with Node.js and npm available:

```sh
npm exec --yes --package clean-architecture-visualizer@1.1.3 -- cave verify
mvn --batch-mode --no-transfer-progress clean verify
```

CAVE was run before and after the changes. The pinned npm package is 1.1.3. Its bundled CLI prints 1.1.0 for `--version`. CAVE exited with code 0 in both runs and marked nine of thirteen detected groups with findings. The second run reported one fewer `edit_boards` cross-use-case edge. The four groups marked clear were `security`, `view_boards`, `view_pins`, and `view_profile`.

| CAVE output | Source review |
|---|---|
| Missing `entities`, `views`, and `database` directories | The scanner probes both forms of directory names. Photofy uses the course layout with `entity`, `view`, and `data_access`. The singular directories were recognized. |
| `view → dataAccessInterface` | CAVE assigns roles from the full path. For example, names in `use_case/view_pins` match its “view” check, and `JsonBoardDataAccessObject` matches its “accessobject” check. The actual JSON adapters implement inward-facing interfaces, and ArchUnit rejects imports from `view` into `data_access` or `use_case`. |
| `view → view (external)` and `viewModel → view` | CAVE groups dependencies by use case and guessed role. Shared state and reuse across board use cases need inspection in the source. For example, Edit Board returns `OpenBoardOutputData` within `use_case`, and its presenter reuses `OpenBoardPresenter` within `interface_adapter`. Both are same-layer dependencies. |
| Discovery `view → presenter` | `DiscoveryView` implements `DiscoveryPresenter.View`, a rendering interface in the inner adapter layer. Discovery returns response data through its input boundary, and the controller invokes the presenter. The [Discovery diagrams](DISCOVERY_DESIGN.md) record that design. |

The scanner's [role-matching implementation](https://github.com/CA-Visualizer-for-Education/Clean-Architecture-Visualizer/blob/main/clean-architecture-visualizer/src/use_case/graphVerification/graphVerificationInteractor.ts) explains why these labels need a source review. The [layer diagram](ARCHITECTURE.md#dependency-rule) and [ArchUnit test](../src/test/java/architecture/DependencyRuleTest.java) show the actual package rules.

Two findings from reviewing the source became changes in this PR:

- The Accounts panel sat under the composition package. It now lives under `view`, and ArchUnit enforces that placement for production Swing panels.
- Edit Board's presenter depended on a concrete interactor and compared its error text to decide whether to clear the editor. The missing-board result now has a dedicated output callback. The [class diagram](EDIT_BOARDS_UML.puml) shows the boundary, and the [regression test](../src/test/java/use_case/edit_boards/EditBoardInteractorTest.java) checks both loading an inaccessible board and saving a missing one. ArchUnit now rejects interface adapters that depend on concrete `*Interactor` classes.

## Code quality

The baseline passed 182 tests and the existing Checkstyle configuration. Adding Checkstyle's `UnusedImports` rule exposed three stale imports in production and test code. They were removed, and the rule runs on both source sets during the existing Maven verification step. The PR adds the regression test and retains Java 17 CI. On the Windows review checkout, `mvn --batch-mode --no-transfer-progress --offline clean verify` passed with 183 tests, zero failures or skips, and zero Checkstyle violations.

For the presentation, show this PR's diff alongside its Java CI log. Point out the new check and the regression test, then use [PR #38's review](https://github.com/GabrielCastellon/team-project/pull/38#pullrequestreview-6096210907) as a concrete example of teammate feedback. The [follow-up commit](https://github.com/GabrielCastellon/team-project/commit/c3e838e) addressed stale saved labels and pending recommendations after switching accounts.

## Rubric and demo

| Rubric area | Evidence to show |
|---|---|
| Presentation materials | Use the project deck, the [layer diagram](ARCHITECTURE.md#dependency-rule), and the [Open Board class diagram](OPEN_BOARDS_UML.puml). Show the board list, the opened gallery, and the ownership lookup in the interactor. |
| Specification and project scope | Explain the five distinct user goals in the [MVP](../README.md#mvp-use-cases). Keep pin detail editing and appearance customization out of the demonstrated scope. |
| API usage | Run an Unsplash keyword search. Point out filters and photographer credit. Explain that saved-pin ranking runs locally. The integration tests use a fixture, so the live call is a separate demo check. |
| Data persistence | Save a new pin, quit, relaunch, and sign back in. Show the same image in Pins. Profile also persists by user. [Pin restart test](../src/test/java/app/SavePinPersistenceIntegrationTest.java) · [Profile integration tests](../src/test/java/app/ProfileModuleTest.java) |
| Clean Architecture | Trace Open Board from click to controller, interactor ports, presenter, and view model. Use the CAVE findings and the ArchUnit rules above to explain the dependency direction. [Open Board sequence](OPEN_BOARDS_SEQUENCE.puml) |
| Overall design | Show dependency inversion in Open Board and interface segregation in Profile. Explain the [Virtual Proxy](ARCHITECTURE.md#implemented-design-pattern-virtual-proxy) and how supplying a new panel extends the shell. Confirm lecture coverage before claiming the exceptional design-pattern criterion. |
| Code quality | Show Checkstyle output, the two ArchUnit safeguards, and a representative review and follow-up. |
| Functionality demo | Matthew signs in. Kevin searches and ranks results from saved pins. Alex saves a new pin and reloads it after restart. Gabriel opens and edits a prepared board. Kieran changes the signed-in profile and shows the saved change. |
| Delivery and Q&A | Rehearse the speaker handoffs and the full demo within 15 minutes. Keep the app open for up to five minutes of questions. Each teammate can answer questions about their own use case. |

Use one prepared account with a saved pin and a populated board. Configure the Unsplash key locally. The live API call, restart demonstration, delivery, and Q&A require team rehearsal on the presentation build.

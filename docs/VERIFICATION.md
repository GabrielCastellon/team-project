# Discovery verification and rubric evidence

## Active application and backup

- Shared foundation: team `main`, published at `01a32869d53ef368c899fb44600ad0a95b52b528` before discovery integration.
- Original code: `kevin_working` at `1cdd12d817b281ba80dbe2d84c8cc379e8a27f1a`; historical backup only. It is not a runtime dependency and is not a branch to merge into the skeleton.
- Active proposed feature: `feature/kevin-discovery-team`, PR #10, based on the foundation. `mvn compile exec:java` starts `app.Main`, creates the shared shell and opens Discovery inside that shell.
- Review: GabrielCastellon was requested on PR #10; it remains unmerged. Issue #9 tracks the work. Teammates' account, pin, board and profile features remain theirs to implement.

## Automated checks

Run from the repository root with JDK 17:

```sh
mvn --batch-mode --no-transfer-progress verify
```

The suite now has 32 tests. `verify` runs the tests, packages the application and runs the targeted Checkstyle configuration. GitHub Actions runs the same command on Java 17.

| Evidence | What it establishes |
|---|---|
| `src/test/java/app/DiscoveryShellIntegrationTest.java` | The real Discovery module can be constructed with explicit fixture configuration inside the real skeleton; initialization is lazy, its panel/state is retained, other owners' tabs remain placeholders, and missing configuration does not break navigation |
| `src/test/java/view/MainViewTest.java` | Generic proxy/navigation state retention, failure isolation/retry and EDT enforcement |
| `src/test/java/architecture/DependencyRuleTest.java` | Inner layers do not depend on outer implementations; adapters and views respect the configured boundaries |
| `src/test/java/interface_adapter/discovery/DiscoveryControllerTest.java` | Requests run off the EDT, UI updates return to the EDT, failures restore controls and stale candidates are not reused |
| `src/test/java/interface_adapter/discovery/DiscoveryPresenterTest.java` | Failed preview retains metadata; scores use stable locale-independent formatting |
| `src/test/java/use_case/discovery/DiscoveryServiceTest.java` | Search validation, ranking/exclusions, mixing endpoints, group compromise, analysis and gateway failure behavior |
| Entity/data-access tests | Vector mathematics, JSON save/reload/corruption, local Unsplash request/response/error/tracking fixtures, preprocessing and embedding cache behavior |
| Exported-source build | The feature can be built/tested from a source archive containing no `.git` directory, backup branch or old checkout |

The shell composition tests use a fixture access-key string and make no network requests; they do not validate a real credential. Existing HTTP adapter tests use a loopback server. The exported-source build establishes independence from Git metadata, not live API availability.

## Rubric mapping

- **Specification and scope:** README retains all owners/stories; `docs/USE_CASES.md` provides proposed flows. Kevin's features remain distinct from teammates' account/pin/board/profile work.
- **Clean Architecture:** `docs/ARCHITECTURE.md` explains source dependency direction. `docs/DISCOVERY_DESIGN.md` shows actual use-case types, interface realizations, search success/failure, skeleton startup and ranking/persistence dependencies. The service's synchronous response style is explicitly documented rather than inventing an output callback.
- **SOLID:** explain SRP using controller/presenter/composition responsibilities and DIP using the input boundary and use-case-owned catalog/embedding/store ports; show the concrete code and tests. Open/Closed is illustrated by the panel supplier and interchangeable embedding providers.
- **Patterns/extensibility:** shell Virtual Proxy, embedding Decorator and embedding Strategy are implemented. The rubric's Exceptional criterion requires a pattern not presented in class; the supplied assessment files do not establish which patterns were taught. Do not claim that condition without checking lecture coverage.
- **Code quality:** Checkstyle is enforced by `mvn verify`, alongside tests and architectural checks. It is a targeted baseline, not every course style rule. Use PR #10 and its review history as evidence of teammate review once completed.
- **API/persistence:** Unsplash adapters and JSON preferences are implemented and fixture-tested. Demonstrate a real call and save/restart/reload in the live demo; preference collections are not authenticated team boards.
- **Presentation:** diagrams/evidence are prepared in Markdown, not a completed presentation deck. The whole team must present, rehearse and demonstrate its completed integrated MVP.

## Manual checks still required before the final demo

1. Set a real `UNSPLASH_ACCESS_KEY`, start the shared app and open Discovery. Check search results, attribution, page/filter/order controls and errors.
2. Save preference samples, recommend/mix/group/analyze, restart, and verify sample reload/removal.
3. If presenting CLIP, export a compatible model and verify real inference and ranking on the demo machine. Default RGB histogram mode is not semantic CLIP.
4. After teammates integrate their features, test actual account ownership, board/pin mapping and profile behavior together.

No live Unsplash call, real optional CLIP inference, completed teammate integration, final slides or human review is claimed by these automated checks.

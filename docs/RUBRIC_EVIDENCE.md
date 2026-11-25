# Evidence plan — November 15, 2026 draft rubric

Sources: user-supplied `Project Rubric 2026 [Nov 15].pdf`, `CSC207 Project Rubrics — Lab Summary 2026.pdf`, README template and presentation template. They provide assessment context; they do not authorize implementing teammates' use cases or merging Kevin's PR.

| Category | Foundation / later evidence |
|---|---|
| Specification / scope | README retains stories and owners; use-case flow table needs owners' confirmation; demonstrate a significant distinct story per member |
| Clean Architecture | Dependency diagram + ArchUnit rules; add actual class/sequence diagrams for implemented features; explain any deviations |
| SOLID / design patterns | Explain at least two SOLID principles with actual code for Excellent; explain extensibility; Virtual Proxy is implemented in LazyPanel |
| Exceptional design criterion | Rubric specifically says significant appropriate use of at least one pattern **not presented in class**. Confirm Proxy against lectures; KISS alone cannot satisfy this |
| Code quality | `mvn verify` runs tests and a targeted Checkstyle configuration; retain CI logs and a representative reviewed PR. Rubric explicitly requires a tool like Checkstyle for Excellent or higher |
| External API | Foundation has no calls; discovery PR restores Unsplash. Show request/response mapping and record a real successful call using a private access key |
| Persistence | Foundation has no persistence; feature PRs must show save + restart + reload. Discovery preferences alone do not prove account/board/pin integration |
| Demo | Prepare realistic data and configuration; walk through each member's story, including errors and persistence |
| Slides / delivery / Q&A | Use supplied template, readable diagrams and rehearsed transitions; 15-minute limit and all members speaking are required for higher delivery levels |

The skeleton is not a completed MVP. Do not claim rubric compliance solely because folders or tests exist. Checkstyle checks a small explicit baseline, not the complete course style standard. Offline fixtures do not prove real Unsplash credentials or optional CLIP model inference.

Presentation checklist from the supplied template: introduce team/project, explain user stories/MVP, demo, API and persistence, architecture, SOLID/design pattern/extensibility, quality/review process, challenges/future work and references. Keep updating evidence as real feature PRs land.

## Implemented discovery evidence

PR #10 is based on the already published skeleton. [Discovery UML](DISCOVERY_DESIGN.md) now shows actual use-case classes, interface realizations, search success/failure and shell startup, rather than only entity classes. [Verification](VERIFICATION.md) links the 32 tests to code-quality, architecture, integration and API/persistence evidence. It distinguishes fixture checks from live/manual checks and records the synchronous response tradeoff. The backup branch is historical only; final human review and teammates' features are still outside this PR's completed work.

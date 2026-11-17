# GitHub issue draft: Establish shared Swing foundation and restore discovery through review

This is the prepared issue body. An actual issue number/link must be added after GitHub write access is available.

## Goal
Give teammates a minimal Java 17 / Swing foundation for their own use cases while preserving Kevin's existing discovery module and every historical commit.

## Acceptance criteria
- [ ] Copy `kevin/java17-demo` at `3db4d4aa4021e458c6d4cf6691b818ffd569cc1f` to `archive/kevin-java17-demo` and verify before deleting its old branch name.
- [ ] Preserve original main (`1cdd12d817b281ba80dbe2d84c8cc379e8a27f1a`) as `kevin`, verifying the remote backup before changing main.
- [ ] Publish a normal descendant foundation commit on main; retain the team contract and history.
- [ ] Run the Swing shell, offline tests, dependency checks and Checkstyle on Java 17.
- [ ] Document feature ownership, Clean Architecture/SOLID, API/persistence contracts and design pattern evidence.
- [ ] Publish Kevin's discovery code on a separate branch based on the foundation and open a PR targeting main.
- [ ] A teammate reviews and merges that discovery PR; the author/assistant does not merge.

## Ownership
Matthew: accounts; Kevin: discovery/recommendations; Alex: pins; Gabriel: boards; Kieran, coordinating with Matthew: profile/appearance. Implement only shared foundations and Kevin's existing feature in this migration.

## Rubric
The November 15 rubric requires concrete design-pattern evidence; Exceptional specifically calls for a significant appropriate pattern not presented in class. Virtual Proxy is implemented in the shell; confirm classroom coverage. KISS is documented but does not satisfy that criterion alone. Excellent code quality or higher requires a tool such as Checkstyle; verify runs it.

## Review evidence
Link the foundation commit, discovery PR, test/Checkstyle CI results, architecture diagrams, API manual check and persistence save/reload check. Offline fixtures do not prove live Unsplash credentials or real optional CLIP model inference.

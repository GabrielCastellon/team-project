Tracking issue: https://github.com/GabrielCastellon/team-project/issues/9

# Shared Swing foundation and Kevin discovery migration

## Goal
Give teammates a minimal Java 17 / Swing foundation for their own use cases while preserving Kevin's existing code and all Git history.

## Acceptance criteria
- [x] Preserve current team main (`1cdd12d817b281ba80dbe2d84c8cc379e8a27f1a`) as `kevin_working`, and verify the remote backup before updating main.
- [ ] Publish the shared foundation on main as a normal descendant commit; retain the team contract and history.
- [ ] Include runnable Swing placeholders, inward dependency checks, targeted Checkstyle, Java 17 CI, ownership/use-case documentation and review guidance.
- [ ] Publish Kevin's discovery integration on a separate feature branch and open a PR against the skeleton main.
- [ ] A teammate reviews and merges Kevin's PR; the assistant must not merge it or enable auto-merge.

## Ownership
Matthew: accounts; Kevin: discovery/recommendations; Alex: pins; Gabriel: boards; Kieran coordinating with Matthew: profile/appearance. Teammates implement their own complete use cases; this migration leaves their features as placeholders.

## Design and rubric evidence
The shell implements Virtual Proxy through LazyPanel; confirm it was not presented in class before claiming the rubric's Exceptional design criterion. KISS is an additional principle, not a replacement for the design-pattern criterion. Checkstyle runs under `mvn verify` alongside tests and architecture checks.

## Validation
The previously prepared foundation passed 4 tests and Checkstyle; integrated discovery passed 30 tests and Checkstyle on Java 17, including GitHub CI in Kevin's fork. The final team-repo commits will be checked again. Offline fixtures do not prove live Unsplash credentials, real optional CLIP inference, or the full interactive GUI workflow.

## Branch naming
Use `kevin_working` as Kevin requested. Existing `kevin/java17-demo` remains untouched; no branch renaming or deletion is required.

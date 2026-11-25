# Contributing

Use JDK 17 and Maven 3.9+. Start from the latest `main` and create a feature branch:

```sh
git switch main
git pull --ff-only
git switch -c yourname/your-use-case
mvn --batch-mode --no-transfer-progress verify
```

Open a GitHub issue for your use case, link it in your PR, and ask a teammate to review before merging. Each owner writes the majority of their own interactor, boundaries, controller, presenter and UI. Coordinate shared classes before changing fields. Do not copy the backup branch wholesale into main.

See [architecture](docs/ARCHITECTURE.md) for dependency rules and the feature checklist. Replace your placeholder factory in `app.ApplicationFactory`, keeping other modules intact. All Swing factories run on the EDT; put HTTP, disk operations and inference into background tasks, then publish UI updates on the EDT.

`mvn verify` runs offline unit tests, architecture rules and targeted Checkstyle checks. Checkstyle currently checks tabs, final newlines, outer filenames, empty statements and equals/hashCode consistency; it is not the full course style standard. Tighten it by team agreement. Review correctness, tests, style, Javadoc and architecture manually as well.

Never commit API keys, local data, model files, or passwords. Live API checks use each developer's own environment variables. Record successful API calls and save/reload demonstrations honestly in your feature documentation.

After the foundation lands, teammates with older branches should create a new branch from main and bring over only their feature files/commits. A plain merge of an old branch can restore removed standalone files. The `kevin_working` backup is historical reference, not a branch to merge into main.

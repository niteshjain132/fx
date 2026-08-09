# cursor-ws

Workspace repo for **independent** apps and experiments.

https://github.com/niteshjain132/cursor-ws

Projects here are **not** a Maven reactor. Each app owns its own `pom.xml` (or other build file) inside its folder and can use a completely different stack. Unrelated apps do not share a parent POM.

```
cursor-ws/
├── README.md                 ← this index
├── projecthub/               ← ProjectHub (own pom.xml)
│   ├── pom.xml
│   ├── src/
│   └── scripts/
└── <your-next-app>/          ← add another self-contained folder
    └── pom.xml / package.json / …
```

## Projects

| Folder | Description | Build |
|--------|-------------|-------|
| [`projecthub/`](./projecthub) | macOS engineering dashboard (Java 21, Spring Boot 3, JavaFX 21, AtlantaFX) | `cd projecthub && mvn spring-boot:run` |

## Adding a new (unrelated) project

1. Create a new top-level folder, e.g. `my-app/`.
2. Put that project's own build file inside it (`pom.xml`, `package.json`, `Cargo.toml`, …).
3. Do **not** add a root aggregator POM — keep projects isolated.
4. Link it in the table above.

## ProjectHub quick start

```bash
cd projecthub
mvn clean test
mvn spring-boot:run
```

macOS double-clickable app (build on a Mac):

```bash
cd projecthub
./scripts/package-macos.sh
# → target/dist/ProjectHub.app
```

See [`projecthub/PROJECTHUB.md`](./projecthub/PROJECTHUB.md) for architecture, start/stop, and packaging details.

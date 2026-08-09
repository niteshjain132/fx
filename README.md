# cursor-ws

Cursor workspace monorepo.

## Modules

| Module | Description |
|--------|-------------|
| [`projecthub`](./projecthub) | macOS-native engineering dashboard (Java 21, Spring Boot 3, JavaFX 21, AtlantaFX) |

## Build

```bash
mvn clean test
mvn -pl projecthub spring-boot:run
```

See [PROJECTHUB.md](./PROJECTHUB.md) for ProjectHub details.

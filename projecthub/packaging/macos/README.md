# macOS packaging assets for ProjectHub

ProjectHub.png — source icon (1024×1024). On a Mac, `scripts/package-macos.sh`
converts this to ProjectHub.icns via sips + iconutil.

Build the double-clickable app (must run on macOS / MacBook):

```bash
cd projecthub
./scripts/package-macos.sh
```

Outputs:
- `target/dist/ProjectHub.app`
- `target/dist/ProjectHub-1.0.0.dmg`

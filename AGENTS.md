# AGENTS.md

## Cursor Cloud specific instructions

### Repositories in this workspace

This git repo contains two independent projects:

| Path | Description |
|------|-------------|
| `/` (root) | **FX Live Rate Dashboard** — React + TypeScript frontend, Express/Socket.IO backend on port 3000 |
| `ai-agents/` | **AI Agents monorepo** — npm workspaces for building agent apps |

### FX dashboard (root)

| Service | Command | Port |
|---------|---------|------|
| Backend + static files | `npm start` (from repo root) | 3000 |

Workflow: `npm install` → `npm run build` → `npm start`. Open http://localhost:3000.

No lint/test scripts. TrueFX API may return empty data; app still runs.

### AI Agents monorepo (`ai-agents/`)

| Package | Command | Notes |
|---------|---------|-------|
| All workspaces | `npm run build` | Builds `packages/agent-core` and all apps |
| hello-agent | `npm start` | Interactive CLI starter agent |

Workflow from `ai-agents/`:

```bash
npm install
npm run build
npm start          # runs hello-agent
npm run create:app <name>   # scaffold a new app from template
```

`ai-agents/` has its own `package.json`, `node_modules`, and TypeScript 5 — do not rely on the root FX project's older TypeScript 2 toolchain when working under `ai-agents/`.

### Gotchas

- FX: run `npm run build` after changes under `src/`; no watch mode.
- FX: frontend externals load from CDN in `index.html`.
- AI Agents: build `agent-core` before apps if you change shared types (`npm run build` at monorepo root handles order).


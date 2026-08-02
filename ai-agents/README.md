# ai-agents

Monorepo for building AI agent applications.

## Structure

```
ai-agents/
├── apps/           # Individual agent applications
│   └── hello-agent # Starter CLI agent (template for new apps)
├── packages/       # Shared libraries
│   └── agent-core  # Core agent types, runner, and tool registry
└── scripts/        # Repo tooling
```

## Quick start

```bash
cd ai-agents
npm install
npm run build
npm start
```

## Create a new app

```bash
npm run create:app my-new-agent
```

This scaffolds `apps/my-new-agent` from the hello-agent template.

## Workspaces

| Package | Description |
|---------|-------------|
| `@ai-agents/agent-core` | Shared agent framework (messages, tools, runner) |
| `hello-agent` | Interactive CLI agent starter |

## Development

```bash
npm run build    # Build all packages and apps
npm run dev      # Run hello-agent in watch mode
npm start        # Run hello-agent
```

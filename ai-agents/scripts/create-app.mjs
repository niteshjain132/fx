#!/usr/bin/env node
import { cpSync, existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";

const appName = process.argv[2];

if (!appName) {
  console.error("Usage: npm run create:app <app-name>");
  process.exit(1);
}

if (!/^[a-z][a-z0-9-]*$/.test(appName)) {
  console.error("App name must be lowercase alphanumeric with hyphens (e.g. my-agent)");
  process.exit(1);
}

const root = new URL("..", import.meta.url).pathname;
const templateDir = join(root, "apps/hello-agent");
const targetDir = join(root, "apps", appName);

if (existsSync(targetDir)) {
  console.error(`App already exists: apps/${appName}`);
  process.exit(1);
}

mkdirSync(targetDir, { recursive: true });
cpSync(join(templateDir, "src"), join(targetDir, "src"), { recursive: true });
cpSync(join(templateDir, "tsconfig.json"), join(targetDir, "tsconfig.json"));

const packageJson = JSON.parse(readFileSync(join(templateDir, "package.json"), "utf8"));
packageJson.name = appName;
writeFileSync(join(targetDir, "package.json"), JSON.stringify(packageJson, null, 2) + "\n");

console.log(`Created apps/${appName}`);
console.log(`  cd ai-agents && npm install && npm run build --workspace ${appName}`);

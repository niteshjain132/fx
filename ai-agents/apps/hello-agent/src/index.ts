import { Agent } from "@ai-agents/agent-core";
import type { Tool } from "@ai-agents/agent-core";
import * as readline from "node:readline/promises";
import { stdin as input, stdout as output } from "node:process";

const tools: Tool[] = [
  {
    name: "time",
    description: "Returns the current server time",
    execute: () => new Date().toISOString(),
  },
  {
    name: "calc",
    description: "Evaluates a simple math expression (e.g. 2 + 2)",
    execute: (rawInput: unknown) => {
      const expression = String(rawInput).trim();
      if (!/^[\d\s+\-*/().]+$/.test(expression)) {
        throw new Error("Only numbers and + - * / ( ) are allowed");
      }
      const result = Function(`"use strict"; return (${expression})`)();
      return `${expression} = ${result}`;
    },
  },
];

const agent = new Agent({
  name: "Hello Agent",
  instructions: "You are a helpful starter agent in the ai-agents monorepo.",
  tools,
});

async function main(): Promise<void> {
  console.log("ai-agents / hello-agent");
  console.log("Type a message and press Enter. Commands: /tool time, /tool calc 2+2, /exit\n");

  const rl = readline.createInterface({ input, output });

  while (true) {
    const line = await rl.question("you> ");
    const trimmed = line.trim();

    if (!trimmed) {
      continue;
    }

    if (trimmed === "/exit") {
      console.log("Goodbye!");
      break;
    }

    const { finalResponse } = await agent.run(trimmed);
    console.log(`\nagent> ${finalResponse}\n`);
  }

  rl.close();
}

main().catch((error: unknown) => {
  console.error(error);
  process.exit(1);
});

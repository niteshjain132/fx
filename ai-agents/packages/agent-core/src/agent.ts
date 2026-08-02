import type { AgentConfig, AgentRunResult, Message, Tool, ToolContext } from "./types.js";

export class Agent {
  readonly name: string;
  readonly instructions: string;
  private readonly tools: Map<string, Tool>;
  private readonly history: Message[];

  constructor(config: AgentConfig) {
    this.name = config.name;
    this.instructions = config.instructions;
    this.tools = new Map((config.tools ?? []).map((tool) => [tool.name, tool]));
    this.history = [{ role: "system", content: config.instructions }];
  }

  getMessages(): Message[] {
    return [...this.history];
  }

  registerTool(tool: Tool): void {
    this.tools.set(tool.name, tool);
  }

  async run(userInput: string): Promise<AgentRunResult> {
    this.history.push({ role: "user", content: userInput });

    const toolResult = await this.tryToolInvocation(userInput);
    const finalResponse = toolResult ?? this.generateResponse(userInput);

    this.history.push({ role: "assistant", content: finalResponse });

    return {
      messages: this.getMessages(),
      finalResponse,
    };
  }

  private async tryToolInvocation(userInput: string): Promise<string | null> {
    const match = userInput.match(/^\/tool\s+(\S+)\s*(.*)$/i);
    if (!match) {
      return null;
    }

    const [, toolName, rawInput] = match;
    const tool = this.tools.get(toolName);
    if (!tool) {
      return `Unknown tool "${toolName}". Available: ${[...this.tools.keys()].join(", ") || "none"}`;
    }

    const context: ToolContext = { agentName: this.name };
    const output = await tool.execute(rawInput.trim(), context);
    const serialized = typeof output === "string" ? output : JSON.stringify(output, null, 2);

    this.history.push({
      role: "tool",
      name: toolName,
      content: serialized,
    });

    return `Tool \`${toolName}\` result:\n${serialized}`;
  }

  private generateResponse(userInput: string): string {
    return [
      `Hi, I'm ${this.name}.`,
      `You said: "${userInput}"`,
      "",
      "This starter agent echoes your message. Wire in an LLM provider or add tools with `/tool <name> <input>`.",
      "Try: `/tool time` or `/tool calc 2 + 2`",
    ].join("\n");
  }
}

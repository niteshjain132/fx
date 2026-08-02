export type Role = "system" | "user" | "assistant" | "tool";

export interface Message {
  role: Role;
  content: string;
  name?: string;
}

export interface ToolContext {
  agentName: string;
}

export interface Tool<TInput = unknown, TOutput = unknown> {
  name: string;
  description: string;
  execute: (input: TInput, context: ToolContext) => Promise<TOutput> | TOutput;
}

export interface AgentConfig {
  name: string;
  instructions: string;
  tools?: Tool[];
}

export interface AgentRunResult {
  messages: Message[];
  finalResponse: string;
}

import { createInitialState, moveLane, updateGame } from "./game";
import { drawFrame } from "./renderer";

const canvas = document.getElementById("game") as HTMLCanvasElement;
const ctx = canvas.getContext("2d")!;
const scoreEl = document.getElementById("score")!;
const speedEl = document.getElementById("speed")!;
const overlay = document.getElementById("overlay")!;
const startBtn = document.getElementById("start-btn")!;

let state = createInitialState();
let lastTime = 0;
let bestScore = Number(localStorage.getItem("rickshaw-best") ?? 0);

function resizeCanvas(): void {
  const maxH = window.innerHeight * 0.95;
  const scale = Math.min(1, maxH / canvas.height);
  canvas.style.width = `${canvas.width * scale}px`;
  canvas.style.height = `${canvas.height * scale}px`;
}

function showOverlay(title: string, subtitle: string, buttonText: string): void {
  overlay.querySelector("h1")!.textContent = title;
  overlay.querySelector("p")!.textContent = subtitle;
  startBtn.textContent = buttonText;
  overlay.classList.add("visible");
}

function startGame(): void {
  state = createInitialState();
  state.running = true;
  overlay.classList.remove("visible");
  lastTime = performance.now();
}

function endGame(): void {
  if (state.score > bestScore) {
    bestScore = state.score;
    localStorage.setItem("rickshaw-best", String(bestScore));
  }
  showOverlay(
    "Bumper to Bumper!",
    `Score: ${state.score} · Best: ${bestScore} · Max speed: ${state.speed.toFixed(1)}x`,
    "Try Again"
  );
}

function gameLoop(now: number): void {
  const dt = Math.min((now - lastTime) / 1000, 0.05);
  lastTime = now;

  if (state.running) {
    updateGame(state, dt);
    if (state.gameOver) endGame();
  }

  drawFrame(ctx, state);
  scoreEl.textContent = String(state.score);
  speedEl.textContent = `Speed: ${state.speed.toFixed(1)}x`;

  requestAnimationFrame(gameLoop);
}

function onKeyDown(e: KeyboardEvent): void {
  if (e.key === "ArrowLeft" || e.key === "a" || e.key === "A") {
    e.preventDefault();
    if (state.running) moveLane(state, -1);
  }
  if (e.key === "ArrowRight" || e.key === "d" || e.key === "D") {
    e.preventDefault();
    if (state.running) moveLane(state, 1);
  }
  if (e.key === " " && !state.running) startGame();
}

function onPointerDown(e: PointerEvent): void {
  if (!state.running) return;
  const rect = canvas.getBoundingClientRect();
  const x = e.clientX - rect.left;
  if (x < rect.width / 2) moveLane(state, -1);
  else moveLane(state, 1);
}

startBtn.addEventListener("click", startGame);
window.addEventListener("keydown", onKeyDown);
canvas.addEventListener("pointerdown", onPointerDown);
window.addEventListener("resize", resizeCanvas);

resizeCanvas();
showOverlay(
  "Rickshaw Run",
  "Dodge traffic. Survive. Speed up the streets!",
  "Honk & Go!"
);
requestAnimationFrame(gameLoop);

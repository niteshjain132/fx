import type { GameState, Obstacle } from "./game";
import { LANE_WIDTH, LANES } from "./game";

export function drawFrame(ctx: CanvasRenderingContext2D, state: GameState): void {
  const w = ctx.canvas.width;
  const h = ctx.canvas.height;

  drawSky(ctx, w, h);
  drawRoad(ctx, w, h, state.roadOffset);
  drawObstacles(ctx, state.obstacles);
  drawRickshaw(ctx, state.laneX, 600, state.honkFlash > 0);
  drawVignette(ctx, w, h);
}

function drawSky(ctx: CanvasRenderingContext2D, w: number, h: number): void {
  const grad = ctx.createLinearGradient(0, 0, 0, h * 0.35);
  grad.addColorStop(0, "#ff8c42");
  grad.addColorStop(1, "#ffd89b");
  ctx.fillStyle = grad;
  ctx.fillRect(0, 0, w, h * 0.35);

  ctx.fillStyle = "#c4a882";
  for (let i = 0; i < 6; i++) {
    const bx = (i * 90 + (Date.now() / 50) % 90) % w;
    ctx.fillRect(bx, 40 + (i % 3) * 30, 60, 80 + (i % 2) * 40);
  }
}

function drawRoad(ctx: CanvasRenderingContext2D, w: number, h: number, offset: number): void {
  ctx.fillStyle = "#4a4a4a";
  ctx.fillRect(0, h * 0.28, w, h);

  ctx.strokeStyle = "#fff";
  ctx.lineWidth = 3;
  ctx.setLineDash([20, 20]);
  ctx.lineDashOffset = -offset;
  for (let i = 1; i < LANES; i++) {
    const x = i * LANE_WIDTH;
    ctx.beginPath();
    ctx.moveTo(x, h * 0.28);
    ctx.lineTo(x, h);
    ctx.stroke();
  }
  ctx.setLineDash([]);

  ctx.fillStyle = "#e8c547";
  ctx.fillRect(0, h * 0.28 - 4, w, 8);
}

function drawObstacles(ctx: CanvasRenderingContext2D, obstacles: Obstacle[]): void {
  for (const obs of obstacles) {
    const x = obs.lane * LANE_WIDTH + (LANE_WIDTH - obs.width) / 2;
    drawObstacle(ctx, obs.kind, x, obs.y, obs.width, obs.height);
  }
}

function drawObstacle(
  ctx: CanvasRenderingContext2D,
  kind: Obstacle["kind"],
  x: number,
  y: number,
  w: number,
  h: number
): void {
  switch (kind) {
    case "car":
      ctx.fillStyle = "#e53935";
      roundRect(ctx, x, y, w, h, 8);
      ctx.fillStyle = "#81d4fa";
      ctx.fillRect(x + 8, y + 10, w - 16, 18);
      ctx.fillStyle = "#333";
      ctx.beginPath();
      ctx.arc(x + 14, y + h - 6, 8, 0, Math.PI * 2);
      ctx.arc(x + w - 14, y + h - 6, 8, 0, Math.PI * 2);
      ctx.fill();
      break;
    case "truck":
      ctx.fillStyle = "#1565c0";
      roundRect(ctx, x, y, w, h * 0.55, 6);
      ctx.fillStyle = "#0d47a1";
      roundRect(ctx, x + 4, y + h * 0.5, w - 8, h * 0.45, 4);
      ctx.fillStyle = "#333";
      ctx.beginPath();
      ctx.arc(x + 14, y + h - 8, 9, 0, Math.PI * 2);
      ctx.arc(x + w - 14, y + h - 8, 9, 0, Math.PI * 2);
      ctx.fill();
      break;
    case "cow":
      ctx.fillStyle = "#8d6e63";
      roundRect(ctx, x, y + 10, w, h - 10, 12);
      ctx.fillStyle = "#fff";
      ctx.fillRect(x + 10, y + 20, 18, 14);
      ctx.fillRect(x + w - 28, y + 24, 14, 12);
      ctx.fillStyle = "#5d4037";
      ctx.beginPath();
      ctx.arc(x + 8, y + 8, 10, 0, Math.PI * 2);
      ctx.fill();
      break;
    case "pothole":
      ctx.fillStyle = "#2a2a2a";
      ctx.beginPath();
      ctx.ellipse(x + w / 2, y + h / 2, w / 2, h / 2, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.strokeStyle = "#1a1a1a";
      ctx.lineWidth = 3;
      ctx.stroke();
      break;
    case "cart":
      ctx.fillStyle = "#ff7043";
      roundRect(ctx, x, y + 20, w, h - 30, 6);
      ctx.strokeStyle = "#5d4037";
      ctx.lineWidth = 3;
      ctx.beginPath();
      ctx.moveTo(x + w / 2, y);
      ctx.lineTo(x + w / 2, y + 20);
      ctx.stroke();
      ctx.fillStyle = "#333";
      ctx.beginPath();
      ctx.arc(x + w / 2, y + h - 8, 10, 0, Math.PI * 2);
      ctx.fill();
      break;
  }
}

function drawRickshaw(ctx: CanvasRenderingContext2D, x: number, y: number, honking: boolean): void {
  const w = 50;
  const h = 70;
  const left = x - w / 2;
  const top = y - h / 2;

  ctx.save();
  if (honking) {
    ctx.shadowColor = "#ffeb3b";
    ctx.shadowBlur = 20;
  }

  ctx.fillStyle = "#333";
  ctx.beginPath();
  ctx.arc(left + 12, top + h - 4, 9, 0, Math.PI * 2);
  ctx.arc(left + w - 12, top + h - 4, 9, 0, Math.PI * 2);
  ctx.fill();

  ctx.fillStyle = "#fdd835";
  roundRect(ctx, left + 4, top + 28, w - 8, h - 36, 6);

  ctx.fillStyle = "#43a047";
  ctx.beginPath();
  ctx.moveTo(left + 2, top + 28);
  ctx.lineTo(left + w / 2, top + 4);
  ctx.lineTo(left + w - 2, top + 28);
  ctx.closePath();
  ctx.fill();

  ctx.fillStyle = "#1a1a1a";
  ctx.fillRect(left + 14, top + 36, 22, 16);

  if (honking) {
    ctx.font = "bold 14px sans-serif";
    ctx.fillStyle = "#ffeb3b";
    ctx.fillText("HONK!", left + w + 4, top + 20);
  }

  ctx.restore();
}

function roundRect(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  w: number,
  h: number,
  r: number
): void {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.lineTo(x + w - r, y);
  ctx.quadraticCurveTo(x + w, y, x + w, y + r);
  ctx.lineTo(x + w, y + h - r);
  ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
  ctx.lineTo(x + r, y + h);
  ctx.quadraticCurveTo(x, y + h, x, y + h - r);
  ctx.lineTo(x, y + r);
  ctx.quadraticCurveTo(x, y, x + r, y);
  ctx.closePath();
  ctx.fill();
}

function drawVignette(ctx: CanvasRenderingContext2D, w: number, h: number): void {
  const grad = ctx.createRadialGradient(w / 2, h / 2, h * 0.2, w / 2, h / 2, h * 0.75);
  grad.addColorStop(0, "rgba(0,0,0,0)");
  grad.addColorStop(1, "rgba(0,0,0,0.35)");
  ctx.fillStyle = grad;
  ctx.fillRect(0, 0, w, h);
}

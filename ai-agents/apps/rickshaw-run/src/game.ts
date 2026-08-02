export const LANES = 3;
export const LANE_WIDTH = 400 / LANES;

export type ObstacleKind = "car" | "truck" | "cow" | "pothole" | "cart";

export interface Obstacle {
  lane: number;
  y: number;
  kind: ObstacleKind;
  width: number;
  height: number;
}

export interface GameState {
  running: boolean;
  gameOver: boolean;
  lane: number;
  laneX: number;
  targetLaneX: number;
  obstacles: Obstacle[];
  score: number;
  speed: number;
  baseSpeed: number;
  spawnTimer: number;
  elapsed: number;
  roadOffset: number;
  honkFlash: number;
}

export function createInitialState(): GameState {
  const lane = 1;
  const laneX = laneCenterX(lane);
  return {
    running: false,
    gameOver: false,
    lane,
    laneX,
    targetLaneX: laneX,
    obstacles: [],
    score: 0,
    speed: 1,
    baseSpeed: 180,
    spawnTimer: 0,
    elapsed: 0,
    roadOffset: 0,
    honkFlash: 0,
  };
}

export function laneCenterX(lane: number): number {
  return lane * LANE_WIDTH + LANE_WIDTH / 2;
}

export function spawnObstacle(state: GameState): Obstacle {
  const kinds: ObstacleKind[] = ["car", "car", "truck", "cow", "pothole", "cart"];
  const kind = kinds[Math.floor(Math.random() * kinds.length)]!;
  const sizes: Record<ObstacleKind, [number, number]> = {
    car: [52, 88],
    truck: [58, 110],
    cow: [64, 48],
    pothole: [70, 24],
    cart: [48, 72],
  };
  const [width, height] = sizes[kind];
  const lane = Math.floor(Math.random() * LANES);
  return { lane, y: -height - 20, kind, width, height };
}

export function updateGame(state: GameState, dt: number): void {
  if (!state.running || state.gameOver) return;

  state.elapsed += dt;
  state.score = Math.floor(state.elapsed * 10 * state.speed);
  state.speed = 1 + state.elapsed * 0.08;
  state.roadOffset = (state.roadOffset + state.baseSpeed * state.speed * dt) % 60;

  state.laneX += (state.targetLaneX - state.laneX) * Math.min(1, dt * 14);

  state.spawnTimer -= dt;
  const spawnInterval = Math.max(0.45, 1.4 - state.elapsed * 0.02);
  if (state.spawnTimer <= 0) {
    state.obstacles.push(spawnObstacle(state));
    state.spawnTimer = spawnInterval + Math.random() * 0.3;
  }

  const scroll = state.baseSpeed * state.speed * dt;
  for (const obs of state.obstacles) {
    obs.y += scroll;
  }
  state.obstacles = state.obstacles.filter((o) => o.y < 750);

  if (state.honkFlash > 0) state.honkFlash -= dt;

  checkCollision(state);
}

function checkCollision(state: GameState): void {
  const playerY = 600;
  const playerW = 50;
  const playerH = 70;
  const px = state.laneX - playerW / 2;
  const py = playerY - playerH / 2;

  for (const obs of state.obstacles) {
    const ox = obs.lane * LANE_WIDTH + (LANE_WIDTH - obs.width) / 2;
    const oy = obs.y;
    if (px < ox + obs.width - 6 && px + playerW - 6 > ox && py < oy + obs.height - 4 && py + playerH - 4 > oy) {
      state.gameOver = true;
      state.running = false;
      return;
    }
  }
}

export function moveLane(state: GameState, direction: -1 | 1): void {
  const next = state.lane + direction;
  if (next < 0 || next >= LANES) {
    state.honkFlash = 0.15;
    return;
  }
  state.lane = next;
  state.targetLaneX = laneCenterX(next);
}

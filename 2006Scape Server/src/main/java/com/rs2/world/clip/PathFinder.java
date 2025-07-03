package com.rs2.world.clip;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.nio.file.Path;
import com.rs2.game.players.Player;
import com.rs2.util.Misc;
import com.rs2.world.clip.AStarPathfinder;
import com.rs2.world.clip.LinkGraph;

public class PathFinder {

	private static final PathFinder pathFinder = new PathFinder();

	public static PathFinder getPathFinder() {
		return pathFinder;
	}

        public void findRoute(Player player, int destX, int destY, boolean moveNear,
                        int xLength, int yLength) {
                if (destX == player.getLocalX() && destY == player.getLocalY() && !moveNear) {
                        player.getPacketSender().sendMessage("ERROR!");
                        return;
                }
                List<int[]> path = computeFullPath(player.absX, player.absY, destX,
                                destY, player.heightLevel);
                if (path == null || path.isEmpty()) {
                        return;
                }
                player.resetWalkingQueue();
                for (int[] step : path) {
                        player.addToWalkingQueue(localize(step[0], player.getMapRegionX()),
                                        localize(step[1], player.getMapRegionY()));
                }
        }

	public int getRegionCoordinate(int x) {
		return (x >> 3) - 6;
	}

	public int getLocalCoordinate(int x) {
		return x - 8 * getRegionCoordinate(x);
	}

        public boolean accessible(int x, int y, int heightLevel, int destX, int destY) {
                List<int[]> path = computeFullPath(x, y, destX, destY, heightLevel);
                return path != null && !path.isEmpty();
        }

        private List<int[]> computeFullPath(int startX, int startY, int endX, int endY, int height) {
                LinkGraph graph = new LinkGraph(
                                Path.of(System.getProperty("user.dir"), "data", "doors.json"),
                                Path.of(System.getProperty("user.dir"), "data", "teleports.json"),
                                Path.of(System.getProperty("user.dir"), "data", "boats.json"));
                LinkGraph.Node start = new LinkGraph.Node(startX, startY, height);
                LinkGraph.Node goal = new LinkGraph.Node(endX, endY, height);
                List<LinkGraph.Node> nodes = graph.shortestPath(start, goal);
                if (nodes.isEmpty()) {
                        nodes = new ArrayList<>();
                        nodes.add(start);
                        nodes.add(goal);
                }
                List<int[]> out = new ArrayList<>();
                for (int i = 0; i < nodes.size() - 1; i++) {
                        LinkGraph.Node a = nodes.get(i);
                        LinkGraph.Node b = nodes.get(i + 1);
                        List<int[]> seg = computeSegmentPath(a.x, a.y, b.x, b.y, a.height);
                        if (seg == null) {
                                return null;
                        }
                        if (!out.isEmpty()) {
                                seg.remove(0);
                        }
                        out.addAll(seg);
                }
                return out;
        }

        private List<int[]> computeSegmentPath(int startX, int startY, int endX, int endY, int height) {
                return AStarPathfinder.findPath(startX, startY, endX, endY, height);
        }

	public static boolean isProjectilePathClear(int x0, int y0, int z, int x1, int y1) {
		int deltaX = x1 - x0;
		int deltaY = y1 - y0;

		double error = 0;
		final double deltaError = Math.abs(
				(deltaY) / (deltaX == 0
						? ((double) deltaY)
						: ((double) deltaX)));

		int x = x0;
		int y = y0;

		int pX = x;
		int pY = y;

		boolean incrX = x0 < x1;
		boolean incrY = y0 < y1;

		while (true) {
			if (x != x1) {
				x += (incrX ? 1 : -1);
			}

			if (y != y1) {
				error += deltaError;

				if (error >= 0.5) {
					y += (incrY ? 1 : -1);
					error -= 1;
				}
			}

			if (!shootable(x, y, z, pX, pY)) {
				return false;
			}

			if (incrX && incrY
					&& x >= x1 && y >= y1) {
				break;
			} else if (!incrX && !incrY
					&& x <= x1 && y <= y1) {
				break;
			} else if (!incrX && incrY
					&& x <= x1 && y >= y1) {
				break;
			} else if (incrX && !incrY
					&& x >= x1 && y <= y1) {
				break;
			}

			pX = x;
			pY = y;
		}

		return true;
	}

	private static boolean shootable(int x, int y, int z, int px, int py) {
		if (x == px && y == py) {
			return true;
		}

		int[] delta1 = Misc.delta(x, y, px, py);
		int[] delta2 = Misc.delta(px, py, x, y);

		int dir = Misc.directionFromDelta(delta1[0], delta1[1]);
		int dir2 = Misc.directionFromDelta(delta2[0], delta2[1]);

		if (dir == -1 || dir2 == -1) {
			return false;
		}

		return Region.canMove(x, y, z, dir) && Region.canMove(px, py, z, dir2)
				|| Region.canShoot(x, y, z, dir) && Region.canShoot(px, py, z, dir2);
	}

	public int localize(int x, int mapRegion) {
		return x - 8 * mapRegion;
	}

}

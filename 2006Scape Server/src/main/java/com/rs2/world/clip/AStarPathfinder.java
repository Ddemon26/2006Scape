package com.rs2.world.clip;

import java.util.*;

/**
 * Simple A* pathfinder operating on the Region clipping map.
 */
public class AStarPathfinder {
    private static class Node implements Comparable<Node> {
        final int x;
        final int y;
        final int g;
        final int f;
        final Node parent;

        Node(int x, int y, int g, int f, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.f, o.f);
        }
    }

    private static int heuristic(int x, int y, int destX, int destY) {
        return Math.abs(x - destX) + Math.abs(y - destY);
    }

    /**
     * Finds a path between two absolute tiles using A* search.
     *
     * @return list of absolute coordinate pairs or {@code null} if no path found
     */
    public static List<int[]> findPath(int startX, int startY, int destX, int destY, int height) {
        int regionX = (startX >> 3) - 6;
        int regionY = (startY >> 3) - 6;
        int localStartX = startX - regionX * 8;
        int localStartY = startY - regionY * 8;
        int localDestX = destX - regionX * 8;
        int localDestY = destY - regionY * 8;

        boolean[][] closed = new boolean[104][104];
        Node[][] nodes = new Node[104][104];
        PriorityQueue<Node> open = new PriorityQueue<>();

        Node start = new Node(localStartX, localStartY, 0,
                heuristic(localStartX, localStartY, localDestX, localDestY), null);
        open.add(start);
        nodes[localStartX][localStartY] = start;

        while (!open.isEmpty()) {
            Node cur = open.poll();
            if (closed[cur.x][cur.y]) {
                continue;
            }
            closed[cur.x][cur.y] = true;

            if (cur.x == localDestX && cur.y == localDestY) {
                List<int[]> out = new ArrayList<>();
                Node n = cur;
                while (n != null) {
                    out.add(new int[] {regionX * 8 + n.x, regionY * 8 + n.y});
                    n = n.parent;
                }
                Collections.reverse(out);
                return out;
            }

            expand(height, regionX, regionY, localDestX, localDestY, cur, nodes, closed, open);
        }
        return null;
    }

    private static void expand(int height, int regionX, int regionY, int destX, int destY, Node cur,
            Node[][] nodes, boolean[][] closed, PriorityQueue<Node> open) {
        int curAbsX = regionX * 8 + cur.x;
        int curAbsY = regionY * 8 + cur.y;
        int g = cur.g + 1;
        for (int dir = 0; dir < 8; dir++) {
            int nx = cur.x;
            int ny = cur.y;
            int absX = curAbsX;
            int absY = curAbsY;
            switch (dir) {
                case 0:
                    ny -= 1;
                    absY -= 1;
                    if (ny < 0
                            || (Region.getClipping(absX, absY, height) & 0x1280102) != 0) {
                        continue;
                    }
                    break;
                case 1:
                    nx -= 1;
                    absX -= 1;
                    if (nx < 0
                            || (Region.getClipping(absX, absY, height) & 0x1280108) != 0) {
                        continue;
                    }
                    break;
                case 2:
                    ny += 1;
                    absY += 1;
                    if (ny >= 104
                            || (Region.getClipping(absX, absY, height) & 0x1280120) != 0) {
                        continue;
                    }
                    break;
                case 3:
                    nx += 1;
                    absX += 1;
                    if (nx >= 104
                            || (Region.getClipping(absX, absY, height) & 0x1280180) != 0) {
                        continue;
                    }
                    break;
                case 4:
                    nx -= 1;
                    ny -= 1;
                    absX -= 1;
                    absY -= 1;
                    if (nx < 0 || ny < 0
                            || (Region.getClipping(absX, absY, height) & 0x128010e) != 0
                            || (Region.getClipping(absX, absY + 1, height) & 0x1280108) != 0
                            || (Region.getClipping(absX + 1, absY, height) & 0x1280102) != 0) {
                        continue;
                    }
                    break;
                case 5:
                    nx -= 1;
                    ny += 1;
                    absX -= 1;
                    absY += 1;
                    if (nx < 0 || ny >= 104
                            || (Region.getClipping(absX, absY, height) & 0x1280138) != 0
                            || (Region.getClipping(absX, absY - 1, height) & 0x1280108) != 0
                            || (Region.getClipping(absX + 1, absY, height) & 0x1280120) != 0) {
                        continue;
                    }
                    break;
                case 6:
                    nx += 1;
                    ny -= 1;
                    absX += 1;
                    absY -= 1;
                    if (nx >= 104 || ny < 0
                            || (Region.getClipping(absX, absY, height) & 0x1280183) != 0
                            || (Region.getClipping(absX - 1, absY, height) & 0x1280180) != 0
                            || (Region.getClipping(absX, absY + 1, height) & 0x1280102) != 0) {
                        continue;
                    }
                    break;
                case 7:
                    nx += 1;
                    ny += 1;
                    absX += 1;
                    absY += 1;
                    if (nx >= 104 || ny >= 104
                            || (Region.getClipping(absX, absY, height) & 0x12801e0) != 0
                            || (Region.getClipping(absX - 1, absY, height) & 0x1280180) != 0
                            || (Region.getClipping(absX, absY - 1, height) & 0x1280120) != 0) {
                        continue;
                    }
                    break;
            }
            if (closed[nx][ny]) {
                continue;
            }
            Node existing = nodes[nx][ny];
            int h = heuristic(nx, ny, destX, destY);
            if (existing == null || g < existing.g) {
                Node next = new Node(nx, ny, g, g + h, cur);
                nodes[nx][ny] = next;
                open.add(next);
            }
        }
    }
}

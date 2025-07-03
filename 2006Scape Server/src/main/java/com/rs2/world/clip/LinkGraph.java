package com.rs2.world.clip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Graph of world links such as doors or teleports.
 */
public class LinkGraph {
    public static class Node {
        public final int x;
        public final int y;
        public final int height;

        public Node(int x, int y, int height) {
            this.x = x;
            this.y = y;
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y && height == node.height;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, height);
        }
    }

    private static class Edge {
        final Node target;
        final int weight;

        Edge(Node target, int weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    private final Map<Node, List<Edge>> graph = new HashMap<>();

    public LinkGraph(Path doors, Path teleports, Path boats) {
        if (doors != null && Files.exists(doors)) {
            parseDoors(doors);
        }
        if (teleports != null && Files.exists(teleports)) {
            parseSimpleLinks(teleports);
        }
        if (boats != null && Files.exists(boats)) {
            parseSimpleLinks(boats);
        }
    }

    private void parseDoors(Path file) {
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONArray arr = new JSONArray(sb.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int face = obj.optInt("face", 0);
                JSONArray locs = obj.getJSONArray("locations");
                for (int j = 0; j < locs.length(); j++) {
                    JSONObject loc = locs.getJSONObject(j);
                    int x = loc.getInt("x");
                    int y = loc.getInt("y");
                    int h = loc.getInt("height");
                    Node a = new Node(x, y, h);
                    Node b;
                    switch (face) {
                        case 0:
                            b = new Node(x, y + 1, h);
                            break;
                        case 1:
                            b = new Node(x - 1, y, h);
                            break;
                        case 2:
                            b = new Node(x, y - 1, h);
                            break;
                        case 3:
                            b = new Node(x + 1, y, h);
                            break;
                        default:
                            b = new Node(x, y, h);
                    }
                    addEdge(a, b, 1);
                    addEdge(b, a, 1);
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private void parseSimpleLinks(Path file) {
        // Expected format: [{"x":..,"y":..,"h":..,"tx":..,"ty":..,"th":..,"cost":1}]
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONArray arr = new JSONArray(sb.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Node a = new Node(o.getInt("x"), o.getInt("y"), o.optInt("h", 0));
                Node b = new Node(o.getInt("tx"), o.getInt("ty"), o.optInt("th", 0));
                int cost = o.optInt("cost", 1);
                addEdge(a, b, cost);
                addEdge(b, a, cost);
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private void addEdge(Node from, Node to, int weight) {
        graph.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to, weight));
    }

    public List<Node> shortestPath(Node start, Node goal) {
        Map<Node, Integer> dist = new HashMap<>();
        Map<Node, Node> prev = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        dist.put(start, 0);
        pq.add(start);
        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (cur.equals(goal)) {
                break;
            }
            for (Edge e : graph.getOrDefault(cur, Collections.emptyList())) {
                int nd = dist.get(cur) + e.weight;
                if (nd < dist.getOrDefault(e.target, Integer.MAX_VALUE)) {
                    dist.put(e.target, nd);
                    prev.put(e.target, cur);
                    pq.add(e.target);
                }
            }
        }
        if (!dist.containsKey(goal)) {
            return Collections.emptyList();
        }
        List<Node> path = new ArrayList<>();
        for (Node at = goal; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}

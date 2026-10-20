package graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Один тип для направленных и ненаправленных взвешенных графов. */
public final class Graph {
    public record Edge(int from, int to, int weight) { }

    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges = new ArrayList<>();
    private final int[] indegrees;

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("Число вершин не может быть отрицательным");
        this.n = n;
        this.directed = directed;
        this.indegrees = new int[n];
        this.adjacency = new ArrayList<>(n);
        for (int v = 0; v < n; v++) adjacency.add(new ArrayList<>());
    }

    public int vertexCount() { return n; }
    public int edgeCount() { return edges.size(); }
    public boolean isDirected() { return directed; }
    public int indegree(int v) { checkVertex(v); return indegrees[v]; }
    public int[] indegrees() { return indegrees.clone(); }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public List<Edge> outgoing(int v) {
        checkVertex(v);
        return Collections.unmodifiableList(adjacency.get(v));
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge); // Логическое ребро хранится здесь ровно один раз.
        adjacency.get(from).add(edge);
        indegrees[to]++;
        if (!directed) {
            adjacency.get(to).add(new Edge(to, from, weight));
            indegrees[from]++;
        }
    }

    public Graph reversed() {
        requireDirected();
        Graph reverse = new Graph(n, true);
        for (Edge edge : edges) reverse.addEdge(edge.to(), edge.from(), edge.weight());
        return reverse;
    }

    public void requireDirected() {
        if (!directed) throw new IllegalArgumentException("Нужен направленный граф");
    }
    public void requireUndirected() {
        if (directed) throw new IllegalArgumentException("Нужен ненаправленный граф");
    }
    public void checkVertex(int v) {
        if (v < 0 || v >= n) throw new IllegalArgumentException("Нет вершины " + v);
    }

    /** Формат: n m directed|undirected, затем ровно m строк u v w. */
    public static Graph read(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null) throw new IllegalArgumentException("Пустой файл");
            String[] fields = header.trim().split("\\s+");
            if (fields.length != 3) throw new IllegalArgumentException("Неверный заголовок");
            boolean directed = switch (fields[2]) {
                case "directed" -> true;
                case "undirected" -> false;
                default -> throw new IllegalArgumentException("Неверный тип графа");
            };
            int m = Integer.parseInt(fields[1]);
            if (m < 0) throw new IllegalArgumentException("Отрицательное число рёбер");
            Graph graph = new Graph(Integer.parseInt(fields[0]), directed);
            for (int i = 0; i < m; i++) {
                String line = reader.readLine();
                if (line == null) throw new IllegalArgumentException("Недостаточно рёбер");
                String[] edge = line.trim().split("\\s+");
                if (edge.length != 3) throw new IllegalArgumentException("Неверная строка ребра");
                graph.addEdge(Integer.parseInt(edge[0]), Integer.parseInt(edge[1]), Integer.parseInt(edge[2]));
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) throw new IllegalArgumentException("Лишние строки после рёбер");
            }
            return graph;
        }
    }
}

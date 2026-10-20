package algorithms;

import graph.Graph;
import graph.Graph.Edge;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.PriorityQueue;

/** Lazy Prim: очередь хранит рёбра; устаревшие кандидаты пропускаются. */
public final class Prim {
    private Prim() { }

    public static MstResult run(Graph graph, Metrics metrics) {
        graph.requireUndirected();
        boolean[] visited = new boolean[graph.vertexCount()];
        var queue = new PriorityQueue<Edge>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        var tree = new ArrayList<Edge>();
        int components = 0;
        long weight = 0;
        // Первый старт из 0; дальнейшие старты обрабатывают несвязный граф.
        for (int start = 0; start < graph.vertexCount(); start++) {
            if (visited[start]) continue;
            components++;
            visit(graph, start, visited, queue, metrics);
            while (!queue.isEmpty()) {
                Edge edge = queue.remove();
                if (visited[edge.to()]) continue;
                tree.add(edge);
                weight += edge.weight();
                visit(graph, edge.to(), visited, queue, metrics);
            }
        }
        return new MstResult(weight, tree, components);
    }

    private static void visit(Graph graph, int v, boolean[] visited,
                              PriorityQueue<Edge> queue, Metrics metrics) {
        visited[v] = true;
        for (Edge edge : graph.outgoing(v)) {
            metrics.edgeScans++; // Каждая запись списка смежности: всего 2E.
            if (!visited[edge.to()]) queue.add(edge);
        }
    }
}

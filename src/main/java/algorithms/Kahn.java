package algorithms;

import graph.Graph;
import metrics.Metrics;
import java.util.ArrayDeque;
import java.util.Arrays;

public final class Kahn {
    private Kahn() { }
    public record Result(int[] order, boolean hasCycle) { }

    public static Result run(Graph graph, Metrics metrics) {
        graph.requireDirected();
        int n = graph.vertexCount();
        int[] indegree = graph.indegrees(); // Счётчики степеней поддерживает Graph.addEdge.
        var queue = new ArrayDeque<Integer>();
        for (int v = 0; v < n; v++) if (indegree[v] == 0) queue.add(v);
        int[] order = new int[n];
        int size = 0;
        while (!queue.isEmpty()) {
            int v = queue.remove();
            order[size++] = v;
            for (Graph.Edge edge : graph.outgoing(v)) {
                metrics.edgeScans++;
                if (--indegree[edge.to()] == 0) queue.add(edge.to());
            }
        }
        // При цикле не выдаём частичный результат за топологический порядок.
        return size == n ? new Result(order, false) : new Result(new int[0], true);
    }

    public static boolean isValidOrder(Graph graph, int[] order) {
        graph.requireDirected();
        int n = graph.vertexCount();
        if (order.length != n) return false;
        int[] position = new int[n];
        Arrays.fill(position, -1);
        for (int i = 0; i < n; i++) {
            int v = order[i];
            if (v < 0 || v >= n || position[v] != -1) return false;
            position[v] = i;
        }
        for (Graph.Edge edge : graph.edges()) if (position[edge.from()] >= position[edge.to()]) return false;
        return true;
    }
}

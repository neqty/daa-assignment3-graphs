package algorithms;

import graph.Graph;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DagShortestPath {
    private DagShortestPath() { }
    public static final long INF = Long.MAX_VALUE;

    public record Result(long[] distance, int[] parent) {
        public long sum() {
            long total = 0;
            for (long value : distance) total = Math.addExact(total, value);
            return total;
        }
        public List<Integer> pathTo(int vertex) {
            if (vertex < 0 || vertex >= distance.length) throw new IllegalArgumentException("Нет вершины");
            if (distance[vertex] == INF) return List.of();
            var path = new ArrayList<Integer>();
            for (int v = vertex; v != -1; v = parent[v]) path.add(v);
            Collections.reverse(path);
            return List.copyOf(path);
        }
    }

    public static Result run(Graph graph, Metrics metrics) {
        graph.requireDirected();
        // Время включает Kahn, но edgeScans здесь считает только релаксации.
        // Иначе один и тот же этап Kahn учитывался бы ещё раз в CSV.
        Kahn.Result topology = Kahn.run(graph, new Metrics());
        if (topology.hasCycle()) throw new IllegalArgumentException("Кратчайшие пути требуют DAG: найден цикл");
        long[] distance = new long[graph.vertexCount()];
        int[] parent = new int[graph.vertexCount()];
        Arrays.fill(distance, INF);
        Arrays.fill(parent, -1);
        for (int v = 0; v < graph.vertexCount(); v++) if (graph.indegree(v) == 0) distance[v] = 0;
        for (int v : topology.order()) {
            for (Graph.Edge edge : graph.outgoing(v)) {
                metrics.edgeScans++;
                if (distance[v] == INF) continue;
                long candidate = distance[v] + edge.weight();
                metrics.comparisons++;
                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    parent[edge.to()] = v;
                }
            }
        }
        return new Result(distance, parent);
    }
}

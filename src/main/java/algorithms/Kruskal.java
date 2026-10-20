package algorithms;

import graph.Graph;
import graph.Graph.Edge;
import metrics.Metrics;
import java.util.ArrayList;

public final class Kruskal {
    private Kruskal() { }

    public static MstResult run(Graph graph, Metrics metrics) {
        graph.requireUndirected();
        var sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        DSU dsu = new DSU(graph.vertexCount(), metrics);
        var tree = new ArrayList<Edge>();
        long weight = 0;
        for (Edge edge : sorted) {
            // Связный граф: после V-1 успешных объединений работу можно закончить.
            if (dsu.components() <= 1) break;
            metrics.edgeScans++;
            if (dsu.find(edge.from()) != dsu.find(edge.to())) {
                dsu.union(edge.from(), edge.to());
                tree.add(edge);
                weight += edge.weight();
            }
        }
        return new MstResult(weight, tree, dsu.components());
    }
}

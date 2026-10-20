package algorithms;

import graph.Graph;
import metrics.Metrics;
import java.util.Arrays;

public final class Kosaraju {
    private Kosaraju() { }
    public record Result(int[] component, int count) { }

    public static Result run(Graph graph, Metrics metrics) {
        graph.requireDirected();
        int n = graph.vertexCount();
        boolean[] seen = new boolean[n];
        int[] finish = new int[n];
        int finishSize = 0;
        int[] stack = new int[n];
        int[] nextEdge = new int[n];

        // Кадр DFS хранит вершину и индекс следующего ребра.
        // В finish вершина попадает только ПОСЛЕ всех её потомков.
        for (int root = 0; root < n; root++) {
            if (seen[root]) continue;
            int depth = 1;
            stack[0] = root;
            seen[root] = true;
            metrics.observeDepth(depth);
            while (depth > 0) {
                int v = stack[depth - 1];
                var outgoing = graph.outgoing(v);
                if (nextEdge[v] < outgoing.size()) {
                    int to = outgoing.get(nextEdge[v]++).to();
                    metrics.edgeScans++;
                    if (!seen[to]) {
                        seen[to] = true;
                        stack[depth++] = to;
                        metrics.observeDepth(depth);
                    }
                } else {
                    finish[finishSize++] = v;
                    depth--;
                }
            }
        }

        // Построение транспонированного графа не входит в счётчик двух DFS.
        // Его O(V+E) время и память включены в полную сложность алгоритма.
        Graph reversed = graph.reversed();
        int[] component = new int[n];
        Arrays.fill(component, -1);
        int count = 0;
        for (int i = finishSize - 1; i >= 0; i--) {
            int root = finish[i];
            if (component[root] != -1) continue;
            int depth = 1;
            stack[0] = root;
            component[root] = count;
            metrics.observeDepth(depth);
            while (depth > 0) {
                int v = stack[--depth];
                for (Graph.Edge edge : reversed.outgoing(v)) {
                    metrics.edgeScans++;
                    int to = edge.to();
                    if (component[to] == -1) {
                        component[to] = count; // Помечаем при добавлении: без дубликатов в стеке.
                        stack[depth++] = to;
                        metrics.observeDepth(depth);
                    }
                }
            }
            count++;
        }
        return new Result(component, count);
    }
}
